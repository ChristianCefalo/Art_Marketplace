package com.example.artmarketplace.model;

import androidx.annotation.Nullable;

/**
 * Represents a user's profile information stored in Firestore.
 */
public class UserProfile {

    private String uid;
    private String role;
    private String name;
    private String email;
    private long createdAt;

    /**
     * Required public no-arg constructor for Firestore deserialization.
     */
    @SuppressWarnings("unused")
    public UserProfile() {
        // Required by Firestore
    }

    public UserProfile(@Nullable String uid,
                       String role,
                       String name,
                       String email,
                       long createdAt) {
        this.uid = uid;
        this.role = role;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    @Nullable
    public String getUid() {
        return uid;
    }

    public void setUid(@Nullable String uid) {
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
