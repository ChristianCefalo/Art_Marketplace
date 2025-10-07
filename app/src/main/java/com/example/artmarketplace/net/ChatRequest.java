package com.example.artmarketplace.net;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for the chat Cloud Function.
 */
public class ChatRequest {

    private List<ChatMessage> messages;

    /**
     * Empty constructor for Gson.
     */
    public ChatRequest() {
        messages = new ArrayList<>();
    }

    public ChatRequest(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
}
