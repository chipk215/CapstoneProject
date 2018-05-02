package com.keyeswest.trackme.models;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class Segment {

    private UUID mId;
    private long mTimeStamp;
    private boolean mMocked;
    private Double mMinLatitude = null;
    private Double mMaxLatitude = null;
    private Double mMinLongitude = null;
    private Double mMaxLongitude = null;
    private Double mDistance = null;


    private long rowId = 0;

    public Segment(){}

    public Segment(UUID id){
        this.mId = id;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        this.mId = id;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    public boolean isMocked() {
        return mMocked;
    }

    public void setMocked(boolean mocked) {
        this.mMocked = mocked;
    }

    public void setMocked(int mocked) {
        this.mMocked = (mocked==1);
    }

    public void setMinLatitude(Double minLatitude) {
        mMinLatitude = minLatitude;
    }

    public Double getMaxLatitude() {
        return mMaxLatitude;
    }

    public void setMaxLatitude(Double maxLatitude) {
        mMaxLatitude = maxLatitude;
    }

    public Double getMinLongitude() {
        return mMinLongitude;
    }

    public void setMinLongitude(Double minLongitude) {
        mMinLongitude = minLongitude;
    }

    public Double getMaxLongitude() {
        return mMaxLongitude;
    }

    public void setMaxLongitude(Double maxLongitude) {
        mMaxLongitude = maxLongitude;
    }

    public Double getDistance() {
        return mDistance;
    }

    public void setDistance(Double distance) {
        mDistance = distance;
    }

    public Double getMinLatitude() {
        return mMinLatitude;
    }

    public String getDateTime(){

        Date date = new Date(mTimeStamp * 1000);
        String dateString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
        return dateString;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }
}
