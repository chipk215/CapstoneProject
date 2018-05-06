package com.keyeswest.trackme;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.data.LocationSchema;
import com.keyeswest.trackme.data.Queries;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.data.TrackerBaseHelper;
import com.keyeswest.trackme.models.Location;
import com.keyeswest.trackme.models.Segment;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import static com.keyeswest.trackme.data.TrackerBaseHelper.createLocationRecord;
import static com.keyeswest.trackme.data.TrackerBaseHelper.createSegmentRecord;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TracksContentProviderQueryTests extends TracksContentProviderBaseTest {

    private TrackerBaseHelper dbHelper = new TrackerBaseHelper(mContext);
    private SQLiteDatabase database = dbHelper.getWritableDatabase();

    @Test
    public void testSimpleSegmentQueryForAllRecords(){

        // insert 10 records
        List<Segment> segments = insertSegmentRecords(database,10);

        // query for all the segments
        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = resolver.query(
                SegmentSchema.SegmentTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort order to return in Cursor */
                null);

        String queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, cursor != null);

        SegmentCursor segmentCursor = new SegmentCursor(cursor);
        queryFailed = "Query failed to return the correct Cursor";
        assertEquals(queryFailed,segments.size(), cursor.getCount());

        Hashtable<Long, Integer> segmentHash = new Hashtable<>();

        while (segmentCursor.moveToNext()){
            Segment segment = segmentCursor.getSegment();
            long timeStamp = segment.getTimeStamp();
            Assert.assertTrue(timeStamp < 10);
            if (segmentHash.containsKey(timeStamp)){
                int count = segmentHash.get(timeStamp);
                segmentHash.put(timeStamp, count+1);
            }else{
                segmentHash.put(timeStamp,1);
            }
        }

        segmentCursor.close();

        Assert.assertTrue(segmentHash.size() == segments.size());
        Enumeration keys = segmentHash.keys();
        while (keys.hasMoreElements()){
            long key = (long)keys.nextElement();
            int count = segmentHash.get(key);
            org.junit.Assert.assertTrue(count == 1);
        }

    }


    @Test
    public void testSegmentTimeStampBetweenValues(){

        // insert 10 records
        insertSegmentRecords(database,10);

        // query for all the segments
        ContentResolver resolver = mContext.getContentResolver();

        String selectionClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " BETWEEN ? AND ? ";
        // inclusive
        String[] selectionArgs = {"3","6"};

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

        database.close();

        String queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, cursor != null);

        queryFailed = "Query failed to return correct number of records";
        Assert.assertEquals(queryFailed,4, cursor.getCount());

        cursor.close();

    }


    @Test
    public void testRetrievingLocationsBelongingToSegment(){

        // insert 2 segment records
        List<Segment> segments =insertSegmentRecords(database,2);

        // insert 5 location records for each segment
        UUID[] segmentIds = new UUID[2];
        segmentIds[0] = segments.get(0).getId();
        segmentIds[1] = segments.get(1).getId();

        // insert location records
        insertLocationRecords(database,5, segmentIds);

        //query for all of the locations (10)
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(
                LocationSchema.LocationTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort order to return in Cursor */
                null);


        String queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, cursor != null);

        queryFailed = "Query failed to return correct number of records";
        Assert.assertEquals(queryFailed,10, cursor.getCount());


        // query for locations belonging to segment 1
        String selectionClause = LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " = ?";
        String[] selectionArgs = {segmentIds[0].toString()};
        cursor = resolver.query(
                LocationSchema.LocationTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                null);



        queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, cursor != null);

        queryFailed = "Query failed to return correct number of records";
        Assert.assertEquals(queryFailed,5, cursor.getCount());

        // query for locations associated with 2nd segment
        selectionArgs[0] = segmentIds[1].toString();
        cursor = resolver.query(
                LocationSchema.LocationTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                null);

        queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, cursor != null);

        queryFailed = "Query failed to return correct number of records";
        Assert.assertEquals(queryFailed,5, cursor.getCount());

        database.close();
        cursor.close();

    }


    @Test
    public void testLocationQueryBySegmentOrderByTimeWithLimit(){

        int numberSegments = 1;
        int numberLocationsRecords = 5;

        //insert a segment
        List<Segment> segments =insertSegmentRecords(database,numberSegments);
        // insert 5 location records for each segment
        UUID[] segmentIds = new UUID[numberSegments];
        segmentIds[0] = segments.get(0).getId();

        // insert location records
        insertLocationRecords(database, numberLocationsRecords, segmentIds);

        LocationCursor locationCursor = Queries.getLatestLocationBySegmentId(mContext,
                segmentIds[0].toString());

        locationCursor.moveToFirst();
        Location location = locationCursor.getLocation();

        Assert.assertEquals(numberLocationsRecords-1, location.getTimeStamp());

        database.close();
        locationCursor.close();

    }




}
