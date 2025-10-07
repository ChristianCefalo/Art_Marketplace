package com.example.artmarketplace.net;

/**
 * Response payload returned by the chat Cloud Function.
 */
public class ChatResponse {

    private String reply;

    /**
     * Empty constructor for Gson.
     */
    public ChatResponse() {
    }

    public ChatResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
