package com.example.LibraryBee.User_Pannel;

// Book.java
public class Book {
    private final String title;
    private final String author;
    private final int year;
    private final String imageUrl;

    public Book(String title, String author, int year, String imageUrl) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

