package com.utc2.facilityui.model;

import com.utc2.facilityui.response.UserResponse; // Đảm bảo import đúng lớp UserResponse của client
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class User {
    // Trường dữ liệu
    private String id;
    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String avatar;
    private String roleName;
    // THAY ĐỔI: Kiểu dữ liệu thành LocalDateTime
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // JavaFX Properties (transient)
    private transient StringProperty idProperty;
    private transient StringProperty userIdProperty;
    private transient StringProperty usernameProperty;
    private transient StringProperty fullNameProperty;
    private transient StringProperty emailProperty;
    private transient StringProperty roleNameProperty;
    private transient StringProperty createdAtProperty; // Vẫn là StringProperty để hiển thị
    private transient StringProperty updatedAtProperty; // Vẫn là StringProperty để hiển thị

    private static final String PLACEHOLDER_FOR_NULL_DATE = "N/A"; // Hoặc "N/A", "-", tùy bạn chọn
    private static final String PLACEHOLDER_FOR_NULL_TEXT = "N/A"; // Hoặc "N/A", "", tùy bạn chọn

    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy", new Locale("vi", "VN"));

    public User() {
    }

    public User(UserResponse dto) { // UserResponse client vẫn có createdAt/updatedAt là String
        if (dto == null) return;
        this.id = dto.getId();
        this.userId = dto.getUserId();
        this.username = dto.getUsername();
        this.fullName = dto.getFullName();
        this.email = dto.getEmail();
        this.avatar = dto.getAvatar();
        this.roleName = dto.getRoleName();

        // THAY ĐỔI: Parse chuỗi từ DTO thành LocalDateTime
        this.createdAt = parseRawDateTimeString(dto.getCreatedAt());
        this.updatedAt = parseRawDateTimeString(dto.getUpdatedAt());
    }

    /**
     * Parse chuỗi ISO date-time (từ DTO) thành đối tượng LocalDateTime.
     * Trả về null nếu không parse được hoặc đầu vào là null/rỗng.
     */
    private LocalDateTime parseRawDateTimeString(String isoDateTimeString) {
        // Thêm dòng log để debug xem client nhận được gì
        System.out.println("User.java (model) - parseRawDateTimeString nhận được: '" + isoDateTimeString + "'");
        if (isoDateTimeString == null || isoDateTimeString.isBlank() || isoDateTimeString.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            // Thử parse theo chuẩn Instant (thường có 'Z' hoặc offset)
            Instant instant = Instant.parse(isoDateTimeString);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (DateTimeParseException e1) {
            try {
                // Thử parse theo chuẩn ISO_LOCAL_DATE_TIME (ví dụ: yyyy-MM-ddTHH:mm:ss.SSS)
                return LocalDateTime.parse(isoDateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                try {
                    // Thử parse với một pattern cụ thể hơn không có millisecond, phổ biến
                    return LocalDateTime.parse(isoDateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                } catch (DateTimeParseException e3) {
                    System.err.println("User Model: Lỗi parse ngày trong parseRawDateTimeString cho '" + isoDateTimeString + "': " + e3.getMessage());
                    return null; // Trả về null nếu tất cả các cách parse đều thất bại
                }
            }
        }
    }

    /**
     * Định dạng đối tượng LocalDateTime thành chuỗi hiển thị.
     * Trả về PLACEHOLDER_FOR_NULL_DATE nếu LocalDateTime là null.
     */
    private String formatDisplayDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return PLACEHOLDER_FOR_NULL_DATE;
        }
        return dateTime.format(DISPLAY_DATE_TIME_FORMATTER);
    }

    private String getFormattedText(String value) {
        return (value == null || value.isBlank() || value.equalsIgnoreCase("null")) ? PLACEHOLDER_FOR_NULL_TEXT : value;
    }

    // --- Property Getters ---
    public StringProperty idProperty() {
        if (idProperty == null) idProperty = new SimpleStringProperty(this, "id", getId());
        return idProperty;
    }

    public StringProperty userIdProperty() {
        if (userIdProperty == null) userIdProperty = new SimpleStringProperty(this, "userId", getUserId());
        return userIdProperty;
    }

    public StringProperty usernameProperty() {
        if (usernameProperty == null) usernameProperty = new SimpleStringProperty(this, "username", getUsername());
        return usernameProperty;
    }

    public StringProperty fullNameProperty() {
        if (fullNameProperty == null) fullNameProperty = new SimpleStringProperty(this, "fullName", getFullName());
        return fullNameProperty;
    }

    public StringProperty emailProperty() {
        if (emailProperty == null) emailProperty = new SimpleStringProperty(this, "email", getEmail());
        return emailProperty;
    }

    public StringProperty roleNameProperty() {
        if (roleNameProperty == null) roleNameProperty = new SimpleStringProperty(this, "roleName", getRoleName());
        return roleNameProperty;
    }

    public StringProperty createdAtProperty() {
        if (createdAtProperty == null) createdAtProperty = new SimpleStringProperty(this, "createdAt", getCreatedAt());
        return createdAtProperty;
    }

    public StringProperty updatedAtProperty() {
        if (updatedAtProperty == null) updatedAtProperty = new SimpleStringProperty(this, "updatedAt", getUpdatedAt());
        return updatedAtProperty;
    }

    // --- Standard Getters (cho hiển thị - trả về String đã định dạng) ---
    public String getId() { return getFormattedText(this.id); }
    public String getUserId() { return getFormattedText(this.userId); }
    public String getUsername() { return getFormattedText(this.username); }
    public String getFullName() { return getFormattedText(this.fullName); }
    public String getEmail() { return getFormattedText(this.email); }
    public String getRoleName() { return getFormattedText(this.roleName); }
    // THAY ĐỔI: Các getter này giờ định dạng từ trường LocalDateTime
    public String getCreatedAt() { return formatDisplayDateTime(this.createdAt); }
    public String getUpdatedAt() { return formatDisplayDateTime(this.updatedAt); }

    public String getAvatar() { return this.avatar; }

    // --- Raw Value Getters (lấy giá trị gốc) ---
    public String getRawId() { return this.id; }
    public String getRawUserId() { return this.userId; }
    // ... (thêm các getRaw... khác cho các trường String nếu cần)

    // THAY ĐỔI: Getter cho giá trị LocalDateTime gốc
    public LocalDateTime getRawCreatedAt() { return this.createdAt; }
    public LocalDateTime getRawUpdatedAt() { return this.updatedAt; }


    // --- Setters (cập nhật giá trị gốc) ---
    public void setId(String id) { this.id = id; if (idProperty != null) idProperty.set(getId());}
    public void setUserId(String userId) { this.userId = userId; if (userIdProperty != null) userIdProperty.set(getUserId());}
    public void setUsername(String username) { this.username = username; if (usernameProperty != null) usernameProperty.set(getUsername());}
    public void setFullName(String fullName) { this.fullName = fullName; if (fullNameProperty != null) fullNameProperty.set(getFullName());}
    public void setEmail(String email) { this.email = email; if (emailProperty != null) emailProperty.set(getEmail());}
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setRoleName(String roleName) { this.roleName = roleName; if (roleNameProperty != null) roleNameProperty.set(getRoleName());}

    // THAY ĐỔI: Setters cho createdAt và updatedAt giờ nhận LocalDateTime
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        if (createdAtProperty != null) createdAtProperty.set(getCreatedAt());
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        if (updatedAtProperty != null) updatedAtProperty.set(getUpdatedAt());
    }

    // Nếu bạn vẫn cần set từ String (ví dụ, khi test), bạn có thể giữ lại các phương thức này
    // hoặc gọi parseRawDateTimeString bên ngoài trước khi gọi setter
    public void setCreatedAtFromString(String isoDateTimeString) {
        this.createdAt = parseRawDateTimeString(isoDateTimeString);
        if (createdAtProperty != null) createdAtProperty.set(getCreatedAt());
    }
    public void setUpdatedAtFromString(String isoDateTimeString) {
        this.updatedAt = parseRawDateTimeString(isoDateTimeString);
        if (updatedAtProperty != null) updatedAtProperty.set(getUpdatedAt());
    }
}