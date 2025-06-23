package com.utc2.facilityui.controller.equipment;

import com.utc2.facilityui.model.Equipment;
import com.utc2.facilityui.model.EquipmentModel;
import com.utc2.facilityui.model.Room;
import com.utc2.facilityui.service.EquipmentService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
        import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AddEquipmentController {

    @FXML
    private ComboBox<EquipmentModel> cmbModel;

    @FXML
    private TextField txtSerial;

    @FXML
    private TextField txtAssetTag;

    @FXML
    private DatePicker dpPurchaseDate;

    @FXML
    private DatePicker dpWarrantyExpiryDate;

    @FXML
    private ComboBox<Room> cmbRoom;

    @FXML
    private TextField txtNotes;

    private EquipmentService service = new EquipmentService();

    @FXML
    public void initialize() {
        // Load danh sách model thiết bị lên cmbModel
        new Thread(() -> {
            List<EquipmentModel> models = service.getAllModels();
            Platform.runLater(() -> {
                ObservableList<EquipmentModel> modelList = FXCollections.observableArrayList(models);
                cmbModel.setItems(modelList);
                if (!modelList.isEmpty()) {
                    cmbModel.getSelectionModel().selectFirst();
                }

                // Set cell factory cho cmbModel
                cmbModel.setCellFactory(param -> new ListCell<>() {
                    @Override
                    protected void updateItem(EquipmentModel item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getModelName());
                    }
                });
                cmbModel.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(EquipmentModel item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getModelName());
                    }
                });
            });
        }).start();

        // Load danh sách phòng lên cmbRoom
        new Thread(() -> {
            List<Room> rooms = service.getAllRooms();
            Platform.runLater(() -> {
                ObservableList<Room> roomList = FXCollections.observableArrayList(rooms);
                cmbRoom.setItems(roomList);
                if (!roomList.isEmpty()) {
                    cmbRoom.getSelectionModel().selectFirst();
                }

                // Set cell factory cho cmbRoom
                cmbRoom.setCellFactory(param -> new ListCell<>() {
                    @Override
                    protected void updateItem(Room item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getName());
                    }
                });
                cmbRoom.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(Room item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getName());
                    }
                });
            });
        }).start();
    }

    @FXML
    public void handleSave(ActionEvent actionEvent) {
        Equipment newEq = new Equipment();

        EquipmentModel selectedModel = cmbModel.getValue();
        if (selectedModel == null) {
            showAlert(Alert.AlertType.ERROR, "Vui lòng chọn model thiết bị.");
            return;
        }
        newEq.setModelId(selectedModel.getModelId());

        if (txtSerial.getText() == null || txtSerial.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Vui lòng nhập số serial.");
            return;
        }
        newEq.setSerialNumber(txtSerial.getText());

        Room selectedRoom = cmbRoom.getValue();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.ERROR, "Vui lòng chọn phòng mặc định.");
            return;
        }
        newEq.setDefaultRoomId(selectedRoom.getId());

        newEq.setNotes(txtNotes.getText());

        new Thread(() -> {
            try {
                boolean created = service.createEquipment(newEq);

                Platform.runLater(() -> {
                    if (created) {
                        showAlert(Alert.AlertType.INFORMATION, "Thêm thiết bị thành công!");
                        Stage stage = (Stage) cmbModel.getScene().getWindow();
                        stage.setUserData("created");
                        stage.close();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Không thể thêm thiết bị.");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi khi kết nối API: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void handleCancel(ActionEvent actionEvent) {
        Stage stage = (Stage) cmbModel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}