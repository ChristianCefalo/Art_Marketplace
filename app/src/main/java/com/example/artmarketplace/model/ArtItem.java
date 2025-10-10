package com.example.artmarketplace.model;

import androidx.annotation.Nullable;

/**
 * Represents an art listing stored in Firestore.
 */
public class ArtItem {

    private String id;
    private String title;
    private int price;
    private String desc;
    private String imageUrl;
    private String providerUid;
    private long createdAt;

    /**
     * Required public no-arg constructor for Firestore deserialization.
     */
    @SuppressWarnings("unused")
    public ArtItem() {
        // Firestore uses reflection
    }

    public ArtItem(@Nullable String id,
                   String title,
                   int price,
                   String desc,
                   String imageUrl,
                   String providerUid,
                   long createdAt) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.desc = desc;
        this.imageUrl = imageUrl;
        this.providerUid = providerUid;
        this.createdAt = createdAt;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProviderUid() {
        return providerUid;
    }

    public void setProviderUid(String providerUid) {
        this.providerUid = providerUid;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
