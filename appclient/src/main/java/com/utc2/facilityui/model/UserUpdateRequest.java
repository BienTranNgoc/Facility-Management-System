package com.utc2.facilityui.model;

// This class should mirror the fields allowed for update by the server's UserUpdateRequest DTO
public class UserUpdateRequest {
    private String username;
    private String fullName;
    private String roleName;
    // private String avatar; // Uncomment if you implement avatar update functionality

    // Constructor
    public UserUpdateRequest(String username, String fullName, String roleName /*, String avatar */) {
        this.username = username;
        this.fullName = fullName;
        this.roleName = roleName;
        // this.avatar = avatar;
    }

    // Getters are necessary for Gson to serialize this object to JSON
    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRoleName() {
        return roleName;
    }

    /*
    public String getAvatar() {
        return avatar;
    }
    */

    // Setters can be useful
    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /*
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    */
}