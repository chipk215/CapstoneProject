package com.keyeswest.trackme.data;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.keyeswest.trackme.models.Segment;

import java.util.List;
import java.util.UUID;


import timber.log.Timber;

import static com.keyeswest.trackme.data.TrackerBaseHelper.createLocationRecord;

import static com.keyeswest.trackme.data.TrackerBaseHelper.createSegmentRecord;


import static com.keyeswest.trackme.data.TrackerBaseHelper
        .updateSegmentRecordBoundsDistanceElapsed;

import static com.keyeswest.trackme.data.TrackerBaseHelper.updateSegmentRecordFavoriteStatus;

import static com.keyeswest.trackme.data.TracksContentProvider
        .CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS;

public class Queries {

    public static LocationCursor getLatestLocationBySegmentId(Context context, String segmentId){

        String[] selectionArgs = {segmentId};
        String selectionClause = LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " = ?";
        String orderByClause = LocationSchema.LocationTable.COLUMN_TIME_STAMP + " DESC ";

        Uri queryUri = LocationSchema.LocationTable.CONTENT_URI;
        queryUri = queryUri.buildUpon().appendQueryParameter("limit","1").build();
        ContentResolver resolver = context.getContentResolver();
        @SuppressLint("Recycle")
        Cursor cursor = resolver.query(queryUri, null, selectionClause, selectionArgs,
                orderByClause);

        if (cursor != null){
            return new LocationCursor(cursor);
        }

        return null;

    }


    /***
     * Inserts a new segment.
     * @param context - context of client
     * @return segmentId of the new segment
     */
    public static Uri createNewSegment(Context context) {

        String segmentId = UUID.randomUUID().toString();
        // timestamp in seconds
        long timeStamp = System.currentTimeMillis() / 1000;

        ContentValues values = createSegmentRecord(segmentId, timeStamp);

        ContentResolver contentResolver = context.getContentResolver();

        try {
            return contentResolver.insert(SegmentSchema.SegmentTable.CONTENT_URI, values);
        } catch (Exception ex) {
            Timber.e(ex, "Exception raised inserting segment to db.");
            return null;
        }

    }


    public static Segment getSegmentFromUri(Context context, Uri uri){

        ContentResolver resolver = context.getContentResolver();
        @SuppressLint("Recycle")
        Cursor cursor = resolver.query(uri, null, null, null,
                null);

        if (cursor != null){
            SegmentCursor segmentCursor = new SegmentCursor(cursor);
            segmentCursor.moveToNext();
            Segment segment = segmentCursor.getSegment();
            segmentCursor.close();
            return segment;
        }

        return null;

    }


    public static Segment getSegmentFromSegmentId(Context context, String segmentId){

        String[] selectionArgs = {segmentId};
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + "= ?";
        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(
                 SegmentSchema.SegmentTable.CONTENT_URI, null, selectionClause,
                selectionArgs, null);

        if (cursor != null){
            SegmentCursor segmentCursor = new SegmentCursor(cursor);
            segmentCursor.moveToFirst();
            Segment segment = segmentCursor.getSegment();
            cursor.close();
            return segment;
        }

        return null;
    }


    public static int updateSegmentBoundsDistanceElapsedTime(Context context, String segmentId,
                                                  double minLat, double maxLat,
                                                  double minLon, double maxLon,
                                                  double distance, long elapsedTime){

        //update the segment
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + " = ?";
        String[] selectionArgs = {segmentId};
        ContentValues updateValues = updateSegmentRecordBoundsDistanceElapsed(minLat, maxLat,
                minLon, maxLon, distance, elapsedTime);
        ContentResolver resolver = context.getContentResolver();

        return resolver.update(SegmentSchema.SegmentTable.CONTENT_URI, updateValues,
                selectionClause, selectionArgs);

    }

