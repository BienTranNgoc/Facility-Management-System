package com.utc2.facilityui.model;

public class EquipmentModel {
    private String modelId;
    private String modelName;

    public EquipmentModel() {
    }

    public EquipmentModel(String modelId, String modelName) {
        this.modelId = modelId;
        this.modelName = modelName;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    // Quan trọng: để ComboBox hiển thị tên model thay vì đối tượng
    @Override
    public String toString() {
        return modelName;
    }
}