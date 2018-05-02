package com.keyeswest.trackme.models;

import java.util.UUID;

public class Location {

    public Location(){}

    private UUID segmentId;
    private long timeStamp;

    private double latitude;
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
