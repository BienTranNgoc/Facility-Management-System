package com.utc2.facilityui.response;

import java.math.BigDecimal;
// Sử dụng String cho các trường ngày giờ để đơn giản, Gson mặc định sẽ xử lý
// Nếu server trả về kiểu LocalDateTime và Gson được cấu hình đúng, bạn có thể dùng LocalDateTime.
// import java.time.LocalDateTime;

public class MaintenanceResponse {
    private String id;
    private String roomName;
    private String modelName;
    private String reportByUser;
    private String technicianName;
    private String description;
    private String notes;
    private BigDecimal cost;
    private String actionTaken;
    private String status;
    private String updatedAt;
    private String startDate;
    private String completionDate;
    private String reportDate;

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getReportByUser() { return reportByUser; }
    public void setReportByUser(String reportByUser) { this.reportByUser = reportByUser; }
    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getCompletionDate() { return completionDate; }
    public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }
    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }
}