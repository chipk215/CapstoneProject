package com.keyeswest.trackme.models;

import com.google.gson.annotations.SerializedName;


public class Trip {

    public Trip(){}

    public Segment getSegment() {
        return mSegment;
    }

    public void setSegment(Segment segment) {
        mSegment = segment;
    }

    public Location[] getLocations() {
        return mLocations;
    }

    public void setLocations(Location[] locations) {
        mLocations = locations;
    }

    @SerializedName("Segment")
    private Segment mSegment;

    @SerializedName("Locations")
    private Location[] mLocations;

}
