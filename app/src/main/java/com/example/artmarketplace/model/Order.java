package com.example.artmarketplace.model;

import androidx.annotation.Nullable;

/**
 * Represents a purchase order between a buyer and provider.
 */
public class Order {

    private String id;
    private String artId;
    private String buyerUid;
    private String providerUid;
    private String status;
    private int total;
    private long createdAt;

    /**
     * Required public no-arg constructor for Firestore deserialization.
     */
    @SuppressWarnings("unused")
    public Order() {
        // Required for Firestore
    }

    public Order(@Nullable String id,
                 String artId,
                 String buyerUid,
                 String providerUid,
                 String status,
                 int total,
                 long createdAt) {
        this.id = id;
        this.artId = artId;
        this.buyerUid = buyerUid;
        this.providerUid = providerUid;
        this.status = status;
        this.total = total;
        this.createdAt = createdAt;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public String getArtId() {
        return artId;
    }

    public void setArtId(String artId) {
        this.artId = artId;
    }

    public String getBuyerUid() {
        return buyerUid;
    }

    public void setBuyerUid(String buyerUid) {
        this.buyerUid = buyerUid;
    }

    public String getProviderUid() {
        return providerUid;
    }

    public void setProviderUid(String providerUid) {
        this.providerUid = providerUid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
