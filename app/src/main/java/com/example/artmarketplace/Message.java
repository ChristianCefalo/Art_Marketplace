package com.example.artmarketplace;

import com.google.firebase.Timestamp;
import java.util.Map;

public class Message {
    public String id;
    public String prompt;
    public String response;
    public String sender;
    public Timestamp createdAt;

    // Optional: attachment map {url, name, mime, size?}
    public Map<String, Object> attachment;

    public Message() {}
}

