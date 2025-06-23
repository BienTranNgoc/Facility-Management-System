package com.utc2.facilityui.controller.nav;

import com.utc2.facilityui.auth.TokenStorage;
import com.utc2.facilityui.service.UserServices;
import com.utc2.facilityui.model.User;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle; // << Import quan trọng
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class SidebarMenuController implements Initializable {

    private BorderPane mainBorderPane;

    @FXML private ImageView imgAdminAvatar;
    @FXML private Text txtAdminName;
    @FXML private Text txtAdminId;

    @FXML private Button manageFacilitiesButton;
    @FXML private Button manageBookingsButton;
    @FXML private Button approvalRequestsButton;
    @FXML private Label approvalCountLabel;
    @FXML private Button cancellationRequestsButton;
    @FXML private Button accountManagementButton;
    // @FXML private Button reportButton; // Bỏ comment nếu bạn có nút này
    @FXML private Button manageDeviceButton; // Thêm nếu bạn có nút này
    @FXML private Button resetPasswordButton;
    @FXML private Button logoutButton;

    private static final String DEFAULT_AVATAR_PATH = "/com/utc2/facilityui/images/man.png";

    public void setMainBorderPane(BorderPane mainBorderPane) {
        this.mainBorderPane = mainBorderPane;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing SidebarMenuController...");

        if (imgAdminAvatar != null) {
            // Lấy kích thước đã được định nghĩa trong FXML (bây giờ là 70.0 x 70.0)
            double fitSize = imgAdminAvatar.getFitWidth(); // Giả định fitWidth = fitHeight

            // Nếu FXML không set hoặc set <=0, dùng giá trị mặc định (dù bạn đã set trong FXML rồi)
            if (fitSize <= 0) {
                fitSize = 70.0; // Kích thước vuông mặc định
                imgAdminAvatar.setFitWidth(fitSize);
                imgAdminAvatar.setFitHeight(fitSize);
            }

            double radius = fitSize / 2.75;
            double centerX = fitSize / 2.0; // Tâm X của clip là giữa ImageView
            double centerY = fitSize / 2.0; // Tâm Y của clip là giữa ImageView (mặc định)

            // --- ĐIỀU CHỈNH ĐỂ TẬP TRUNG VÀO KHUÔN MẶT (DỊCH CLIP LÊN TRÊN) ---
            // Bạn có thể thử nghiệm với giá trị verticalOffsetFactor này.
            // 0.0  : Căn giữa hoàn toàn.
            // 0.1  : Dịch tâm clip lên một chút (10% bán kính).
            // 0.15 : Dịch tâm clip lên nhiều hơn một chút.
            // 0.2  : Dịch tâm clip lên nữa.
            // Hãy bắt đầu với một giá trị và xem kết quả, sau đó tinh chỉnh.
            double verticalOffsetFactor = 0.15; // Thử nghiệm với 15% dịch lên
            centerY = centerY - (radius * verticalOffsetFactor);
            // --------------------------------------------------------------------

            Circle clip = new Circle(centerX, centerY, radius);
            imgAdminAvatar.setClip(clip);

            System.out.println("Sidebar: Avatar clip set. FitSize=" + fitSize +
                    ", Clip CenterX=" + centerX + ", Adjusted CenterY=" + centerY +
                    ", Radius=" + radius);
        } else {
            System.err.println("Sidebar: imgAdminAvatar is null during initialize. Cannot set clip. Check FXML fx:id.");
        }

        // Gọi các phương thức khởi tạo khác của bạn
        setUIToLoadingState();
        loadUserInfo();
        updateApprovalCount(0);
    }

    private void setUIToLoadingState() {
        if (txtAdminName != null) txtAdminName.setText("Đang tải...");
        if (txtAdminId != null) txtAdminId.setText("ID: Đang tải...");
        setDefaultAvatar(); // Hiển thị avatar mặc định trong khi tải
    }

    private void loadUserInfo() {
        System.out.println("Sidebar: Starting to load user info task...");
        Task<User> loadUserTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return UserServices.getMyInfo();
            }
        };

        loadUserTask.setOnSucceeded(event -> {
            User user = loadUserTask.getValue();
            System.out.println("Sidebar: User info task succeeded.");
            Platform.runLater(() -> {
                if (user != null && user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
                    System.out.println("Sidebar: Updating UI with user info for: " + user.getUsername());
                    updateUserInfoUI(user);
                } else {
                    System.out.println("Sidebar: User object or critical fields (like userId) are null or empty.");
                    setUIToDefaultOrError("Lỗi Tải User", "ID: Lỗi", true);
                }
            });
        });

        loadUserTask.setOnFailed(event -> {
            Throwable exception = loadUserTask.getException();
            System.err.println("Sidebar: User info task failed: " + exception.getMessage());
            exception.printStackTrace();
            Platform.runLater(() -> {
                setUIToDefaultOrError("Lỗi", "ID: Lỗi", true);
                showErrorAlert("Lỗi Tải Thông Tin Người Dùng", "Không thể tải thông tin admin: " + exception.getMessage());
            });
        });

        System.out.println("Sidebar: Starting user info loading thread...");
        Thread backgroundThread = new Thread(loadUserTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void updateUserInfoUI(User user) {
        String displayName = (user.getUsername() != null && !user.getUsername().trim().isEmpty()) ? user.getUsername() : "Admin";
        if (txtAdminName != null) txtAdminName.setText(displayName);

        String displayId = user.getUserId() != null ? user.getUserId() : "N/A";
        if (txtAdminId != null) txtAdminId.setText("ID: " + displayId);

        String avatarIdentifier = user.getAvatar();
        updateAvatarImage(avatarIdentifier); // Clip đã được set, chỉ cần set image
    }

    private void updateAvatarImage(String avatarIdentifier) {
        if (imgAdminAvatar == null) {
            System.err.println("Sidebar: imgAdminAvatar is null, cannot update image.");
            return; // Clip đã được set trong initialize
        }

        if (avatarIdentifier != null && !avatarIdentifier.trim().isEmpty()) {
            String path = avatarIdentifier.trim();

            if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
                System.out.println("Sidebar: Avatar is an HTTP URL: " + path + ". Loading from network.");
                try {
                    Image networkImage = new Image(path, true); // true để tải nền
                    imgAdminAvatar.setImage(networkImage); // Set image, clip đã có

                    networkImage.errorProperty().addListener((obs, wasError, isError) -> {
                        if (isError) {
                            System.err.println("Sidebar: Error loading avatar from HTTP URL: " + path +
                                    (networkImage.getException() != null ? ". Reason: " + networkImage.getException().getMessage() : ". Unknown error."));
                            Platform.runLater(this::setDefaultAvatar);
                        }
                    });
                    if (networkImage.isError()) { // Kiểm tra lỗi tức thì
                        System.err.println("Sidebar: Immediate error after creating Image from HTTP URL: " + path);
                        setDefaultAvatar();
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Sidebar: Invalid HTTP URL format for avatar: " + path + " - " + e.getMessage());
                    setDefaultAvatar();
                } catch (Exception e) {
                    System.err.println("Sidebar: Unexpected error creating Image from HTTP URL: " + path + " - " + e.getMessage());
                    e.printStackTrace();
                    setDefaultAvatar();
                }
                return;
            } else {
                // Xử lý như một tài nguyên trong classpath
                String classpathResourcePath = path.startsWith("/") ? path : "/com/utc2/facilityui/images/" + path;
                System.out.println("Sidebar: Attempting to load avatar from classpath: " + classpathResourcePath);
                Image classpathImage = loadImageFromClasspathInternal(classpathResourcePath);
                if (classpathImage != null) {
                    System.out.println("Sidebar: Successfully loaded avatar from classpath: " + classpathResourcePath);
                    imgAdminAvatar.setImage(classpathImage); // Set image, clip đã có
                    return;
                } else {
                    System.err.println("Sidebar: Failed to load avatar from classpath: " + classpathResourcePath);
                }
            }
        }
        // Nếu avatarIdentifier rỗng/null, hoặc tải từ HTTP/classpath thất bại
        System.out.println("Sidebar: Avatar from API/classpath failed or not provided. Loading default avatar.");
        setDefaultAvatar();
    }

    private Image loadImageFromClasspathInternal(String classpathPath) {
        if (classpathPath == null || classpathPath.trim().isEmpty()) {
            System.err.println("Sidebar: Classpath for image is null or empty.");
            return null;
        }
        try (InputStream imageStream = getClass().getResourceAsStream(classpathPath)) {
            if (imageStream != null) {
                Image image = new Image(imageStream);
                if (image.isError()) {
                    System.err.println("Sidebar: Error creating Image object from classpath: " + classpathPath + (image.getException() != null ? " - " + image.getException().getMessage() : ""));
                    return null;
                }
                return image;
            } else {
                System.err.println("Sidebar: Cannot find resource on classpath: " + classpathPath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Sidebar: Exception while loading image from classpath: " + classpathPath + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void setDefaultAvatar() {
        if (imgAdminAvatar == null) {
            System.err.println("Sidebar: imgAdminAvatar is null, cannot set default avatar.");
            return; // Clip đã được set trong initialize
        }
        try (InputStream stream = getClass().getResourceAsStream(DEFAULT_AVATAR_PATH)) {
            if (stream == null) {
                System.err.println("Sidebar CRITICAL: Cannot find default avatar resource: " + DEFAULT_AVATAR_PATH);
                imgAdminAvatar.setImage(null);
                return;
            }
            Image defaultImage = new Image(stream);
            if (defaultImage.isError()) {
                System.err.println("Sidebar CRITICAL: Error loading default avatar object: " + DEFAULT_AVATAR_PATH + (defaultImage.getException() != null ? " - " + defaultImage.getException().getMessage() : ""));
                imgAdminAvatar.setImage(null);
            } else {
                imgAdminAvatar.setImage(defaultImage); // Set image, clip đã có
                System.out.println("Sidebar: Default avatar image set.");
            }
        } catch (Exception e) {
            System.err.println("Sidebar CRITICAL: Exception loading default avatar resource: " + DEFAULT_AVATAR_PATH + " - " + e.getMessage());
            imgAdminAvatar.setImage(null);
            e.printStackTrace();
        }
    }

    private void setUIToDefaultOrError(String nameText, String idText, boolean useDefaultAvatar) {
        if (txtAdminName != null) txtAdminName.setText(nameText);
        if (txtAdminId != null) txtAdminId.setText(idText);
        if (useDefaultAvatar) {
            setDefaultAvatar(); // Phương thức này sẽ tự xử lý việc set ảnh
        }
        System.out.println("Sidebar UI set to default/error state - Name: " + nameText + ", ID: " + idText);
    }

    // --- Các phương thức xử lý sự kiện cho các nút menu ---
    @FXML private void handleManageFacilities(ActionEvent event) { System.out.println("Manage Facilities clicked."); loadView("/com/utc2/facilityui/view/manageFacility.fxml"); }
    @FXML private void handleManageBookings(ActionEvent event) { System.out.println("Manage Bookings clicked."); loadView("/com/utc2/facilityui/view/manageBookings.fxml"); }
    @FXML private void handleApprovalRequests(ActionEvent event) { System.out.println("Approval Requests clicked."); loadView("/com/utc2/facilityui/view/approvalrequests.fxml"); }
    @FXML private void handleCancellationRequests(ActionEvent event) { System.out.println("Overdue Requests clicked."); loadView("/com/utc2/facilityui/view/overdueRequest.fxml"); }
    @FXML private void handleManageDevice(ActionEvent event) { System.out.println("Manage Device clicked."); loadView("/com/utc2/facilityui/view/manageDevice.fxml"); }
    @FXML private void handleResetPassword(ActionEvent event) { System.out.println("Reset Password clicked."); loadView("/com/utc2/facilityui/view/resetpassword.fxml"); }
    @FXML private void handleAccountManagement(ActionEvent event) { System.out.println("Account Management clicked."); loadView("/com/utc2/facilityui/view/accountManagement.fxml"); }
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logout button clicked.");
        TokenStorage.logout();
        System.out.println("Session cleared via TokenStorage.logout().");
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            if (stage == null) { System.err.println("Sidebar: Cannot get stage for logout."); return; }
            String loginFxmlPath = "/com/utc2/facilityui/view/login2.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(loginFxmlPath));
            if (loader.getLocation() == null) {
                System.err.println("Sidebar CRITICAL: Cannot find login FXML: " + loginFxmlPath);
                showErrorAlert("Logout Error", "Cannot find login screen resource.");
                return;
            }
            Parent loginRoot = loader.load();
            Scene loginScene = new Scene(loginRoot);
            stage.setScene(loginScene);
            stage.setTitle("Đăng nhập");
            stage.centerOnScreen();
            stage.show();
            System.out.println("Navigated back to Login screen.");
        } catch (Exception e) {
            System.err.println("Sidebar: Error during logout navigation: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Lỗi Đăng Xuất", "Không thể quay về màn hình đăng nhập: " + e.getMessage());
        }
    }

    private void loadView(String fxmlPath) {
        if (mainBorderPane == null) {
            System.err.println("Sidebar: Main BorderPane is not set. Cannot load view: " + fxmlPath);
            showErrorAlert("Lỗi Giao Diện", "Không thể tải giao diện. Vui lòng thử lại sau.");
            return;
        }
        try {
            System.out.println("Loading view: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                System.err.println("Sidebar CRITICAL: Cannot find FXML to load into center: " + fxmlPath);
                showErrorAlert("Lỗi Tải Giao Diện", "Không tìm thấy file giao diện: " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1));
                return;
            }
            Parent view = loader.load();
            mainBorderPane.setCenter(view);
            System.out.println("View loaded into BorderPane center: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Sidebar: Failed to load view " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Lỗi Tải Giao Diện", "Không thể tải giao diện: " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1) + "\nLỗi: " + e.getMessage());
        }
    }

    public void updateApprovalCount(int count) {
        if (approvalCountLabel == null) return;
        if (count > 0) {
            approvalCountLabel.setText(String.valueOf(count));
            approvalCountLabel.setVisible(true);
        } else {
            approvalCountLabel.setVisible(false);
        }
        System.out.println("Sidebar: Approval count updated to " + count);
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            if (logoutButton != null && logoutButton.getScene() != null && logoutButton.getScene().getWindow() != null) {
                alert.initOwner(logoutButton.getScene().getWindow());
            }
            alert.showAndWait();
        });
    }
}