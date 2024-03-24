package com.example.LibraryBee;

public class Message {

    private String messageText;
    private String sender;
    private long timestamp;

    public Message() {
        // Required empty public constructor for Firebase
    }

    public Message(String messageText, String sender, long timestamp) {
        this.messageText = messageText;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
