package com.keyeswest.trackme.utilities;

import timber.log.Timber;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class LatLonBounds {
    Double mMinLat = null;
    Double mMinLon = null;
    Double mMaxLat = null;
    Double mMaxLon = null;

    public LatLonBounds(){}

    public void update(double latitude, double longitude){
        Timber.d("Update bounds.. latitude= " + Double.toString(latitude) +
                "  longitude= " + Double.toString(longitude));

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

      //  Timber.d("Updated Bounds: maxLat= "+  Double.toString(mMaxLat));
      //  Timber.d("Updated Bounds: maxLon= "+  Double.toString(mMaxLon));
      //  Timber.d("Updated Bounds: minLat= "+  Double.toString(mMinLat));
      //  Timber.d("UpdatedBounds: minLon= "+  Double.toString(mMinLon));
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

    public void setMinLat(Double minLat) {
        mMinLat = minLat;
    }

    public void setMinLon(Double minLon) {
        mMinLon = minLon;
    }

    public void setMaxLat(Double maxLat) {
        mMaxLat = maxLat;
    }

    public void setMaxLon(Double maxLon) {
        mMaxLon = maxLon;
    }
}

