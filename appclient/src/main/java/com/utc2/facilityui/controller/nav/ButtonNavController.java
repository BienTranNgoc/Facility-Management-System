package com.utc2.facilityui.controller.nav;

import com.utc2.facilityui.auth.TokenStorage;
import com.utc2.facilityui.model.ButtonNav; // Sử dụng model ButtonNav hiện tại của bạn

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node; // Import Node
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane; // Import Pane chung
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ButtonNavController {

    @FXML private AnchorPane btn; // Giả sử đây là root của buttonNav.fxml hoặc vùng có thể click
    @FXML public Button buttonNav; // Nút UI thực tế
    @FXML private ImageView imgButtonNav;

    // Không cần trường mainDisplayPaneTarget ở đây vì sẽ dùng lookup

    public void setData(ButtonNav bntNav) {
        if (bntNav == null) {
            System.err.println("ButtonNavController: setData được gọi với model ButtonNav null.");
            // Có thể ẩn nút hoặc đặt trạng thái mặc định nếu cần
            if (buttonNav != null) buttonNav.setVisible(false);
            return;
        }
        if (buttonNav != null) buttonNav.setVisible(true);


        if (imgButtonNav != null && bntNav.getImageSrc() != null && !bntNav.getImageSrc().isEmpty()) {
            try (InputStream imgStream = getClass().getResourceAsStream(bntNav.getImageSrc())) {
                if (imgStream != null) {
                    Image image = new Image(imgStream);
                    imgButtonNav.setImage(image);
                } else {
                    System.err.println("ButtonNavController: Không tìm thấy ảnh: " + bntNav.getImageSrc());
                    imgButtonNav.setImage(null); // Xóa ảnh cũ nếu không tìm thấy ảnh mới
                }
            } catch (IOException e) {
                System.err.println("ButtonNavController: Lỗi khi đọc ảnh: " + bntNav.getImageSrc() + " - " + e.getMessage());
                imgButtonNav.setImage(null);
            } catch (NullPointerException e) {
                System.err.println("ButtonNavController: getResourceAsStream trả về null cho: " + bntNav.getImageSrc());
                imgButtonNav.setImage(null);
            }
        } else if (imgButtonNav != null) {
            imgButtonNav.setImage(null); // Xóa ảnh nếu không có đường dẫn
        }

        if (buttonNav != null) {
            buttonNav.setText(bntNav.getName());
            // Gán sự kiện click
            buttonNav.setOnAction(event -> {
                if (bntNav.getName() != null && !bntNav.getName().trim().isEmpty()) {
                    loadPageByName(bntNav.getName());
                } else {
                    System.err.println("ButtonNavController: Tên ButtonNav là null hoặc rỗng, không thể xác định trang để tải.");
                    showErrorAlert("Lỗi Điều Hướng", "Nút không được cấu hình đúng tên trang.");
                }
            });
        }
    }

    private void loadPageByName(String pageName) {
        if (pageName == null || pageName.trim().isEmpty()) {
            showErrorAlert("Lỗi Điều Hướng", "Tên trang không hợp lệ để điều hướng.");
            return;
        }

        try {
            if ("Đăng xuất".equalsIgnoreCase(pageName.trim())) {
                handleLogout();
            } else {
                // Ưu tiên tìm "mainContentArea" (từ MainScreenController), nếu không thấy thì thử "mainCenter" (từ HomeController hoặc code cũ).
                String mainContentPaneFxId = "mainContentArea"; // Theo MainScreenController.java
                Node lookedUpNode = null;

                if (btn == null || btn.getScene() == null) {
                    showErrorAlert("Lỗi Giao Diện Nghiêm Trọng", "Không thể truy cập Scene hiện tại để thực hiện điều hướng.");
                    System.err.println("ButtonNavController: btn hoặc btn.getScene() là null.");
                    return;
                }
                lookedUpNode = btn.getScene().lookup("#" + mainContentPaneFxId);

                if (lookedUpNode == null) { // Thử fallback nếu mainContentArea không tìm thấy
                    System.out.println("ButtonNavController: Không tìm thấy fx:id='" + mainContentPaneFxId + "', thử tìm fx:id='mainCenter'.");
                    mainContentPaneFxId = "mainCenter"; // Fallback
                    lookedUpNode = btn.getScene().lookup("#" + mainContentPaneFxId);
                }

                if (lookedUpNode == null) {
                    System.err.println("ButtonNavController: Không tìm thấy Pane với fx:id='mainContentArea' hoặc 'mainCenter' trong Scene.");
                    showErrorAlert("Lỗi Giao Diện", "Không tìm thấy vùng hiển thị nội dung chính.\nVui lòng kiểm tra fx:id của Pane trung tâm trong FXML chính.");
                    return;
                }

                if (!(lookedUpNode instanceof Pane)) {
                    System.err.println("ButtonNavController: Node với fx:id='" + mainContentPaneFxId + "' không phải là một Pane. Nó là: " + lookedUpNode.getClass().getName());
                    showErrorAlert("Lỗi Cấu Trúc Giao Diện", "Vùng hiển thị nội dung chính (fx:id='" + mainContentPaneFxId + "') không phải là một container (Pane) hợp lệ.");
                    return;
                }

                Pane mainDisplayPane = (Pane) lookedUpNode; // Giờ đây là Pane, không phải AnchorPane cụ thể

                String fxmlFile = getPageFile(pageName.trim()); // Trim pageName
                if (fxmlFile == null || fxmlFile.trim().isEmpty()){
                    showErrorAlert("Lỗi Điều Hướng", "Không tìm thấy tệp FXML nào được ánh xạ cho trang: '" + pageName + "'.");
                    System.err.println("ButtonNavController: getPageFile trả về null hoặc rỗng cho pageName: '" + pageName + "'");
                    return;
                }

                String fullFxmlPath = "/com/utc2/facilityui/view/" + fxmlFile;
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fullFxmlPath));

                if (loader.getLocation() == null) {
                    System.err.println("ButtonNavController LỖI NGHIÊM TRỌNG: Không thể tìm thấy resource FXML: " + fullFxmlPath);
                    showErrorAlert("Lỗi Tải Trang", "Không tìm thấy tệp giao diện:\n" + fxmlFile);
                    return;
                }

                Node newPageNode = loader.load(); // newPageNode có thể là AnchorPane, VBox, HBox, etc.

                mainDisplayPane.getChildren().setAll(newPageNode); // Thay thế nội dung

                // Nếu Pane chính là AnchorPane, thì mới áp dụng các anchors
                if (mainDisplayPane instanceof AnchorPane) {
                    AnchorPane.setTopAnchor(newPageNode, 0.0);
                    AnchorPane.setBottomAnchor(newPageNode, 0.0);
                    AnchorPane.setLeftAnchor(newPageNode, 0.0);
                    AnchorPane.setRightAnchor(newPageNode, 0.0);
                }
                // Nếu mainDisplayPane là VBox, HBox, StackPane,... thì newPageNode sẽ được layout theo quy tắc của container đó.
                System.out.println("Đã tải trang '" + fxmlFile + "' vào Pane fx:id='" + mainContentPaneFxId + "'.");
            }
        } catch (IOException | NullPointerException | IllegalArgumentException e) { // Bắt các lỗi phổ biến
            e.printStackTrace();
            System.err.println("Lỗi khi xử lý trang '" + pageName + "': " + e.getClass().getSimpleName() + " - " + e.getMessage());
            showErrorAlert("Lỗi Điều Hướng", "Không thể tải trang '" + pageName + "'.\nChi tiết: " + e.getMessage());
        }
    }

    private void handleLogout() {
        try {
            TokenStorage.logout();
            System.out.println("ButtonNavController: Đã đăng xuất.");

            String loginFxmlPath = "/com/utc2/facilityui/view/login2.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(loginFxmlPath));
            if (loader.getLocation() == null) {
                System.err.println("ButtonNavController LỖI NGHIÊM TRỌNG: Không tìm thấy " + loginFxmlPath);
                showErrorAlert("Lỗi Đăng Xuất", "Không thể tìm tài nguyên màn hình đăng nhập.");
                return;
            }

            Parent loginRoot = loader.load(); // Không cần ép kiểu nếu không thao tác đặc thù
            Scene loginScene = new Scene(loginRoot);

            if (btn == null || btn.getScene() == null || btn.getScene().getWindow() == null) {
                System.err.println("ButtonNavController: Không thể lấy Stage hiện tại để chuyển về màn hình Login.");
                showErrorAlert("Lỗi Đăng Xuất", "Không thể hiển thị màn hình đăng nhập do không tìm thấy cửa sổ hiện tại.");
                return;
            }
            Stage stage = (Stage) btn.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Lỗi nghiêm trọng khi đăng xuất và chuyển về màn hình Login: " + e.getMessage());
            showErrorAlert("Lỗi Đăng Xuất", "Đã xảy ra sự cố khi cố gắng quay về màn hình đăng nhập.\n" + e.getMessage());
        }
    }

    private String getPageFile(String pageName) {
        // Cập nhật đường dẫn cho resetpassword.fxml nếu nó nằm trong thư mục con auth
        // Giữ nguyên logic mapping của bạn, đảm bảo tên file và đường dẫn chính xác.
        return switch (pageName.toLowerCase()) {
            case "phòng" -> "rooms.fxml"; // Trong /com/utc2/facilityui/view/
            case "thông báo" -> "myNotification.fxml"; // Trong /com/utc2/facilityui/view/
            case "đặt phòng của tôi" -> "myBookings.fxml"; // Trong /com/utc2/facilityui/view/
            case "đặt lại mật khẩu" -> "resetpassword.fxml";
            case "quản lý thiết bị" -> "manageDevice.fxml"; // Trong /com/utc2/facilityui/view/
            case "yêu cầu phê duyệt"-> "approvalrequests.fxml"; // Trong /com/utc2/facilityui/view/
            case "yêu cầu quá hạn"-> "overdueRequest.fxml"; // Trong /com/utc2/facilityui/view/
            case "quản lý cơ sở vật chất" -> "manageFacility.fxml";
            case "quản lý đặt phòng" -> "manageBookings.fxml"; // (tên này dùng trong SidebarMenuController)
            case "bảo trì"-> "maintenance.fxml";
            default -> {
                System.err.println("ButtonNavController - getPageFile: Không có FXML nào được ánh xạ cho tên trang: '" + pageName + "'. Trả về trang chủ mặc định 'home.fxml'.");
                yield "home.fxml"; // Trang mặc định nếu không khớp
            }
        };
    }

    private void showErrorAlert(String title, String content) {
        // Đảm bảo Alert được hiển thị trên luồng UI
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> displayAlertInternal(title, content));
        } else {
            displayAlertInternal(title, content);
        }
    }

    private void displayAlertInternal(String title, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // Cố gắng lấy owner window một cách an toàn
        try {
            if (btn != null && btn.getScene() != null && btn.getScene().getWindow() != null) {
                alert.initOwner(btn.getScene().getWindow());
            }
        } catch (Exception e) {
            System.err.println("ButtonNavController: Không thể đặt owner cho Alert - " + e.getMessage());
        }
        alert.showAndWait();
    }
}