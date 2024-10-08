package com.example.LibraryBee;

public class Request {
    private String userId;
    private String username;
    private String userEmail;
    private String selectedSeatNumber;
    private String selectedSlot;
    private String amount;

    private boolean isApproved;

    private boolean isRejected;


    private long timestamp;

    public Request() {
        // Default constructor required for Firebase
    }

    public Request(String userId, String username, String userEmail, String selectedSeatNumber, String selectedSlot, String amount) {
        this.userId = userId;
        this.username = username;
        this.userEmail = userEmail;
        this.selectedSeatNumber = selectedSeatNumber;
        this.selectedSlot = selectedSlot;
        this.amount = amount;
        timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getSelectedSeatNumber() {
        return selectedSeatNumber;
    }

    public void setSelectedSeatNumber(String selectedSeatNumber) {
        this.selectedSeatNumber = selectedSeatNumber;
    }

    public String getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(String selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getRequestId() {
        return getUserId();
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isRejected() {
        return isRejected;
    }

    public void setRejected(boolean rejected) {
        isRejected = rejected;
    }
}
