package com.utc2.facilityui.model; // Đặt vào package model

public class MaintenanceRequestClient {
    // ... (nội dung còn lại của lớp giữ nguyên) ...
    private String roomId;
    private String itemId;
    private String description;

    public MaintenanceRequestClient() {}

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}