    public static void updateSegmentFavoriteStatus(Context context, UUID segmentId,
                                                  boolean favoriteStatus){
        //update the segment
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + " = ?";
        String[] selectionArgs = {segmentId.toString()};
        ContentValues updateValues = updateSegmentRecordFavoriteStatus(favoriteStatus);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(SegmentSchema.SegmentTable.CONTENT_URI, updateValues,
                selectionClause, selectionArgs);
    }


    public static Uri createNewLocationFromSample(Context context,
                                                  android.location.Location sample,
                                                  String segmentId){

        ContentResolver contentResolver = context.getContentResolver();

        long epochTimeSec = sample.getTime();
        ContentValues locationValues = createLocationRecord(epochTimeSec,
                sample.getLatitude(), sample.getLongitude(), segmentId);

        return contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI,
                locationValues);

    }

    public static LocationCursor getLocationsForSegment(Context context, Uri segmentUri){
        String segmentRowId = segmentUri.getLastPathSegment();
        Uri requestUri = CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS;
        requestUri = requestUri.buildUpon().appendPath(segmentRowId).build();
        ContentResolver contentResolver = context.getContentResolver();
        @SuppressLint("Recycle")
        Cursor cursor = contentResolver.query(requestUri, null, null,
                null, null);

        return new LocationCursor(cursor);
    }

    public static SegmentCursor getSegmentsFromUriList(Context context, List<Uri> segments){


        String selectionClause = SegmentSchema.SegmentTable._ID + " IN ( ";
        for (int i=0; i< segments.size(); i++){
            String segmentRowId = segments.get(i).getLastPathSegment();
            if (i== (segments.size() -1 )){
                selectionClause = selectionClause.concat(segmentRowId + " ");
            }else{
                selectionClause = selectionClause.concat(segmentRowId + ", ");
            }

        }
        selectionClause = selectionClause.concat(")");

        Uri queryUri = SegmentSchema.SegmentTable.CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();

        @SuppressLint("Recycle")
        Cursor cursor = contentResolver.query(queryUri, null, selectionClause,
                null, null);

        return new SegmentCursor(cursor);

    }


    public static void deleteTrip(Context context, UUID segmentId){

        // delete locations first
        Uri locationQueryUri = LocationSchema.LocationTable.CONTENT_URI;
        String selectionClause = LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " = ?";
        String[] selectionArgs = {segmentId.toString()};

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(locationQueryUri, selectionClause, selectionArgs);

        //delete segment
        Uri segmentQueryUri =  SegmentSchema.SegmentTable.CONTENT_URI;
        selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + " = ?";
        contentResolver.delete(segmentQueryUri, selectionClause, selectionArgs);

    }
}



/* * Unused queries * */

/*
     public static SegmentCursor getSegmentsForDateRange(Context context, long startTime,
                                                        long endTime){
        String[] selectionArgs = {Long.toString(startTime), Long.toString(endTime)};
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " BETWEEN ? AND ? ";
        Uri queryUri = SegmentSchema.SegmentTable.CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(queryUri, null, selectionClause, selectionArgs,
                null);

        return new SegmentCursor(cursor);

    }


     public static Cursor getSegmentLocationFirstLastTimeStamps(Context context, String segmentId){
        String[] selectionArgs = {segmentId};
        String selectionClause = LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " = ?";
        String column = LocationSchema.LocationTable.COLUMN_TIME_STAMP;
        String[] projection = {"MIN(" + column + ")", "MAX("+ column + ") "};

        Uri queryUri = LocationSchema.LocationTable.CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(queryUri, projection, selectionClause, selectionArgs,
                null);

        return cursor;

    }


    public static int updateSegmentDuration(Context context, String segmentId, long duration){
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + " = ?";
        String[] selectionArgs = {segmentId};
        ContentValues updateValues = TrackerBaseHelper.updateSegmentDuration(duration);

        ContentResolver resolver = context.getContentResolver();
        return resolver.update(SegmentSchema.SegmentTable.CONTENT_URI, updateValues,
                selectionClause, selectionArgs);
    }


 */
