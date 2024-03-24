package com.example.LibraryBee;

public class Seat {
    private int number;
    private Status status;

    public enum Status {
        AVAILABLE,
        SELECTED,
        RESERVED
    }

    public Seat(int number, Status status) {
        this.number = number;
        this.status = status;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
