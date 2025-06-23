package com.utc2.facilityui.model;

public class Equipment {
    private String id;
    private String modelId;
    private String modelName;
    private String typeName;
    private String serialNumber;
    private String status;
    private String purchaseDate;
    private String warrantyExpiryDate;
    private String defaultRoomName;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private String imgModel;
    private String description;
    private String assetTag;
    private String defaultRoomId;


    // Getters + Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getWarrantyExpiryDate() { return warrantyExpiryDate; }
    public void setWarrantyExpiryDate(String warrantyExpiryDate) { this.warrantyExpiryDate = warrantyExpiryDate; }

    public String getDefaultRoomName() { return defaultRoomName; }
    public void setDefaultRoomName(String defaultRoomName) { this.defaultRoomName = defaultRoomName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getImgModel() { return imgModel; }
    public void setImgModel(String imgModel) { this.imgModel = imgModel; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModelId() {
        return modelId;
    }
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getAssetTag() {
        return assetTag;
    }
    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getDefaultRoomId() {
        return defaultRoomId;
    }
    public void setDefaultRoomId(String defaultRoomId) {
        this.defaultRoomId = defaultRoomId;
    }
}
