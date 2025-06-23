package com.utc2.facilityui.controller.equipment;

import com.utc2.facilityui.model.Equipment;
import com.utc2.facilityui.service.EquipmentService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.net.URL;
import java.util.ResourceBundle;

public class EditEquipmentController implements Initializable {

    @FXML private TextField txtModelName;
    @FXML private TextField txtTypeName;
    @FXML private TextField txtSerial;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private TextField txtDefaultRoomName;
    @FXML private TextField txtNotes;
    @FXML private TextField txtImgModel;
    @FXML private TextField txtDescription;


    private Equipment currentEquipment;

    public void initialize(URL location, ResourceBundle resources) {
        // Đặt các giá trị trạng thái vào ComboBox
        cmbStatus.setItems(FXCollections.observableArrayList(
                "AVAILABLE", "BROKEN", "DISPOSED", "UNDER_MAINTENANCE"
        ));
    }

    public void setEquipmentToEdit(Equipment equipment) {
        this.currentEquipment = equipment;

        txtModelName.setText(equipment.getModelName());
        txtSerial.setText(equipment.getSerialNumber());
        txtNotes.setText(equipment.getNotes());
        cmbStatus.setValue(equipment.getStatus());
        txtDefaultRoomName.setText(equipment.getDefaultRoomName());
        txtTypeName.setText(equipment.getTypeName());
        txtImgModel.setText(equipment.getImgModel());
        txtDescription.setText(equipment.getDescription());
    }

    private boolean updateDescriptionByEquipmentId(String equipmentId, String newDescription) {
        String sql = """
        UPDATE equipment_models em
        JOIN equipment_item ei ON em.id = ei.model_id
        SET em.description = ?
        WHERE ei.id = ?
    """;

        String url = "jdbc:mysql://localhost:3306/facility";
        String user = "root";
        String password = "Tranbien2809@";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, newDescription);
                stmt.setString(2, equipmentId);

                int rowsUpdated = stmt.executeUpdate();
                System.out.println("🔧 Rows updated in equipment_models: " + rowsUpdated);
                return rowsUpdated > 0;
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật mô tả từ equipment_id: " + e.getMessage());
            return false;
        }
    }


    @FXML
    private void handleCancel() {
        ((Stage) txtModelName.getScene().getWindow()).close();
    }

    @FXML
    public void handleSave(javafx.event.ActionEvent actionEvent) {
        if (currentEquipment == null) {
            new Alert(Alert.AlertType.ERROR, "Thiết bị chưa được chọn để sửa.").showAndWait();
            return;
        }

        // Lấy dữ liệu từ form
        Equipment edited = currentEquipment;
        edited.setModelName(txtModelName.getText());
        edited.setTypeName(txtTypeName.getText());
        edited.setSerialNumber(txtSerial.getText());
        edited.setStatus(cmbStatus.getValue());
        edited.setDefaultRoomName(txtDefaultRoomName.getText());
        edited.setNotes(txtNotes.getText());
        edited.setImgModel(txtImgModel.getText());
        edited.setDescription(txtDescription.getText());

        // Thực hiện cập nhật trong Thread riêng
        new Thread(() -> {
            EquipmentService service = new EquipmentService();
            boolean apiSuccess = false;
            boolean dbSuccess = false;
            boolean modelNameUpdated = false;
            boolean typeNameUpdated = false;
            boolean defaultRoomUpdated = false;
            boolean serialUpdated = false;
            boolean imgUpdated = false;

            try {
                // 1. Gọi API cập nhật thiết bị
                apiSuccess = service.updateEquipment(edited);

                if (apiSuccess) {
                    // 2. Cập nhật mô tả theo equipmentId
                    dbSuccess = updateDescriptionByEquipmentId(edited.getId(), edited.getDescription());

                    // 3. Cập nhật model name và type name theo equipmentId
                    modelNameUpdated = service.updateModelNameByEquipmentId(edited.getId(), edited.getModelName());
                    typeNameUpdated = service.updateTypeNameByEquipmentId(edited.getId(), edited.getTypeName());

                    // 4. Cập nhật default_room_id
                    if (edited.getDefaultRoomName() == null || edited.getDefaultRoomName().isBlank()) {
                        defaultRoomUpdated = service.clearDefaultRoomByEquipmentId(edited.getId());
                    } else {
                        defaultRoomUpdated = service.updateDefaultRoomByEquipmentId(edited.getId(), edited.getDefaultRoomName());
                    }
                    // 5. Cập nhật serialNumber
                    serialUpdated = service.updateSerialNumberByEquipmentId(edited.getId(), edited.getSerialNumber());
                    // 6. Cập nhật imgModel
                    imgUpdated = service.updateImgUrlByEquipmentId(edited.getId(), edited.getImgModel());

                } else {
                    System.out.println("⚠️ API thất bại, bỏ qua các bước cập nhật DB.");
                }

                // In kết quả debug
                System.out.println("✅ API success: " + apiSuccess);
                System.out.println("✅ Description update: " + dbSuccess);
                System.out.println("✅ Model name update: " + modelNameUpdated);
                System.out.println("✅ Type name update: " + typeNameUpdated);
                System.out.println("✅ Default room update/clear: " + defaultRoomUpdated);
                System.out.println("✅ Serial number update: " + serialUpdated);
                System.out.println("✅ Img model update: " + imgUpdated);

                boolean allSuccess = apiSuccess && dbSuccess && modelNameUpdated && typeNameUpdated && defaultRoomUpdated && imgUpdated;


                // Cập nhật giao diện
                Platform.runLater(() -> {
                    if (allSuccess) {
                        new Alert(Alert.AlertType.INFORMATION, "Cập nhật thành công!").showAndWait();
                        Stage stage = (Stage) txtModelName.getScene().getWindow();
                        stage.setUserData("updated");
                        stage.close();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Cập nhật thất bại ở API hoặc CSDL.").showAndWait();
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() ->
                        new Alert(Alert.AlertType.ERROR, "Lỗi mạng: " + e.getMessage()).showAndWait()
                );
            }
        }).start();
    }
}