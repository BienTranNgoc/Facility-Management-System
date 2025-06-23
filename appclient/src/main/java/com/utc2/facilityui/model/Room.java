package com.utc2.facilityui.model;

import java.util.List;
//
public class Room {
    private String id;
    private String name;
    private String description;
    private int capacity;
    private String img; // Đường dẫn ảnh
    private String status;
    private String buildingName;
    private String roomTypeName; // Quan trọng để phân loại
    private String nameFacilityManager;
    private String location;
    private String createdAt; // Giữ là String hoặc parse thành LocalDateTime/Date
    private String updatedAt;
    private String deletedAt;
    private List<Object> defaultEquipments; // Hoặc List<Equipment> nếu có model Equipment

    public Room() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public String getNameFacilityManager() {
        return nameFacilityManager;
    }

    public void setNameFacilityManager(String nameFacilityManager) {
        this.nameFacilityManager = nameFacilityManager;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<Object> getDefaultEquipments() {
        return defaultEquipments;
    }

    public void setDefaultEquipments(List<Object> defaultEquipments) {
        this.defaultEquipments = defaultEquipments;
    }
}
