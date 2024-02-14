package com.smpt;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONArray;

public class Place implements Parcelable {
    private String name;
    private double latitude;
    private double longitude;
    private String photoUrl; // Nowe pole przechowujące URL do zdjęcia
    private String code;
    private Boolean open;
    private Integer rate;
    private String address;
    private String[] types;

    public Place(String name, double latitude, double longitude, String photoUrl, String code, Boolean open, Integer rate, String address, String[] types) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photoUrl;
        this.code=code;
        this.open=open;
        this.rate=rate;
        this.address=address;
        this.types=types;
    }

    protected Place(Parcel in) {
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        photoUrl = in.readString();
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

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

    public String getAddress() {
        return address;
    }
    public String getCode() {
        if (code != null && code.contains(" ")) {
            return code.substring(code.indexOf(" ") + 1);
        }
        return code;
    }
    public Boolean getOpen(){
        return open;
    }
    public Integer getRate(){
        return rate;
    }
    public String[] getTypes(){
        return types;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(photoUrl);
    }
}

