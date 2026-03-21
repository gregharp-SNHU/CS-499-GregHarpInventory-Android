package com.mobile2app.gregharpinventory.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = @Index(value = "username", unique = true)
)
public class User {
    // use a long integer user ID as a unique key for the DB
    @PrimaryKey(autoGenerate = true)
    private long userId;

    // username
    @NonNull
    private String username;
    // password -- stored plaintext and locally -- normally this should be encrypted
    @NonNull
    private String password;
    // role stored as a string
    @NonNull
    private String role;
    // phone number stored as string
    @NonNull
    private String phone;

    // constants for valid roles
    public static final String ROLE_USER = "User";
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_OWNER = "Owner";

    // no-argument constructor for Room
    public User () {
        // nothing to do here
    }

    // parameterized constructor for testing -- ignored by Room
    @Ignore
    public User(String username, String password, String role, String phone) {
        // use the setters since they perform validation
        setUsername(username);
        setPassword(password);
        setRole(role);
        setPhone(phone);
    }

    // method to get User ID
    public long getUserId() {
        return userId;
    }

    // method to set User ID
    public void setUserId(long userId) {
        // validate user ID hasn't already been set by room
        if (this.userId != 0) {
            throw new IllegalStateException("User ID has already been set by the database");
        }

        // validate user ID is otherwise >=0
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }

        this.userId = userId;
    }

    // method to get username
    @NonNull
    public String getUsername() {
        return(username);
    }

    // method to validate and set username
    public void setUsername(String username) {
        // validate username
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        this.username = username.trim();
    }

    // method to get password
    @NonNull
    public String getPassword() {
        return(password);
    }

    // method to validate and set password
    public void setPassword(String password) {
        // validate password
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        this.password = password.trim();
    }

    // method to get role
    @NonNull
    public String getRole() {
        return role;
    }

    // method to validate and set role
    public void setRole(String role) {
        // validate non-null and not empty
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }

        // validate role matches one of the available ones
        if (!role.equals(ROLE_USER) && !role.equals(ROLE_MANAGER) && !role.equals(ROLE_OWNER)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        this.role = role;
    }

    // method to get phone
    @NonNull
    public String getPhone() {
        return(phone);
    }

    // method to validate and set phone number -- new phone should not be blank
    public void setPhone(String phone) {
        // validate phone isn't null -- can be empty
        if (phone == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }

        this.phone = phone.trim();
    }
}

