package com.utc2.facilityui.controller;

import com.utc2.facilityui.model.UserUpdateRequest;
import com.utc2.facilityui.model.User; // Client's User model
import com.utc2.facilityui.response.UserResponse; // Client's UserResponse DTO (from server)
import com.utc2.facilityui.service.UserServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class EditUserController {

    @FXML private Label userid;         // To display user's business key (userId)
    @FXML private TextField name;       // Bound to fx:id="name", for username
    @FXML private TextField fullName;   // Bound to fx:id="fullName"
    @FXML private ComboBox<String> roleName; // Bound to fx:id="roleName"

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private User currentUserForDisplay; // Keep the original User object for display properties if needed
    private String originalBusinessUserId; // Store the UserID (business key) for the API update call

    private Runnable onUserUpdatedCallback;

    public void setOnUserUpdatedCallback(Runnable onUserUpdatedCallback) {
        this.onUserUpdatedCallback = onUserUpdatedCallback;
    }

    @FXML
    public void initialize() {
        roleName.getItems().addAll("USER", "ADMIN", "FACILITY_MANAGER");
    }

    public void setUserData(User userToEdit) {
        this.currentUserForDisplay = userToEdit;
        // IMPORTANT: Use the raw, unformatted business key (userId) for API calls
        this.originalBusinessUserId = userToEdit.getRawUserId(); // Assuming User.java has getRawUserId()

        // Display non-editable identifiers.
        // userToEdit.getId() might be the formatted UUID, userToEdit.getRawUserId() is the business key.
        userid.setText("Mã người dùng: " + userToEdit.getRawUserId() + " (ID: " + userToEdit.getRawId() + ")");

        // Populate fields with raw, unformatted data for editing.
        // This ensures that if "N/A" or other placeholders were used for display,
        // they don't end up in the edit fields.
        name.setText(userToEdit.getUsername());     // Assuming User.java has getRawUsername()
        fullName.setText(userToEdit.getFullName()); // Assuming User.java has getRawFullName()

        String currentRole = userToEdit.getRoleName(); // Assuming User.java has getRawRoleName()
        if (currentRole != null && !currentRole.isEmpty() && !currentRole.equals("-") /* Check against your text placeholder */) {
            // Check if the role from user data is a valid choice in ComboBox
            if (roleName.getItems().contains(currentRole)) {
                roleName.setValue(currentRole);
            } else {
                // Handle case where user's current role is not in the predefined list
                // For example, add it to the list, or default, or show an error.
                // For now, let's try to set it, ComboBox might allow it if editable, or clear selection.
                roleName.setValue(currentRole);
                if (!roleName.getItems().contains(currentRole)) {
                    // If still not in items (e.g. ComboBox not editable and role was invalid)
                    // then clear or set a default to avoid issues.
                    roleName.setValue(null); // Or a default like "USER"
                    System.err.println("EditUserController: User's current role '" + currentRole + "' is not in the ComboBox list.");
                }
            }
        } else {
            roleName.setValue("USER"); // Default if no valid role is found
        }
    }

    @FXML
    private void handleSaveAction() {
        if (originalBusinessUserId == null || originalBusinessUserId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Dữ Liệu", "Không có thông tin mã người dùng (UserID) để cập nhật.");
            return;
        }

        String updatedUsername = name.getText().trim();
        String updatedFullName = fullName.getText().trim(); // Server DTO allows empty, but server entity might not. Server UserUpdateRequest has @Size(min=1) for fullName.
        String updatedRoleName = roleName.getValue();

        // --- Input Validation ---
        if (updatedUsername.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Tên tài khoản không được để trống.");
            name.requestFocus();
            return;
        }
        // According to server DTO UserUpdateRequest, fullName must be @Size(min=1)
        if (updatedFullName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Họ và tên không được để trống.");
            fullName.requestFocus();
            return;
        }
        if (updatedRoleName == null || updatedRoleName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng chọn vai trò.");
            roleName.requestFocus();
            return;
        }
        // --- End Input Validation ---

        UserUpdateRequest updateRequest = new UserUpdateRequest(updatedUsername, updatedFullName, updatedRoleName);

        saveButton.setDisable(true);
        cancelButton.setDisable(true);

        new Thread(() -> {
            try {
                // Call service with originalBusinessUserId (the stable, non-editable user identifier)
                UserResponse updatedUserResponse = UserServices.updateUser(originalBusinessUserId, updateRequest);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Thông tin tài khoản '" + updatedUserResponse.getUsername() + "' đã được cập nhật.");
                    if (onUserUpdatedCallback != null) {
                        onUserUpdatedCallback.run();
                    }
                    closeWindow();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Cập Nhật", "Không thể cập nhật tài khoản:\n" + e.getMessage());
                    e.printStackTrace(); // For detailed error in console
                });
            } finally {
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
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