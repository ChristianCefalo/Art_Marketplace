package com.example.artmarketplace.net;

import androidx.annotation.Nullable;

/**
 * Represents a single chat message passed to the AI assistant.
 */
public class ChatMessage {

    private String role;
    private String content;

    /**
     * Empty constructor required for Gson serialization.
     */
    public ChatMessage() {
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "role='" + role + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChatMessage)) {
            return false;
        }
        ChatMessage other = (ChatMessage) obj;
        if (role != null ? !role.equals(other.role) : other.role != null) {
            return false;
        }
        return content != null ? content.equals(other.content) : other.content == null;
    }

    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
