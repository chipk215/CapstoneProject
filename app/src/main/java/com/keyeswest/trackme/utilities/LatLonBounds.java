package com.keyeswest.trackme.utilities;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class LatLonBounds {
    Double mMinLat = null;
    Double mMinLon = null;
    Double mMaxLat = null;
    Double mMaxLon = null;

    public LatLonBounds(){}

    public void update(double latitude, double longitude){
        if (mMinLat == null){
            mMinLat = latitude;
            mMinLon = longitude;
            mMaxLat = latitude;
            mMaxLon = longitude;
        }else{
            mMinLat = min(mMinLat, latitude);
            mMaxLat = max(mMaxLat, latitude);
            mMinLon = min(mMinLon, longitude);
            mMaxLon = max(mMaxLon, longitude);
        }
    }

    public Double getMinLat() {
        return mMinLat;
    }

    public Double getMinLon() {
        return mMinLon;
    }

    public Double getMaxLat() {
        return mMaxLat;
    }

    public Double getMaxLon() {
        return mMaxLon;
    }
}

