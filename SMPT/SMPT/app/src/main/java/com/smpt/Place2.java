package com.smpt;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Place2 implements Parcelable {
    private String title;
    private String extract;
    private String url;
    private double latitude;
    private double longitude;
    private List<String> imageUrls;
    private String mainImageUrl;

    public Place2(String title, String extract, String url, double latitude, double longitude, List<String> imageUrls, String mainImageUrl) {
        this.title = title;
        this.extract = extract;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrls = imageUrls;
        this.mainImageUrl = mainImageUrl;
    }

    protected Place2(Parcel in) {
        title = in.readString();
        extract = in.readString();
        url = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        imageUrls = in.createStringArrayList();
        mainImageUrl = in.readString();
    }

    public static final Creator<Place2> CREATOR = new Creator<Place2>() {
        @Override
        public Place2 createFromParcel(Parcel in) {
            return new Place2(in);
        }

        @Override
        public Place2[] newArray(int size) {
            return new Place2[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getExtract() {
        return extract;
    }

    public String getUrl() {
        return url;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(extract);
        dest.writeString(url);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(imageUrls);
        dest.writeString(mainImageUrl);
    }
}
