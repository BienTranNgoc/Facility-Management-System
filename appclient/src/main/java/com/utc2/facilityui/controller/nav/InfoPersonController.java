package com.utc2.facilityui.controller.nav;

import com.utc2.facilityui.model.ButtonNav;
import com.utc2.facilityui.model.User;
import com.utc2.facilityui.service.UserServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class InfoPersonController implements Initializable {

    @FXML private VBox putbtn; // Container cho các nút điều hướng
    @FXML private ImageView avatar;
    @FXML private Text namePerson;
    @FXML private Text idPerson;

    private static final String DEFAULT_AVATAR_PATH = "/com/utc2/facilityui/images/man.png";
    private static final String BUTTON_NAV_FXML_PATH = "/com/utc2/facilityui/component/buttonNav.fxml";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing InfoPersonController...");

        if (avatar != null) {
            double fitWidth = avatar.getFitWidth();
            double fitHeight = avatar.getFitHeight();

            if (fitWidth <= 0) fitWidth = 70.0; // Kích thước vuông mặc định nếu FXML không set
            if (fitHeight <= 0) fitHeight = 70.0;

            // Ép ImageView thành hình vuông nếu kích thước không bằng nhau, lấy cạnh nhỏ hơn
            // Điều này quan trọng để clip tròn luôn đẹp. Đảm bảo FXML cũng đặt kích thước vuông.
            if (Math.abs(fitWidth - fitHeight) > 0.1) { // Cho phép sai số nhỏ
                System.out.println("InfoPersonController: Avatar ImageView in FXML is not square. Adjusting for clip. Original: " + fitWidth + "x" + fitHeight);
                fitWidth = Math.min(fitWidth, fitHeight);
                fitHeight = fitWidth;
                avatar.setFitWidth(fitWidth);
                avatar.setFitHeight(fitHeight);
            }

            double radius = fitWidth / 2.0; // Vì đã đảm bảo fitWidth (hoặc kích thước đã điều chỉnh) là cạnh của hình vuông
            double centerX = fitWidth / 2.0;
            double centerY = fitHeight / 2.0; // Tâm Y mặc định

            // Điều chỉnh để tập trung khuôn mặt (dịch tâm Y của clip lên trên)
            // Bạn có thể thử nghiệm với giá trị verticalOffsetFactor này (0.0 đến ~0.3)
            double verticalOffsetFactor = 0.10; // Ví dụ: dịch tâm clip lên 15% bán kính
            centerY = centerY - (radius * verticalOffsetFactor);

            Circle clip = new Circle(centerX, centerY, radius);
            avatar.setClip(clip);
            System.out.println("InfoPersonController: Avatar clip set. Effective size=" + fitWidth +
                    ", Clip CenterX=" + centerX + ", Adjusted CenterY=" + centerY +
                    ", Radius=" + radius);
        } else {
            System.err.println("InfoPersonController: ImageView 'avatar' is null. Check FXML fx:id.");
        }

        setUIToLoadingState();
        loadUserInfo(); // loadUserInfo sẽ gọi loadNavigationButtons sau khi có roleName
    }

    private void setUIToLoadingState() {
        if (namePerson != null) namePerson.setText("Đang tải...");
        if (idPerson != null) idPerson.setText("ID: Đang tải...");
        if (putbtn != null) putbtn.getChildren().clear(); // Xóa các nút cũ
        updateAvatarImage(null); // Tải avatar mặc định (sẽ được clip nếu clip đã set)
    }

    private void loadUserInfo() {
        System.out.println("InfoPersonController: Starting to load user info task...");
        Task<User> loadUserTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return UserServices.getMyInfo();
            }
        };

        loadUserTask.setOnSucceeded(event -> {
            User user = loadUserTask.getValue();
            System.out.println("InfoPersonController: User info task succeeded.");
            Platform.runLater(() -> {
                if (user != null && user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
                    System.out.println("InfoPersonController: Updating UI with user: " + user.getUsername() + ", Role: " + user.getRoleName() + ", FullName: " + user.getUsername());
                    updateUserInfoUI(user);
                    loadNavigationButtons(user.getRoleName()); // Tải nút dựa trên vai trò
                } else {
                    System.out.println("InfoPersonController: User object or critical fields (like userId or roleName) are null or empty.");
                    setUIToDefaultOrError("Không có dữ liệu", "ID: Lỗi", true);
                    loadNavigationButtons(null); // Tải nút mặc định khi lỗi user
                }
            });
        });

        loadUserTask.setOnFailed(event -> {
            Throwable exception = loadUserTask.getException();
            System.err.println("InfoPersonController: User info task failed: " + exception.getMessage());
            exception.printStackTrace();
            Platform.runLater(() -> {
                setUIToDefaultOrError("Lỗi tải dữ liệu", "ID: Lỗi", true);
                String errorType = (exception instanceof java.net.ConnectException) ? "Không thể kết nối server." :
                        (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("parse")) ? "Dữ liệu không hợp lệ từ server." :
                                "Lỗi mạng hoặc hệ thống.";
                showErrorAlert("Lỗi Tải Thông Tin Người Dùng", errorType + "\nChi tiết: " + exception.getMessage());
                loadNavigationButtons(null); // Tải nút mặc định khi lỗi user
            });
        });

        System.out.println("InfoPersonController: Starting user info loading thread...");
        Thread backgroundThread = new Thread(loadUserTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void updateUserInfoUI(User user) {
        String displayName = (user.getUsername() != null && !user.getUsername().trim().isEmpty()) ? user.getUsername() :
                (user.getUsername() != null && !user.getUsername().trim().isEmpty() ? user.getUsername() : "N/A");
        if (namePerson != null) namePerson.setText(displayName);

        String displayId = user.getUserId() != null ? user.getUserId() : "N/A";
        if (idPerson != null) idPerson.setText("ID: " + displayId);

        String avatarIdentifier = user.getAvatar();
        System.out.println("InfoPersonController: Định danh avatar nhận được từ User object: " + avatarIdentifier);
        updateAvatarImage(avatarIdentifier);
    }

    private void updateAvatarImage(String avatarIdentifier) {
        if (avatar == null) {
            System.err.println("InfoPersonController: ImageView 'avatar' is null in updateAvatarImage.");
            return;
        }
        Image imageToSet = null;
        if (avatarIdentifier != null && !avatarIdentifier.trim().isEmpty()) {
            String path = avatarIdentifier.trim();
            if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
                System.out.println("InfoPersonController: Avatar is an HTTP URL: " + path + ". Loading from network.");
                loadAvatarFromHttpURL_Internal(path); // Phương thức này sẽ tự set image hoặc default
                return; // Việc set image sẽ được xử lý bởi loadAvatarFromHttpURL_Internal
            } else {
                String classpathResourcePath = path.startsWith("/") ? path : "/com/utc2/facilityui/images/" + path;
                System.out.println("InfoPersonController: Attempting to load avatar from classpath: " + classpathResourcePath);
                imageToSet = loadImageFromClasspath(classpathResourcePath);
                if (imageToSet != null) {
                    System.out.println("InfoPersonController: Successfully loaded avatar from classpath: " + classpathResourcePath);
                } else {
                    System.err.println("InfoPersonController: Failed to load avatar from classpath: " + classpathResourcePath);
                }
            }
        }

        if (imageToSet == null) { // Nếu không tải được từ avatarIdentifier (hoặc nó null/empty)
            System.out.println("InfoPersonController: Avatar from API/classpath failed or not provided. Loading default avatar.");
            imageToSet = loadImageFromClasspath(DEFAULT_AVATAR_PATH);
            if (imageToSet != null) {
                System.out.println("InfoPersonController: Successfully loaded default avatar: " + DEFAULT_AVATAR_PATH);
            } else {
                System.err.println("InfoPersonController CRITICAL: Failed to load default avatar from: " + DEFAULT_AVATAR_PATH);
            }
        }

        avatar.setImage(imageToSet); // Set ảnh (có thể là null nếu cả hai đều thất bại)
        if (imageToSet == null) {
            System.err.println("InfoPersonController: Could not load any avatar (API or default). ImageView will be empty.");
        }
    }

    private Image loadImageFromClasspath(String classpathPath) {
        if (classpathPath == null || classpathPath.trim().isEmpty()) {
            System.err.println("InfoPersonController: Classpath for image is null or empty.");
            return null;
        }
        try (InputStream imageStream = getClass().getResourceAsStream(classpathPath)) {
            if (imageStream != null) {
                Image image = new Image(imageStream);
                if (image.isError()) {
                    System.err.println("InfoPersonController: Error creating Image object from classpath: " + classpathPath + (image.getException() != null ? " - " + image.getException().getMessage() : ""));
                    return null;
                }
                return image;
            } else {
                System.err.println("InfoPersonController: Cannot find resource on classpath: " + classpathPath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("InfoPersonController: Exception while loading image from classpath: " + classpathPath + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void loadAvatarFromHttpURL_Internal(String avatarUrl) {
        if (avatar == null) return;
        try {
            Image networkImage = new Image(avatarUrl, true); // true để tải nền
            avatar.setImage(networkImage); // Set ngay, Image sẽ tự cập nhật

            networkImage.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) {
                    System.err.println("InfoPersonController: Error loading avatar from HTTP URL: " + avatarUrl +
                            (networkImage.getException() != null ? ". Reason: " + networkImage.getException().getMessage() : ". Unknown error."));
                    Platform.runLater(() -> updateAvatarImage(null)); // Thử tải lại default avatar nếu lỗi
                }
            });

            if (networkImage.isError()) { // Kiểm tra lỗi tức thì
                System.err.println("InfoPersonController: Immediate error after creating Image from HTTP URL: " + avatarUrl +
                        (networkImage.getException() != null ? ". Reason: " + networkImage.getException().getMessage() : ". Unknown error."));
                Platform.runLater(() -> updateAvatarImage(null)); // Gọi updateAvatarImage(null) để nó load default
            }
        } catch (IllegalArgumentException e) {
            System.err.println("InfoPersonController: Invalid HTTP URL format for avatar: " + avatarUrl + " - " + e.getMessage());
            Platform.runLater(() -> updateAvatarImage(null));
        } catch (Exception e) {
            System.err.println("InfoPersonController: Unexpected error creating Image from HTTP URL: " + avatarUrl + " - " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> updateAvatarImage(null));
        }
    }

    private void setUIToDefaultOrError(String nameText, String idText, boolean tryLoadDefaultAvatar) {
        if (namePerson != null) namePerson.setText(nameText);
        if (idPerson != null) idPerson.setText(idText);
        if (tryLoadDefaultAvatar) {
            updateAvatarImage(null); // Sẽ tự động tải default avatar
        }
        System.out.println("InfoPersonController: UI set to default/error state - Name: " + nameText + ", ID: " + idText);
    }

    private void loadNavigationButtons(String roleName) {
        System.out.println("InfoPersonController: Loading navigation buttons for role: " + roleName);
        List<ButtonNav> buttonsToDisplay = createNavigationButtonModels(roleName);

        if (putbtn == null) {
            System.err.println("InfoPersonController CRITICAL: VBox 'putbtn' is null. Cannot add navigation buttons.");
            return;
        }
        putbtn.getChildren().clear();
        try {
            for (ButtonNav btnModel : buttonsToDisplay) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BUTTON_NAV_FXML_PATH));
                if (fxmlLoader.getLocation() == null) {
                    System.err.println("InfoPersonController CRITICAL: Cannot find FXML for navigation button: " + BUTTON_NAV_FXML_PATH);
                    continue;
                }
                AnchorPane buttonPane = fxmlLoader.load();
                ButtonNavController controller = fxmlLoader.getController();
                if (controller != null) {
                    controller.setData(btnModel);
                    putbtn.getChildren().add(buttonPane);
                } else {
                    System.err.println("InfoPersonController CRITICAL: ButtonNavController is null for FXML: " + BUTTON_NAV_FXML_PATH);
                }
            }
            System.out.println("InfoPersonController: Navigation buttons loaded for role " + roleName + " count: " + buttonsToDisplay.size());
        } catch (Exception e) {
            System.err.println("InfoPersonController: Unexpected error during navigation button loading: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Lỗi Giao Diện Nghiêm Trọng", "Không thể tải các thành phần điều hướng.");
        }
    }

    private List<ButtonNav> createNavigationButtonModels(String roleName) {
        List<ButtonNav> ls = new ArrayList<>();

        if (roleName != null) {
            switch (roleName.toUpperCase()) {
                case "FACILITY_MANAGER":
                    ls.add(new ButtonNav("Phòng", "/com/utc2/facilityui/images/medal.png"));
                    ls.add(new ButtonNav("Quản lý thiết bị", "/com/utc2/facilityui/images/device.png"));
                    ls.add(new ButtonNav("Yêu cầu phê duyệt", "/com/utc2/facilityui/images/stamp.png"));
                    ls.add(new ButtonNav("Yêu cầu quá hạn", "/com/utc2/facilityui/images/cancel.png"));
                    ls.add(new ButtonNav("Đặt lại mật khẩu", "/com/utc2/facilityui/images/password.png"));
                    ls.add(new ButtonNav("Đăng xuất", "/com/utc2/facilityui/images/logout.png"));
                    break;
                case "TECHNICIAN": // Technician có thể có quyền hạn chế hơn
                    ls.add(new ButtonNav("Phòng", "/com/utc2/facilityui/images/medal.png")); // Để xem thông tin phòng/thiết bị được giao
                    ls.add(new ButtonNav("Bảo trì","/com/utc2/facilityui/images/icon_maitenance.png"));
                    ls.add(new ButtonNav("Đặt phòng của tôi", "/com/utc2/facilityui/images/calendar.png")); // Nếu họ cần đặt thiết bị/phòng cho công việc
                    ls.add(new ButtonNav("Thông báo", "/com/utc2/facilityui/images/notificationWhite.png"));
                    ls.add(new ButtonNav("Đặt lại mật khẩu", "/com/utc2/facilityui/images/password.png"));
                    ls.add(new ButtonNav("Đăng xuất", "/com/utc2/facilityui/images/logout.png"));
                    break;
                case "USER":
                default: // Mặc định cho USER và các role không xác định
                    ls.add(new ButtonNav("Phòng", "/com/utc2/facilityui/images/medal-outline-icon.png"));
                    ls.add(new ButtonNav("Đặt phòng của tôi", "/com/utc2/facilityui/images/List-Check-icon.png"));
                    ls.add(new ButtonNav("Thông báo", "/com/utc2/facilityui/images/notification.png"));
                    ls.add(new ButtonNav("Đặt lại mật khẩu", "/com/utc2/facilityui/images/rotation-lock.png"));
                    ls.add(new ButtonNav("Đăng xuất", "/com/utc2/facilityui/images/logout-icon.png"));
                    break;
            }
        } else {
            // Fallback nếu roleName là null (lỗi tải thông tin user)
            ls.add(new ButtonNav("Phòng", "/com/utc2/facilityui/images/medal-outline-icon.png"));
            System.out.println("InfoPersonController: roleName is null, loading default minimal navigation buttons.");
        }



        return ls;
    }

    private void showErrorAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            if (putbtn != null && putbtn.getScene() != null && putbtn.getScene().getWindow() != null) {
                alert.initOwner(putbtn.getScene().getWindow());
            }
            alert.showAndWait();
        });
    }
}