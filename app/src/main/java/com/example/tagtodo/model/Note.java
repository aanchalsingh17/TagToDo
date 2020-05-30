package com.example.tagtodo.model;

import android.widget.ImageView;

public class Note {
    //  Note class to be passed into FireStoreRecyclerOptions
    //  These fields must match with contents of firebase database
    private String title;
    private String content;
    private String location;
    private String latitude;
    private String longitude;
    private String alarm;

    public Note() {}

    public Note(String title, String content, String location, String latitude, String longitude ,String alarm) {
        this.title = title;
        this.content = content;
        this.location = location;
        this.latitude  = latitude;
        this.longitude = longitude;
        this.alarm = alarm;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }


}
