package com.keyeswest.trackme.models;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Location {

    public Location(){}

    private UUID segmentId;

    @SerializedName("TimeStamp")
    private long timeStamp;

    @SerializedName("Latitude")
    private double latitude;

    @SerializedName("Longitude")
    private double longitude;

    public UUID getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(UUID segmentId) {
        this.segmentId = segmentId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }




}
