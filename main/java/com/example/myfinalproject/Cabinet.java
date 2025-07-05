package com.example.myfinalproject;

public class Cabinet {
    private String documentId; // Optional for Firestore document ID
    private String cabinetName;
    private String vision;
    private String mission;
    private String imageUrl;

    // Default constructor (required by Firestore)
    public Cabinet() {}

    // Constructor for creating new cabinets
    public Cabinet(String cabinetName, String vision, String mission, String imageUrl) {
        this.cabinetName = cabinetName;
        this.vision = vision;
        this.mission = mission;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getCabinetName() { return cabinetName; }

    public void setCabinetName(String cabinetName) { this.cabinetName = cabinetName; }

    public String getVision() { return vision; }

    public void setVision(String vision) { this.vision = vision; }

    public String getMission() { return mission; }

    public void setMission(String mission) { this.mission = mission; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
