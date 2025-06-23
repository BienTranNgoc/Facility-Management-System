// Trong file: com/utc2/facilityui/controller/room/RequestMaintenanceController.java
package com.utc2.facilityui.controller.room;

import com.utc2.facilityui.controller.BaseReportController;
import com.utc2.facilityui.model.MaintenanceRequestClient;
import com.utc2.facilityui.model.Room;
import com.utc2.facilityui.service.MaintenanceApiService;   // Import Service client
import com.utc2.facilityui.response.MaintenanceResponse; // Import Response client

import javafx.application.Platform; // Import Platform for UI updates from non-UI thread
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException; // Import IOException
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RequestMaintenanceController extends BaseReportController {

    @FXML
    private Label name;

    @FXML
    private ComboBox<ReportableItem> defaultEquipments;

    @FXML
    private TextArea description;

    @FXML
    private Button bntAdd;

    @FXML
    private Button bntCancel;

    private Room currentRoom;
    private MaintenanceApiService maintenanceService; // Khai báo service

    public static class ReportableItem {
        // ... (Nội dung lớp ReportableItem giữ nguyên như trước) ...
        private final String displayText;
        private final String id;
        private final String type;

        public ReportableItem(String displayText, String id, String type) {
            this.displayText = displayText;
            this.id = id;
            this.type = type;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getDisplayText() { return displayText; }

        @Override
        public String toString() { return displayText; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReportableItem that = (ReportableItem) o;
            return Objects.equals(displayText, that.displayText) &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() { return Objects.hash(displayText, id, type); }
    }

    public void setRoomData(Room roomData) {
        // ... (Giữ nguyên như trước) ...
        this.currentRoom = roomData;
        if (this.name != null) {
            if (roomData != null && roomData.getName() != null) {
                this.name.setText("Báo cố sự cố cho phòng: " + roomData.getName());
            } else {
                this.name.setText("Báo cố sự cố cho phòng: Không xác định");
            }
        }
        populateEquipmentsComboBox();
    }

    private void populateEquipmentsComboBox() {
        // ... (Giữ nguyên như trước) ...
        if (defaultEquipments == null) {
            System.err.println("Lỗi: ComboBox 'defaultEquipments' chưa được khởi tạo (null).");
            return;
        }

        defaultEquipments.getItems().clear();

        ReportableItem placeholder = new ReportableItem("-- Chọn --", "NONE", "PLACEHOLDER");
        defaultEquipments.getItems().add(placeholder);
        defaultEquipments.getItems().add(new ReportableItem("Vấn đề chung của phòng", "GENERAL_ROOM_ISSUE", "GENERAL_ISSUE"));
        defaultEquipments.getItems().add(new ReportableItem("--- Thiết bị mặc định ---", "SEPARATOR_DUMMY_ID", "SEPARATOR"));

        if (currentRoom != null && currentRoom.getDefaultEquipments() != null) {
            List<Object> eqs = currentRoom.getDefaultEquipments();
            if (eqs.isEmpty()) {
                defaultEquipments.getItems().add(new ReportableItem("(Không có thiết bị mặc định nào)", "NO_EQUIPMENT", "INFO"));
            } else {
                for (Object eqObj : eqs) {
                    if (eqObj instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) eqObj;
                        String equipmentId = map.get("id") != null ? map.get("id").toString() : "N/A_ID";
                        String modelName = map.get("modelName") != null ? map.get("modelName").toString() : "Không tên";
                        String status = map.get("status") != null ? map.get("status").toString() : "N/A_STATUS";
                        String displayId = equipmentId.length() > 10 ? equipmentId.substring(0,4) + "..." + equipmentId.substring(equipmentId.length()-4) : equipmentId;

                        String displayText = String.format("%s (%s) - [%s]", modelName, displayId, status);
                        defaultEquipments.getItems().add(new ReportableItem(displayText, equipmentId, "EQUIPMENT"));
                    }
                }
            }
        } else {
            defaultEquipments.getItems().add(new ReportableItem("(Không có thông tin thiết bị)", "NO_EQUIPMENT_DATA", "INFO"));
        }
        defaultEquipments.getSelectionModel().select(placeholder);
    }

    @FXML
    public void initialize() {
        maintenanceService = new MaintenanceApiService(); // Khởi tạo service
        // ... (Phần còn lại của initialize giữ nguyên như trước) ...
        if (description != null) {
            if (description.getText() == null || description.getText().isEmpty()){
                description.setText("Description");
            }
            description.setOnMouseClicked(this::handleTextAreaClick);
            description.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal && description.getText().isEmpty()) {
                    description.setText("Description");
                }
            });
        } else {
            System.err.println("Lỗi: TextArea 'description' chưa được inject.");
        }

        if (bntAdd != null) {
            bntAdd.setOnAction(e -> handleAdd());
        } else {
            System.err.println("Lỗi: Button 'bntAdd' chưa được inject.");
        }

        if (bntCancel != null) {
            bntCancel.setOnAction(e -> closeWindow());
        } else {
            System.out.println("Thông báo: Button 'bntCancel' không được inject hoặc không có trong FXML.");
        }
        populateEquipmentsComboBox();
    }

    private void handleTextAreaClick(MouseEvent event) {
        // ... (Giữ nguyên như trước) ...
        if (description != null && description.getText().equals("Description")) {
            description.setText("");
        }
    }

    @Override
    protected void handleAdd() {
        ReportableItem selectedItem = defaultEquipments.getSelectionModel().getSelectedItem();
        String reportContent = description.getText().trim();

        // --- Validations (giữ nguyên) ---
        if (selectedItem == null || "PLACEHOLDER".equals(selectedItem.getType()) || "NONE".equals(selectedItem.getId())) {
            showAlert("Thiếu thông tin", "Vui lòng chọn một thiết bị hoặc vấn đề cụ thể.", Alert.AlertType.WARNING);
            return;
        }
        if ("SEPARATOR".equals(selectedItem.getType()) || "INFO".equals(selectedItem.getType())) {
            showAlert("Lựa chọn không hợp lệ", "Mục bạn chọn không phải là đối tượng có thể báo cáo.", Alert.AlertType.WARNING);
            return;
        }
        if (reportContent.isEmpty() || reportContent.equals("Description")) {
            showAlert("Thiếu thông tin", "Vui lòng nhập mô tả chi tiết cho sự cố.", Alert.AlertType.WARNING);
            return;
        }
        // --- Kết thúc Validations ---

        MaintenanceRequestClient requestDto = new MaintenanceRequestClient();
        requestDto.setDescription(reportContent);

        if ("GENERAL_ISSUE".equals(selectedItem.getType())) {
            if (currentRoom != null && currentRoom.getId() != null) {
                requestDto.setRoomId(currentRoom.getId());
                requestDto.setItemId(null); // Đảm bảo itemId là null cho vấn đề chung của phòng
            } else {
                showAlert("Lỗi dữ liệu", "Không tìm thấy thông tin phòng hiện tại.", Alert.AlertType.ERROR);
                return;
            }
        } else if ("EQUIPMENT".equals(selectedItem.getType())) {
            requestDto.setItemId(selectedItem.getId());
            requestDto.setRoomId(null); // Đảm bảo roomId là null khi báo cáo cho thiết bị cụ thể
            // Server sẽ tự suy ra phòng từ thiết bị nếu cần
        } else {
            showAlert("Lỗi", "Loại mục báo cáo không xác định.", Alert.AlertType.ERROR);
            return;
        }

        // Vô hiệu hóa nút Gửi để tránh click nhiều lần
        if (bntAdd != null) bntAdd.setDisable(true);


        // Thực hiện gọi API trong một luồng riêng để không làm treo UI
        new Thread(() -> {
            try {
                // Gọi service để gửi yêu cầu bảo trì
                MaintenanceResponse serverResponse = maintenanceService.submitMaintenanceRequest(requestDto); //

                // Cập nhật UI trên luồng JavaFX Application Thread
                Platform.runLater(() -> {
                    if (bntAdd != null) bntAdd.setDisable(false); // Kích hoạt lại nút
                    showAlert("Thành công", "Yêu cầu bảo trì đã được gửi thành công!\nMã yêu cầu: " + serverResponse.getId(), Alert.AlertType.INFORMATION);
                    closeWindow();
                });

            } catch (IOException e) {
                // Cập nhật UI trên luồng JavaFX Application Thread
                Platform.runLater(() -> {
                    if (bntAdd != null) bntAdd.setDisable(false); // Kích hoạt lại nút
                    showAlert("Gửi thất bại", "Không thể gửi yêu cầu bảo trì: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace(); // In lỗi ra console để debug
                });
            } catch (Exception e) { // Bắt các lỗi không mong muốn khác
                Platform.runLater(() -> {
                    if (bntAdd != null) bntAdd.setDisable(false);
                    showAlert("Lỗi không mong muốn", "Đã xảy ra lỗi không mong muốn: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        // ... (Giữ nguyên như trước) ...
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        // ... (Giữ nguyên như trước) ...
        Stage stage = null;
        if (bntAdd != null && bntAdd.getScene() != null && bntAdd.getScene().getWindow() instanceof Stage) {
            stage = (Stage) bntAdd.getScene().getWindow();
        } else if (description != null && description.getScene() != null && description.getScene().getWindow() instanceof Stage) {
            stage = (Stage) description.getScene().getWindow();
        } else if (defaultEquipments != null && defaultEquipments.getScene() != null && defaultEquipments.getScene().getWindow() instanceof Stage) {
            stage = (Stage) defaultEquipments.getScene().getWindow();
        }

        if (stage != null) {
            stage.close();
        } else {
            System.err.println("Không thể đóng cửa sổ: không tìm thấy Stage.");
        }
    }

    public Label getNameLabel() { return name; }
    public Button getBntAdd() { return bntAdd; }
    public Button getBntCancel() { return bntCancel; }
}