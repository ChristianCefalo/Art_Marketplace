package com.example.artmarketplace;

public class Message {
    public String id;
    public String sender;   // "user","ai","system"
    public String prompt;   // user/system text
    public String response; // AI reply (extension writes this)
    public long createdAt;  // optional local use

    public Message() {} // Firestore needs empty ctor
}
