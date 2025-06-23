package com.utc2.facilityui.controller.equipment;

import com.utc2.facilityui.model.Equipment;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.nio.file.Paths;

public class ViewEquipmentController {

    @FXML
    private Label labelName, labelModel, labelRoom, labelStatus, labelSerial, labelNotes, labelDescription;

    @FXML
    private ImageView imageView;

    public void setEquipment(Equipment equipment) {
        labelName.setText(equipment.getTypeName());
        labelModel.setText(equipment.getModelName());
        labelRoom.setText(equipment.getDefaultRoomName());
        labelStatus.setText(equipment.getStatus());
        labelSerial.setText(equipment.getSerialNumber());
        labelNotes.setText(equipment.getNotes());
        labelDescription.setText(equipment.getDescription());

        // ✅ xử lý ảnh
        String imagePath = equipment.getImgModel();
        if (imagePath != null && !imagePath.isEmpty()) {
            String fullPath = "/com/utc2/facilityui/images/models/" + Paths.get(imagePath).getFileName();
            InputStream is = getClass().getResourceAsStream(fullPath);
            if (is != null) {
                imageView.setImage(new Image(is));
            } else {
                System.err.println("⚠ Không tìm thấy ảnh: " + fullPath);
            }
        } else {
            System.out.println("ℹ Không có ảnh cho thiết bị này");
        }
    }
}

