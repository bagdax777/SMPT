package com.smpt;

public class Place {
    private String name;
    private double latitude;
    private double longitude;
    private String photoUrl; // Nowe pole przechowujące URL do zdjęcia

    public Place(String name, double latitude, double longitude, String photoUrl) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

