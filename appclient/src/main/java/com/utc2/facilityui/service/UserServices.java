package com.utc2.facilityui.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
// Import your LocalDateTimeAdapter
import com.utc2.facilityui.utils.LocalDateTimeAdapter; // <--- IMPORT YOUR ADAPTER

import com.google.gson.reflect.TypeToken;
import com.utc2.facilityui.auth.TokenStorage;
import com.utc2.facilityui.helper.Config;
import com.utc2.facilityui.model.User; // Model User của client
import com.utc2.facilityui.model.UserUpdateRequest; // Model UserUpdateRequest của client
import com.utc2.facilityui.response.*; // Các lớp Api Response của client

import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime; // Import LocalDateTime
// Các import khác không cần thiết cho ví dụ này đã được lược bỏ để ngắn gọn
// ... (ví dụ: java.sql.*, java.util.ArrayList, etc.) ...
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class UserServices {
    private static final String BASE_URL = Config.getOrDefault("BASE_URL", "http://localhost:8080/api");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    // CẬP NHẬT: Khởi tạo Gson với LocalDateTimeAdapter của bạn
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private static final int API_SUCCESS_CODE = 0; // Xác nhận lại mã này với backend

    public static User getMyInfo() throws IOException {
        String token = TokenStorage.getToken();
        if (token == null || token.isEmpty()) {
            System.err.println("UserServices.getMyInfo: Token is missing.");
            throw new IOException("Authentication token is required to get user info.");
        }

        String url = (BASE_URL.endsWith("/") ? BASE_URL : BASE_URL + "/") + "users/myInfo";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        System.out.println("UserServices.getMyInfo: Requesting User Info from: " + url);

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseData = (responseBody != null) ? responseBody.string() : null;
            System.out.println("UserServices.getMyInfo: Received Response: Code=" + response.code() + ", Body (first 150 chars): " + (responseData != null && responseData.length() > 150 ? responseData.substring(0, 150) + "..." : responseData));

            if (!response.isSuccessful()) {
                System.err.println("UserServices.getMyInfo: API Call Failed. HTTP Status: " + response.code() + ". Body: " + responseData);
                throw new IOException("Failed to get user info with HTTP status: " + response.code() + ". Response: " + responseData);
            }
            if (responseData == null || responseData.isEmpty()) {
                System.err.println("UserServices.getMyInfo: Empty response body from server.");
                throw new IOException("Empty response body received from server for user info.");
            }

            try {
                Type apiUserResponseType = new TypeToken<ApiSingleResponse<User>>() {}.getType();
                ApiSingleResponse<User> apiResponse = gson.fromJson(responseData, apiUserResponseType);

                if (apiResponse == null) {
                    System.err.println("UserServices.getMyInfo: Failed to parse main API response structure.");
                    throw new IOException("Could not parse the API response structure for user info.");
                }

                if (apiResponse.getCode() != API_SUCCESS_CODE) {
                    String errorMessage = "Failed to get user info: " +
                            (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown API error") +
                            " (API Code: " + apiResponse.getCode() + ")";
                    System.err.println("UserServices.getMyInfo: API returned business error. " + errorMessage);
                    throw new IOException(errorMessage);
                }

                User user = apiResponse.getResult();
                if (user == null) {
                    System.err.println("UserServices.getMyInfo: User data (result) is null in API response.");
                    throw new IOException("User data (result) was null in the API response.");
                }

                System.out.println("UserServices.getMyInfo: Successfully parsed user info for user: " + user.getUsername());
                return user;

            } catch (com.google.gson.JsonSyntaxException e) {
                System.err.println("UserServices.getMyInfo: JSON Parsing Error - " + e.getMessage() + ". Response data: " + responseData);
                throw new IOException("Invalid JSON response format from server for user info: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            System.err.println("UserServices.getMyInfo: Network or IO Error - " + e.getMessage());
            throw e;
        }
    }

    public static boolean resetPassword(String oldPassword, String newPassword) throws IOException {
        if (!TokenStorage.hasToken()) {
            System.err.println("resetPassword: Token is missing.");
            throw new IOException("Authentication token is required to reset password.");
        }
        String token = TokenStorage.getToken();
        String url = (BASE_URL.endsWith("/") ? BASE_URL : BASE_URL + "/") + "auth/password/reset";

        Map<String, String> requestData = new HashMap<>();
        requestData.put("oldPassword", oldPassword);
        requestData.put("newPassword", newPassword);

        RequestBody body = RequestBody.create(
                gson.toJson(requestData),
                MediaType.get("application/json; charset=utf-8")
        );

        Request apiRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        System.out.println("Requesting Password Reset to: " + url);

        try (Response response = client.newCall(apiRequest).execute()) {
            ResponseBody responseBody = response.body();
            String responseData = (responseBody != null) ? responseBody.string() : null;
            System.out.println("Received Reset Password Response: Code=" + response.code() + ", Body Length=" + (responseData != null ? responseData.length() : "null"));

            if (!response.isSuccessful()) {
                System.err.println("resetPassword: API Call Failed. HTTP Status Code: " + response.code() + ". Body: " + responseData);
                throw new IOException("Password reset failed with HTTP status: " + response.code() + (responseData != null ? " - " + responseData : ""));
            }

            if (responseData == null || responseData.isEmpty() || responseData.trim().equals("{}") || response.code() == 204) {
                System.out.println("resetPassword: Success (assuming based on HTTP 2xx and minimal/no body).");
                return true;
            }

            try {
                Type apiResponseType = new TypeToken<ApiSingleResponse<Void>>() {}.getType();
                ApiSingleResponse<Void> apiResponse = gson.fromJson(responseData, apiResponseType);

                if (apiResponse == null) {
                    System.err.println("resetPassword: Failed to parse JSON response body. Assuming success due to HTTP 2xx status.");
                    return true;
                }

                if (apiResponse.getCode() != API_SUCCESS_CODE) {
                    System.err.println("resetPassword: API returned business error. Code=" + apiResponse.getCode() + ", Message=" + apiResponse.getMessage());
                    throw new IOException("Password reset failed: " + apiResponse.getMessage() + " (Code: " + apiResponse.getCode() + ")");
                }

                System.out.println("resetPassword: Successfully reset password via API response.");
                return true;

            } catch (com.google.gson.JsonSyntaxException e) {
                System.err.println("resetPassword: JSON Parsing Error - " + e.getMessage() + ". Assuming success due to HTTP 2xx status.");
                return true;
            }
        } catch (IOException e) {
            System.err.println("resetPassword: Network or IO Error - " + e.getMessage());
            throw e;
        }
    }

    public static PaginatedUsers getUsers(int page, int size) throws IOException {
        String token = TokenStorage.getToken();
        if (token == null || token.isEmpty()) {
            throw new IOException("Cần token xác thực để lấy danh sách người dùng.");
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse((BASE_URL.endsWith("/") ? BASE_URL : BASE_URL + "/") + "users").newBuilder();
        urlBuilder.addQueryParameter("page", String.valueOf(page));
        urlBuilder.addQueryParameter("size", String.valueOf(size));
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        System.out.println("UserServices.getUsers: Đang yêu cầu từ: " + url);

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseData = (responseBody != null) ? responseBody.string() : null;
            System.out.println("UserServices.getUsers: ResponseData Raw (first 150 chars): " + (responseData != null && responseData.length() > 150 ? responseData.substring(0, 150) + "..." : responseData));


            if (!response.isSuccessful()) {
                throw new IOException("Lấy danh sách người dùng thất bại (HTTP " + response.code() + "): " + (responseData != null ? responseData : "No response body"));
            }
            if (responseData == null || responseData.isEmpty()) {
                throw new IOException("Lấy danh sách người dùng thất bại: Phản hồi rỗng từ server.");
            }

            Type apiResponseType = new TypeToken<ApiContainerForPagedData<com.utc2.facilityui.response.UserResponse>>() {}.getType();
            ApiContainerForPagedData<com.utc2.facilityui.response.UserResponse> apiContainer = gson.fromJson(responseData, apiResponseType);

            if (apiContainer == null) {
                throw new IOException("Lấy danh sách thất bại: Không thể parse cấu trúc phản hồi chính.");
            }
            if (apiContainer.getCode() != API_SUCCESS_CODE) {
                String message = apiContainer.getMessage() != null ? apiContainer.getMessage() : "Lỗi không xác định từ API.";
                throw new IOException("Lấy danh sách thất bại: " + message + " (Code: " + apiContainer.getCode() + ")");
            }
            if (apiContainer.getResult() == null || apiContainer.getResult().getContent() == null || apiContainer.getResult().getPage() == null) {
                throw new IOException("Lấy danh sách thất bại: Phản hồi không hợp lệ hoặc thiếu thông tin phân trang/nội dung.");
            }
            return new PaginatedUsers(apiContainer.getResult().getContent(), apiContainer.getResult().getPage());
        }
    }

    public static class PaginatedUsers {
        private final List<com.utc2.facilityui.response.UserResponse> content;
        private final PageMetadata pageMetadata;

        public PaginatedUsers(List<com.utc2.facilityui.response.UserResponse> content, PageMetadata pageMetadata) {
            this.content = content;
            this.pageMetadata = pageMetadata;
        }

        public List<com.utc2.facilityui.response.UserResponse> getContent() { return content; }
        public PageMetadata getPageMetadata() { return pageMetadata; }
    }

    public static com.utc2.facilityui.response.UserResponse createUser(Object userCreationRequestDto) throws IOException {
        String token = TokenStorage.getToken();
        if (token == null || token.isEmpty()) {
            throw new IOException("Cần token xác thực để tạo người dùng.");
        }

        String url = (BASE_URL.endsWith("/") ? BASE_URL : BASE_URL + "/") + "users";
        RequestBody body = RequestBody.create(gson.toJson(userCreationRequestDto), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
        System.out.println("UserServices.createUser: Sending request to " + url + " with body: " + gson.toJson(userCreationRequestDto));


        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseData = (responseBody != null) ? responseBody.string() : null;
            System.out.println("UserServices.createUser: Response: Code=" + response.code() + ", Body: " + responseData);

            if (!response.isSuccessful()) {
                throw new IOException("Tạo người dùng thất bại (HTTP " + response.code() + "): " + (responseData != null ? responseData : "No response body"));
            }
            if (responseData == null || responseData.isEmpty()) {
                throw new IOException("Tạo người dùng thất bại: Phản hồi rỗng từ server.");
            }

            Type apiResponseType = new TypeToken<ApiSingleResponse<com.utc2.facilityui.response.UserResponse>>() {}.getType();
            ApiSingleResponse<com.utc2.facilityui.response.UserResponse> apiResponse = gson.fromJson(responseData, apiResponseType);

            if (apiResponse == null) {
                throw new IOException("Tạo người dùng thất bại: Không thể parse cấu trúc phản hồi chính.");
            }
            if (apiResponse.getCode() != API_SUCCESS_CODE || apiResponse.getResult() == null) {
                String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Phản hồi không hợp lệ.";
                throw new IOException("Tạo người dùng thất bại: " + message + " (Code: " + apiResponse.getCode() + ")");
            }
            return apiResponse.getResult();
        }
    }

    public static com.utc2.facilityui.response.UserResponse updateUser(String userIdToUpdate, UserUpdateRequest userUpdateRequestDto) throws IOException {
        String token = TokenStorage.getToken();
        if (token == null || token.isEmpty()) {
            throw new IOException("Cần token xác thực để cập nhật người dùng.");
        }

        String url = (BASE_URL.endsWith("/") ? BASE_URL : BASE_URL + "/") + "users/" + userIdToUpdate;
        RequestBody body = RequestBody.create(gson.toJson(userUpdateRequestDto), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .patch(body)// Hoặc .patch(body) tùy theo API server
                .build();
        System.out.println("UserServices.updateUser: Sending request to " + url + " for userId: " + userIdToUpdate + " with body: " + gson.toJson(userUpdateRequestDto));

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseData = (responseBody != null) ? responseBody.string() : null;
            System.out.println("UserServices.updateUser: Response: Code=" + response.code() + ", Body: " + responseData);

            if (!response.isSuccessful()) {
                throw new IOException("Cập nhật người dùng thất bại (HTTP " + response.code() + "): " + (responseData != null ? responseData : "No response body"));
            }
            if (responseData == null || responseData.isEmpty()) {
                throw new IOException("Cập nhật người dùng thất bại: Phản hồi rỗng từ server.");
            }

            Type apiResponseType = new TypeToken<ApiSingleResponse<com.utc2.facilityui.response.UserResponse>>() {}.getType();
            ApiSingleResponse<com.utc2.facilityui.response.UserResponse> apiResponse = gson.fromJson(responseData, apiResponseType);

            if (apiResponse == null) {
                throw new IOException("Cập nhật người dùng thất bại: Không thể parse cấu trúc phản hồi chính.");
            }
            if (apiResponse.getCode() != API_SUCCESS_CODE || apiResponse.getResult() == null) {
                String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Phản hồi không hợp lệ.";
                throw new IOException("Cập nhật người dùng thất bại: " + message + " (Code: " + apiResponse.getCode() + ")");
            }
            return apiResponse.getResult();
        }
    }

    public static void deleteUser(String userIdToDelete) throws IOException {
        String token = TokenStorage.getToken();
        if (token == null || token.isEmpty()) {
            throw new IOException("Cần token xác thực để xóa người dùng.");
        }

        String url = (BASE_URL.endsWith("/") ? BASE_URL : BASE_URL + "/") + "users/" + userIdToDelete;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();
        System.out.println("UserServices.deleteUser: Sending DELETE request to " + url);

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseData = (responseBody != null) ? responseBody.string() : null;
            System.out.println("UserServices.deleteUser: Response: Code=" + response.code() + ", Body: " + responseData);

            if (!response.isSuccessful()) {
                throw new IOException("Xóa người dùng thất bại (HTTP " + response.code() + "): " + (responseData != null ? responseData : "No response body"));
            }

            if (responseData != null && !responseData.isEmpty() && !responseData.trim().equals("{}")) {
                try {
                    Type apiResponseType = new TypeToken<ApiSingleResponse<Void>>() {}.getType();
                    ApiSingleResponse<Void> apiResponse = gson.fromJson(responseData, apiResponseType);

                    if (apiResponse == null) {
                        System.err.println("UserServices.deleteUser: Không thể parse JSON response body. Code HTTP: " + response.code());
                        if (response.code() >= 200 && response.code() < 300) return; // Vẫn coi là thành công nếu HTTP 2xx
                        throw new IOException("Xóa người dùng thất bại: Phản hồi JSON không hợp lệ.");
                    }
                    if (apiResponse.getCode() != API_SUCCESS_CODE) {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi không xác định từ API.";
                        throw new IOException("Xóa người dùng thất bại: " + message + " (Code: " + apiResponse.getCode() + ")");
                    }
                } catch (com.google.gson.JsonSyntaxException e) {
                    System.err.println("UserServices.deleteUser: Lỗi parse JSON response: " + e.getMessage() + ". Body: " + responseData);
                    if (response.code() >= 200 && response.code() < 300) return; // Vẫn coi là thành công nếu HTTP 2xx
                    throw new IOException("Xóa người dùng thất bại: Phản hồi JSON không hợp lệ.", e);
                }
            }
            System.out.println("UserServices.deleteUser: User deleted successfully or no content returned with success code.");
        }
    }

    // --- CÁC PHƯƠNG THỨC TRUY CẬP DATABASE TRỰC TIẾP (CÂN NHẮC LOẠI BỎ) ---
    // Phần này giữ nguyên như code bạn cung cấp trước, bạn nên xem xét lại việc sử dụng chúng.
    // private static final String DB_URL = "jdbc:mysql://localhost:3306/facility";
    // private static final String DB_USER = "root";
    // private static final String DB_PASSWORD = "Tranbien2809@";
    // ... (các phương thức JDBC của bạn) ...
}