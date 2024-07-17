package com.example.LibraryBee.Admin_Pannel;

public class Request {
    private String userId;
    private String userName;
    private String userEmail;
    private String selectedSeatNumber;
    private String selectedSlot;
    private String amount;

    private boolean isApproved;

    private boolean isRejected;

    public Request() {
        // Default constructor required for Firebase
    }

    public Request(String userId, String userName, String userEmail, String selectedSeatNumber, String selectedSlot, String amount) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.selectedSeatNumber = selectedSeatNumber;
        this.selectedSlot = selectedSlot;
        this.amount = amount;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
