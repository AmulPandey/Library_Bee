package com.example.LibraryBee;

public class Admin {
    private String email;

    public Admin() {
        // Default constructor required for Firebase
    }

    public Admin(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

