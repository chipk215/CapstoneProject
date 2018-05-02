package com.keyeswest.trackme.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import timber.log.Timber;


public class TracksContentProvider extends ContentProvider {

    // Location data
    public static final int LOCATION_DIRECTORY = 100;
    public static final int LOCATION_WITH_ID = 101;
    public static final int LOCATION_FROM_JOIN = 102;


    //Segment data
    public static final int SEGMENT_DIRECTORY = 200;
    public static final int SEGMENT_WITH_ID = 201;



    private static final String PATH_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS =
            "relationship_join_segment_get_locations";

    public static final Uri CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS =
            Uri.parse(LocationSchema.BASE_CONTENT_URI + "/" +
                    PATH_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS);


    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Location queries
        matcher.addURI(LocationSchema.AUTHORITY,
                LocationSchema.PATH_LOCATION, LOCATION_DIRECTORY);

        matcher.addURI(LocationSchema.AUTHORITY,
                LocationSchema.PATH_LOCATION + "/#", LOCATION_WITH_ID);


        // Segment queries
        matcher.addURI(SegmentSchema.AUTHORITY, SegmentSchema.PATH_SEGMENT, SEGMENT_DIRECTORY);
        matcher.addURI(SegmentSchema.AUTHORITY, SegmentSchema.PATH_SEGMENT + "/#",
                SEGMENT_WITH_ID);

        // segment location join query
        // e.g. content://com.keyeswest.fleettracker/relationship_join_segment_get_locations/id
        //  where id is the row id (_id) of the segment to join on
        matcher.addURI(LocationSchema.AUTHORITY, PATH_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS +
        "/#",LOCATION_FROM_JOIN);


        return matcher;
    }

    private TrackerBaseHelper mTrackerBaseHelper;


    @Override
    public boolean onCreate() {
        Context context = getContext();
        mTrackerBaseHelper = new TrackerBaseHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder
                        ) {

        final SQLiteDatabase db = mTrackerBaseHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor cursor;
        switch (match){
            case LOCATION_DIRECTORY:
                String limit = uri.getQueryParameter("limit");

                cursor = db.query(LocationSchema.LocationTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        limit);

                break;
            case SEGMENT_DIRECTORY:
                cursor = db.query(SegmentSchema.SegmentTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case SEGMENT_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};

                cursor = db.query(SegmentSchema.SegmentTable.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case LOCATION_FROM_JOIN:
                String segmentId = uri.getPathSegments().get(1);

                String[] args = new String[]{segmentId};
                cursor = db.rawQuery(
                        "SELECT l.latitude, l.longitude, l.timestamp, l.segmentId  " +
                                "FROM location l " +
                                "INNER JOIN segment s ON s.segment_id = l.segmentid " +
                                "WHERE s._id = ?", args);

                break;

            default:

                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mTrackerBaseHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Uri returnUri=null;

        switch(match){
            case LOCATION_DIRECTORY:

                try {
                    long locationId = db.insertOrThrow(LocationSchema.LocationTable.TABLE_NAME, null, values);
                    if (locationId > 0){
                        returnUri = ContentUris.withAppendedId(LocationSchema.LocationTable.CONTENT_URI, locationId);
                    }
                }catch(SQLException ex){
                    Timber.e(ex, "Failed to insert record");
                    throw(ex);

                }

                break;
            case SEGMENT_DIRECTORY:
                long segmentId = db.insert(SegmentSchema.SegmentTable.TABLE_NAME, null, values);
                if (segmentId > 0){
                    returnUri = ContentUris.withAppendedId(SegmentSchema.SegmentTable.CONTENT_URI, segmentId);
                }else{
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported insert operation.");
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;


    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mTrackerBaseHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int rowsUpdated = 0;

        switch (match){
            case SEGMENT_DIRECTORY:

                rowsUpdated = db.update(SegmentSchema.SegmentTable.TABLE_NAME, values, selection,  selectionArgs);

                break;
            default:
                throw new UnsupportedOperationException("Not implemented");
        }

        if (rowsUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
