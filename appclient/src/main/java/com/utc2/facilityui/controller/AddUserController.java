package com.utc2.facilityui.controller;

import com.utc2.facilityui.model.UserCreationRequest; // Updated client-side DTO
import com.utc2.facilityui.response.UserResponse; // Server response DTO (client-side version)
import com.utc2.facilityui.service.UserServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField; // PasswordField removed
import javafx.stage.Stage;

import java.io.IOException;

public class AddUserController {

    @FXML
    private TextField userIdTextField;

    @FXML
    private TextField userNameTextField;

    @FXML
    private TextField fullNameTextField;

    @FXML
    private TextField emailTextField;

    // @FXML private PasswordField passwordField; // REMOVE THIS LINE

    @FXML
    private ComboBox<String> roleNameComboBox;

    @FXML
    private Button addButton;

    @FXML
    private Button cancelButton;

    private Runnable onUserAddedCallback;

    public void setOnUserAddedCallback(Runnable onUserAddedCallback) {
        this.onUserAddedCallback = onUserAddedCallback;
    }

    @FXML
    public void initialize() {
        roleNameComboBox.getItems().addAll("USER", "ADMIN", "FACILITY_MANAGER");
        roleNameComboBox.setValue("USER");
    }

    @FXML
    private void handleAddUserAction() {
        String userId = userIdTextField.getText().trim();
        String username = userNameTextField.getText().trim();
        String fullName = fullNameTextField.getText().trim(); // Server expects @NotBlank
        String email = emailTextField.getText().trim();
        // String password = passwordField.getText(); // REMOVE THIS LINE
        String selectedRole = roleNameComboBox.getValue();

        // === Input Validation ===
        if (userId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Mã số không được để trống.");
            userIdTextField.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Tên tài khoản không được để trống.");
            userNameTextField.requestFocus();
            return;
        }
        if (fullName.isEmpty()) { // Server expects fullName to be NotBlank
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Họ tên không được để trống.");
            fullNameTextField.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Email không được để trống.");
            emailTextField.requestFocus();
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Định dạng email không hợp lệ.");
            emailTextField.requestFocus();
            return;
        }
        // REMOVE password validation
        // if (password.isEmpty()) { ... }
        // if (password.length() < 6) { ... }

        if (selectedRole == null || selectedRole.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng chọn vai trò cho người dùng.");
            roleNameComboBox.requestFocus();
            return;
        }
        // === End Input Validation ===

        // Create DTO matching server expectations (no password, no avatar)
        UserCreationRequest newUserDto = new UserCreationRequest(
                userId, username, email, fullName, selectedRole
        );

        addButton.setDisable(true);
        cancelButton.setDisable(true);

        new Thread(() -> {
            try {
                // UserServices.createUser will serialize newUserDto using Gson.
                // Gson will only include fields present in the ClientUserCreationRequest class.
                UserResponse createdUser = UserServices.createUser(newUserDto);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Tài khoản '" + createdUser.getUsername() + "' đã được thêm thành công! Mật khẩu mặc định là mã số người dùng.");
                    if (onUserAddedCallback != null) {
                        onUserAddedCallback.run();
                    }
                    closeWindow();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Tạo Người Dùng", "Không thể thêm tài khoản:\n" + e.getMessage());
                });
            } finally {
                Platform.runLater(() -> {
                    addButton.setDisable(false);
                    cancelButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleCancelAction() {
        closeWindow();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}