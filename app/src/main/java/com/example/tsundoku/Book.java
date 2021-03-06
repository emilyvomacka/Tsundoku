package com.example.tsundoku;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Book implements Comparable<Book> {
    private String title;
    private String author;
    private String description;
    private String imageUrl;
    private Timestamp timeAdded;
    private String userId;
    private String userName;
    private Boolean priority;
    private String status;
    private Integer pageCount;

    //We must have an empty constructor for Firestore
    public Book() { }

    public Book(String title, String author, String description, String imageUrl,
                Timestamp timeAdded, String userId, String userName,
                Boolean priority, String status, Integer pageCount) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.timeAdded = timeAdded;
        this.userId = userId;
        this.userName = userName;
        this.priority = priority;
        this.status = status;
        this.pageCount = pageCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

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

    public Boolean getPriority() {
        return priority;
    }

    public void setPriority(Boolean priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public int compareTo(Book o) {
        int priorityCmp = o.priority.compareTo(priority);
        return (priorityCmp != 0 ? priorityCmp : o.timeAdded.compareTo(timeAdded));
    }
}
