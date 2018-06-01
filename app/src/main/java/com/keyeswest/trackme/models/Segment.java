package com.keyeswest.trackme.models;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.keyeswest.trackme.R;
import com.keyeswest.trackme.data.SegmentSchema;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.UUID;

import timber.log.Timber;

public class Segment implements Parcelable {

    private static double METERS_TO_MILES = 0.000621371;

    @SerializedName("ID")
    private UUID mId;

    @SerializedName("TimeStamp")
    private long mTimeStamp;

    private boolean mFavorite = false;

    @SerializedName("MinLat")
    private Double mMinLatitude = null;

    @SerializedName("MaxLat")
    private Double mMaxLatitude = null;

    @SerializedName("MinLon")
    private Double mMinLongitude = null;

    @SerializedName("MaxLon")
    private Double mMaxLongitude = null;

    @SerializedName("Distance")
    private Double mDistance = null;

    @SerializedName("Duration")
    private long mElapsedTime = 0;

    private Double mMaximumSpeed = 0d;
    private long mRowId = 0;


    public static final Parcelable.Creator<Segment> CREATOR
            = new Parcelable.Creator<Segment>(){

        public Segment createFromParcel(Parcel in){
            return new Segment(in);
        };

        public Segment[] newArray(int size){
            return new Segment[size];
        }
    };

    private Segment(Parcel in){

        String id = in.readString();
        mId = UUID.fromString(id);

        mTimeStamp = in.readLong();
        mFavorite = in.readByte() != 0;
        mMinLatitude = in.readDouble();
        mMaxLatitude = in.readDouble();
        mMinLongitude = in.readDouble();
        mMaxLongitude = in.readDouble();
        mDistance = in.readDouble();
        mRowId = in.readLong();
        mElapsedTime = in.readLong();
        mMaximumSpeed = in.readDouble();

    }


    @Override
    public boolean equals(Object o){

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Segment or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Segment)) {
            return false;
        }

        Segment segment = (Segment) o;

        return mId.equals(segment.getId());


    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId.toString());
        dest.writeLong(mTimeStamp);
        dest.writeByte((byte)(mFavorite ? 1:0));
        dest.writeDouble(mMinLatitude);
        dest.writeDouble(mMaxLatitude);
        dest.writeDouble(mMinLongitude);
        dest.writeDouble(mMaxLongitude);
        dest.writeDouble(mDistance);
        dest.writeLong(mRowId);
        dest.writeLong(mElapsedTime);
        dest.writeDouble(mMaximumSpeed);

    }



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

    public boolean isFavorite() {
        return mFavorite;
    }

    public void setFavorite(int favorite) {
        this.mFavorite = (favorite==1);
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

    public String getDistanceMiles(){
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(mDistance * METERS_TO_MILES);
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

    public String getDate(){
        Date date = new Date(mTimeStamp * 1000);
        String dateString = DateFormat.getDateInstance(DateFormat.SHORT).format(date);
        return dateString;

    }

    public String getTime(){
        Date date = new Date(mTimeStamp * 1000);
        String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        return timeString;

    }

    public DurationRecord getSegmentDuration(Context context){
        DurationRecord record = new DurationRecord(context, mElapsedTime);
        return record;
    }

    public long getRowId() {
        return mRowId;
    }

    public void setRowId(long rowId) {
        this.mRowId = rowId;
    }

    public long getElapsedTime() {
        return mElapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        mElapsedTime = elapsedTime;
    }

    public Double getMaximumSpeed() {
        return mMaximumSpeed;
    }

    public void setMaximumSpeed(Double maximumSpeed) {
        mMaximumSpeed = maximumSpeed;
    }


    public Uri getSegmentUri(){
        return SegmentSchema.SegmentTable.buildItemUri(mRowId);
    }



}
