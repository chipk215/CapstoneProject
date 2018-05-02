package com.keyeswest.trackme.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.keyeswest.trackme.models.Segment;

import java.util.UUID;

import timber.log.Timber;

import static com.keyeswest.trackme.data.TrackerBaseHelper.createLocationRecord;
import static com.keyeswest.trackme.data.TrackerBaseHelper.createSegmentRecord;
import static com.keyeswest.trackme.data.TrackerBaseHelper.updateSegmentRecord;
import static com.keyeswest.trackme.data.TracksContentProvider.CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS;

public class Queries {

    public static LocationCursor getLatestLocationBySegmentId(Context context, String segmentId){

        String[] selectionArgs = {segmentId};
        String selectionClause = LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " = ?";
        String orderByClause = LocationSchema.LocationTable.COLUMN_TIME_STAMP + " DESC ";

        Uri queryUri = LocationSchema.LocationTable.CONTENT_URI;
        queryUri = queryUri.buildUpon().appendQueryParameter("limit","1").build();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                queryUri,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                orderByClause);

        if (cursor != null){
            LocationCursor locationCursor = new LocationCursor(cursor);
            return locationCursor;
        }

        return null;

    }


    /***
     * Inserts a new segment.
     * @param context
     * @return segmentId of the new segment
     */
    public static Uri createNewSegment(Context context) {

        String segmentId = UUID.randomUUID().toString();
        // timestamp in seconds
        long timeStamp = System.currentTimeMillis() / 1000;
        int mocked = 0;
        ContentValues values = createSegmentRecord(segmentId, timeStamp, mocked);

        ContentResolver contentResolver = context.getContentResolver();

        try {
            Uri segment = contentResolver.insert(SegmentSchema.SegmentTable.CONTENT_URI, values);
            return segment;
        } catch (Exception ex) {
            Timber.e(ex, "Exception raised inserting segment to db.");
            return null;
        }

    }


    public static Segment getSegmentFromUri(Context context, Uri uri){

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                uri,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort order to return in Cursor */
                null);
        if (cursor != null){
            SegmentCursor segmentCursor = new SegmentCursor(cursor);
            segmentCursor.moveToNext();
            Segment segment = segmentCursor.getSegment();
            return segment;
        }

        return null;

    }


    public static Segment getSegmentFromSegmentId(Context context, String segmentId){

        String[] selectionArgs = {segmentId};
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + "= ?";
        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(
                 SegmentSchema.SegmentTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                null);

        if (cursor != null){
            SegmentCursor segmentCursor = new SegmentCursor(cursor);
            segmentCursor.moveToFirst();
            Segment segment = segmentCursor.getSegment();
            segmentCursor.close();
            return segment;
        }

        return null;

    }


    public static int updateSegment(Context context,String segmentId,
                                        double minLat, double maxLat,
                                        double minLon, double maxLon,
                                        double distance){

        //update the segment
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + " = ?";
        String[] selectionArgs = {segmentId.toString()};
        ContentValues updateValues = updateSegmentRecord(minLat, maxLat, minLon, maxLon, distance);
        ContentResolver resolver = context.getContentResolver();
        int rowsUpdated =resolver.update(SegmentSchema.SegmentTable.CONTENT_URI, updateValues,
                selectionClause, selectionArgs);

        return rowsUpdated;

    }


    public static Uri createNewLocationFromSample(Context context,
                                                  android.location.Location sample,
                                                  String segmentId){

        ContentResolver contentResolver = context.getContentResolver();

        long epochTimeSec = sample.getTime();
        ContentValues locationValues = createLocationRecord(epochTimeSec,
                sample.getLatitude(), sample.getLongitude(), segmentId);

        Uri newLocation =contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI,
                locationValues);

        return newLocation;

    }

    public static LocationCursor getLocationsForSegment(Context context, Uri segmentUri){
        String segmentRowId = segmentUri.getLastPathSegment();
        Uri requestUri = CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS;
        requestUri = requestUri.buildUpon().appendPath(segmentRowId).build();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(requestUri, null, null,
                null, null);

        LocationCursor locCursor = new LocationCursor(cursor);
        return locCursor;
    }
}
