package com.example.myfinalproject;

import java.util.List;

public class Event {
    private String id; // Field to store the Firestore document ID
    private String name;
    private String date;
    private String description;
    private String imageUrl;
    private List<String> joins; // List to store the UIDs of users who joined

    // Default constructor required for Firestore
    public Event() {
        // Empty constructor for Firestore
    }

    // Constructor with parameters
    public Event(String id, String name, String date, String description, String imageUrl, List<String> joins) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.description = description;
        this.imageUrl = imageUrl;
        this.joins = joins;
    }

    // Getter and setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public List<String> getJoins() {
        return joins; // Return the list of joined users
    }

    public void setJoins(List<String> joins) {
        this.joins = joins; // Set the list of joined users
    }
}
