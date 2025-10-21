package com.example.artmarketplace;
import com.google.firebase.Timestamp;

public class Message {
    public String id;
    public String prompt;
    public String response;
    public String sender;
    public Timestamp createdAt;

    public Message() {} // Firestore needs empty constructor
}
