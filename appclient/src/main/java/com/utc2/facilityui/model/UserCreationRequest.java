package com.utc2.facilityui.model;

// This class should mirror the fields expected by the server's UserCreationRequest DTO
public class UserCreationRequest {
    private String userId;
    private String username;
    private String email;
    private String fullName; // Can be optional on client, but server expects @NotBlank
    private String roleName;

    // Constructor
    public UserCreationRequest(String userId, String username, String email, String fullName, String roleName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roleName = roleName;
    }

    // Getters are useful for Gson serialization and for other parts of your code if needed.
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRoleName() {
        return roleName;
    }

    // Setters are optional if you only create the object once via constructor.
    // However, they can be useful for form-building or if the object is modified before sending.
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}