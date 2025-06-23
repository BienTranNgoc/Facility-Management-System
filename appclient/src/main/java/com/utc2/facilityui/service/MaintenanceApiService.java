package com.utc2.facilityui.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.utc2.facilityui.auth.TokenStorage;
import com.utc2.facilityui.model.MaintenanceRequestClient;
// Import client-side ApiResponse và MaintenanceResponse (cần tạo nếu chưa có)
import com.utc2.facilityui.response.ApiSingleResponse;
import com.utc2.facilityui.response.MaintenanceResponse;


import com.utc2.facilityui.response.Page;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class MaintenanceApiService {
    private final OkHttpClient client;
    private final Gson gson;
    private static final String BASE_URL = "http://localhost:8080/facility"; // Hoặc URL API của bạn
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public MaintenanceApiService() {
        client = new OkHttpClient();
        gson = new GsonBuilder()
                // Cấu hình Gson cho Date/Time nếu cần, ví dụ:
                // .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    /**
     * Gửi yêu cầu bảo trì lên server.
     * @param reportDto Dữ liệu yêu cầu bảo trì.
     * @return Đối tượng MaintenanceResponse từ server nếu thành công.
     * @throws IOException Nếu có lỗi mạng hoặc server trả về lỗi.
     */
    public MaintenanceResponse submitMaintenanceRequest(MaintenanceRequestClient reportDto) throws IOException {
        String token = TokenStorage.getToken();
        if (token == null || token.isEmpty()) {
            throw new IOException("Token xác thực không tồn tại.");
        }

        String url = BASE_URL + "/maintenance"; // Endpoint POST /maintenance
        String jsonRequestBody = gson.toJson(reportDto);
        System.out.println("Gửi yêu cầu bảo trì: " + jsonRequestBody);

        RequestBody body = RequestBody.create(jsonRequestBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseBodyString = (responseBody != null) ? responseBody.string() : null;

            if (!response.isSuccessful()) {
                System.err.println("Lỗi API khi gửi yêu cầu bảo trì. Code: " + response.code() + ", Body: " + responseBodyString);
                throw new IOException("Gửi yêu cầu bảo trì thất bại. Mã lỗi: " + response.code() +
                        (responseBodyString != null ? ". Phản hồi: " + responseBodyString : ""));
            }

            if (responseBodyString == null || responseBodyString.isEmpty()) {
                throw new IOException("Phản hồi rỗng từ server.");
            }
            System.out.println("Phản hồi từ server: " + responseBodyString);
            // Server trả về cấu trúc ApiResponse<MaintenanceResponse>
            // Giả sử client có com.utc2.facilityui.response.ApiResponse tương ứng
            // và com.utc2.facilityui.response.MaintenanceResponse tương ứng
            Type apiResponseType = new TypeToken<ApiSingleResponse<MaintenanceResponse>>() {}.getType();
            try {
                ApiSingleResponse<MaintenanceResponse> apiResponse = gson.fromJson(responseBodyString, apiResponseType);

                // Server có thể không trả về trường "code" trong JSON nếu chỉ dùng builder().result().build()
                // Chúng ta dựa vào response.isSuccessful() là chính.
                // Nếu apiResponse.getResult() là null, có thể do parse lỗi hoặc cấu trúc JSON không khớp.
                if (apiResponse != null && apiResponse.getResult() != null) {
                    return apiResponse.getResult();
                } else {
                    // Thử parse trực tiếp MaintenanceResponse nếu không có wrapper ApiResponse
                    try {
                        MaintenanceResponse directResponse = gson.fromJson(responseBodyString, MaintenanceResponse.class);
                        if (directResponse != null && directResponse.getId() != null) return directResponse;
                    } catch (JsonSyntaxException e_direct) {
                        // ignore, fall through to throw main exception
                    }
                    System.err.println("Không thể parse phản hồi thành công từ server hoặc kết quả null. JSON: " + responseBodyString);
                    throw new IOException("Không thể xử lý phản hồi từ server. Dữ liệu trả về có thể không đúng định dạng.");
                }
            } catch (JsonSyntaxException e) {
                System.err.println("Lỗi parse JSON phản hồi từ server: " + e.getMessage() + ". JSON: " + responseBodyString);
                throw new IOException("Lỗi xử lý dữ liệu trả về từ server: " + e.getMessage(), e);
            }
        }
    }
    public Page<MaintenanceResponse> getMaintenanceTickets(int page, int size, List<String> statuses) throws IOException {
        System.out.println("[ApiService] getMaintenanceTickets - Called with: page=" + page + ", size=" + size + ", statuses=" + statuses);

        String token = TokenStorage.getToken();
        if (token == null || token.isEmpty()) {
            System.err.println("[ApiService] getMaintenanceTickets - Error: Auth token is missing.");
            throw new IOException("Token xác thực không tồn tại.");
        }
        System.out.println("[ApiService] getMaintenanceTickets - Token: " + token.substring(0, Math.min(token.length(), 10)) + "..."); // In một phần token

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/maintenance").newBuilder();
        urlBuilder.addQueryParameter("page", String.valueOf(page));
        urlBuilder.addQueryParameter("size", String.valueOf(size));
        if (statuses != null && !statuses.isEmpty()) {
            for (String status : statuses) {
                urlBuilder.addQueryParameter("status", status);
            }
        }

        String url = urlBuilder.build().toString();
        System.out.println("[ApiService] getMaintenanceTickets - Calling API URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .get()
                .build();

        System.out.println("[ApiService] getMaintenanceTickets - Executing request...");
        try (Response response = client.newCall(request).execute()) {
            System.out.println("[ApiService] getMaintenanceTickets - Response Code: " + response.code());
            ResponseBody responseBody = response.body();
            String responseBodyString = (responseBody != null) ? responseBody.string() : null;

            System.out.println("[ApiService] getMaintenanceTickets - Raw API Response Body: " + responseBodyString);

            if (!response.isSuccessful()) {
                System.err.println("[ApiService] getMaintenanceTickets - API call failed. Code: " + response.code() + ", Body: " + responseBodyString);
                throw new IOException("Lấy danh sách bảo trì thất bại. Mã lỗi: " + response.code() +
                        (responseBodyString != null ? ". Phản hồi: " + responseBodyString : ""));
            }

            if (responseBodyString == null || responseBodyString.isEmpty()) {
                System.err.println("[ApiService] getMaintenanceTickets - Error: Empty response body from server.");
                throw new IOException("Phản hồi rỗng từ server.");
            }

            Type apiResponseType = new TypeToken<ApiSingleResponse<Page<MaintenanceResponse>>>() {}.getType();
            try {
                ApiSingleResponse<Page<MaintenanceResponse>> apiResponse = gson.fromJson(responseBodyString, apiResponseType);
                System.out.println("[ApiService] getMaintenanceTickets - Parsed apiResponse object: " + apiResponse);


                if (apiResponse != null && apiResponse.getResult() != null) {
                    System.out.println("[ApiService] getMaintenanceTickets - Successfully parsed. Result content size: " + (apiResponse.getResult().getContent() != null ? apiResponse.getResult().getContent().size() : "null content"));
                    return apiResponse.getResult();
                } else {
                    System.err.println("[ApiService] getMaintenanceTickets - Error: Parsed apiResponse or its result is null. JSON: " + responseBodyString);
                    // Thử parse trực tiếp sang Page<MaintenanceResponse> nếu wrapper không đúng
                    try {
                        System.out.println("[ApiService] getMaintenanceTickets - Attempting direct parse to Page<MaintenanceResponse>...");
                        Type pageType = new TypeToken<Page<MaintenanceResponse>>() {}.getType();
                        Page<MaintenanceResponse> directPageResponse = gson.fromJson(responseBodyString, pageType);
                        if (directPageResponse != null && directPageResponse.getContent() != null) {
                            System.out.println("[ApiService] getMaintenanceTickets - Direct parse to Page successful. Content size: " + directPageResponse.getContent().size());
                            return directPageResponse;
                        } else {
                            System.err.println("[ApiService] getMaintenanceTickets - Direct parse to Page also failed or content is null.");
                        }
                    } catch (JsonSyntaxException eDirect) {
                        System.err.println("[ApiService] getMaintenanceTickets - JsonSyntaxException during direct Page parse: " + eDirect.getMessage());
                    }
                    throw new IOException("Không thể xử lý phản hồi từ server (Page). Dữ liệu trả về có thể không đúng định dạng hoặc kết quả null.");
                }
            } catch (JsonSyntaxException e) {
                System.err.println("[ApiService] getMaintenanceTickets - JsonSyntaxException during ApiSingleResponse<Page> parse: " + e.getMessage() + ". JSON: " + responseBodyString);
                throw new IOException("Lỗi xử lý dữ liệu (Page) trả về từ server: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            System.err.println("[ApiService] getMaintenanceTickets - IOException during API call: " + e.getMessage());
            throw e; // Ném lại lỗi để controller xử lý
        }
    }
}