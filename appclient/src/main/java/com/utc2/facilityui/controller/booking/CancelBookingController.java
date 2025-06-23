package com.utc2.facilityui.controller.booking;

import com.utc2.facilityui.response.BookingResponse;
import com.utc2.facilityui.service.BookingService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class CancelBookingController {

    @FXML
    private Label name; // This is fx:id="name" in your FXML

    @FXML
    private TextArea reasonTextArea; // This is fx:id="reasonTextArea"

    @FXML
    private Button bntAdd; // This is fx:id="bntAdd" (Từ chối yêu cầu)

    @FXML
    private Button bntCancel; // This is fx:id="bntCancel" (Hủy bỏ)

    private BookingResponse currentBooking;
    private BookingService bookingService;

    // Method to receive data from ManageBookingsController
    public void initData(BookingResponse booking, BookingService bookingService) {
        this.currentBooking = booking;
        this.bookingService = bookingService;

        if (booking != null) {
            // Update the label text as per your FXML content
            // "Bạn có chắc chắn muốn từ chối yêu cầu đặt phòng nameRoom của userName?"
            String roomName = booking.getRoomName() != null ? booking.getRoomName() : "[Không rõ tên phòng]";
            String userName = booking.getUserName() != null ? booking.getUserName() : "[Không rõ người dùng]";
            name.setText("Bạn có chắc chắn muốn từ chối yêu cầu đặt phòng " + roomName + " của " + userName + "?");
            name.setWrapText(true); // Ensure text wraps
        } else {
            name.setText("Không có thông tin đặt phòng.");
        }
    }

    @FXML
    void initialize() {
        // Action for "Từ chối yêu cầu" button
        bntAdd.setOnAction(event -> handleRejectBooking());

        // Action for "Hủy bỏ" button
        bntCancel.setOnAction(event -> closeDialog());
    }

    private void handleRejectBooking() {
        if (currentBooking == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có thông tin đặt phòng để từ chối.");
            return;
        }

        String reason = reasonTextArea.getText();
        if (reason == null || reason.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập lý do từ chối.");
            reasonTextArea.requestFocus();
            return;
        }

        // Disable button to prevent multiple clicks
        bntAdd.setDisable(true);
        bntCancel.setDisable(true);

        new Thread(() -> {
            try {
                //TODO: Ensure bookingService.rejectBooking method exists and handles the API call
                // You might need to adjust parameters if your service method is different
                bookingService.rejectBooking(currentBooking.getId(), reason);

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Yêu cầu đặt phòng đã được từ chối.");
                    closeDialog();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể từ chối yêu cầu đặt phòng: " + e.getMessage());
                    // Re-enable buttons on error
                    bntAdd.setDisable(false);
                    bntCancel.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void closeDialog() {
        Stage stage = (Stage) bntCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Set owner if possible for better modality behavior
        Window owner = (bntCancel != null && bntCancel.getScene() != null) ? bntCancel.getScene().getWindow() : null;
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }
}