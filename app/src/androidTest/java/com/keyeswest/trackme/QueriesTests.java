package com.keyeswest.trackme;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.data.LocationSchema;
import com.keyeswest.trackme.data.Queries;
import com.keyeswest.trackme.models.Location;
import com.keyeswest.trackme.models.Segment;

import junit.framework.Assert;

import org.junit.Test;

import static com.keyeswest.trackme.data.Queries.getLocationsForSegment;
import static com.keyeswest.trackme.data.TrackerBaseHelper.createLocationRecord;
import static java.lang.Math.abs;

public class QueriesTests extends TracksContentProviderBaseTest {


    // Verify reading a location from a non-existent segment returns an empty cursor
    @Test
    public void noLocationNoSegmentGetTest(){
        LocationCursor locationCursor = Queries.getLatestLocationBySegmentId(mContext,
                "No Segment");
        Assert.assertNotNull(locationCursor);
        int count = locationCursor.getCount();
        Assert.assertEquals(0, count);
    }


    // Verify reading a location from an  empty segment returns an empty cursor
    @Test
    public void noLocationEmptySegmentGetTest(){

        Uri segmentUri = Queries.createNewSegment(mContext);
        Segment segment = Queries.getSegmentFromUri(mContext, segmentUri);
        LocationCursor locationCursor = Queries.getLatestLocationBySegmentId(mContext,
                segment.getId().toString());

        Assert.assertNotNull(locationCursor);
        int count = locationCursor.getCount();
        Assert.assertEquals(0, count);
    }

    @Test
    public void getSegmentBySegmentIdTest(){

        // create a segment
        Uri segmentUri = Queries.createNewSegment(mContext);
        Segment segment = Queries.getSegmentFromUri(mContext, segmentUri);

        // retrieve segment by segmentId
        Segment sameSegment = Queries.getSegmentFromSegmentId(mContext, segment.getId().toString());
        Assert.assertNotNull(sameSegment);
        Assert.assertTrue(segment.getId().equals(sameSegment.getId()));
        Assert.assertEquals(segment.getTimeStamp(), sameSegment.getTimeStamp());
    }

    @Test
    public void getLatestLocationBySegmentIdTest(){

        long locationTimeStamp =1;


        // create a segment
        Uri segmentUri = Queries.createNewSegment(mContext);
        Segment segment = Queries.getSegmentFromUri(mContext, segmentUri);

        // add some locations
        ContentValues locationValues = createLocationRecord(locationTimeStamp,
                45.0d, 60.0d, segment.getId().toString());

        ContentResolver contentResolver = mContext.getContentResolver();

        Uri newLocation =contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI,
                locationValues);

        // retrieve the latest location
        LocationCursor cursor = Queries.getLatestLocationBySegmentId(mContext,
                segment.getId().toString());

        Assert.assertNotNull(cursor);
        Assert.assertTrue(cursor.getCount() == 1);
        cursor.moveToFirst();
        Location location = cursor.getLocation();
        Assert.assertEquals(locationTimeStamp, location.getTimeStamp() );
        cursor.close();


        // insert another location
        locationTimeStamp = 10;

        locationValues = createLocationRecord(locationTimeStamp,
                45.0d, 60.0d, segment.getId().toString());

        contentResolver = mContext.getContentResolver();

        newLocation =contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI,
                locationValues);

        // retrieve the latest location
        cursor = Queries.getLatestLocationBySegmentId(mContext,
                segment.getId().toString());

        Assert.assertNotNull(cursor);
        Assert.assertTrue(cursor.getCount() == 1);
        cursor.moveToFirst();
        location = cursor.getLocation();
        Assert.assertEquals(locationTimeStamp, location.getTimeStamp() );
        cursor.close();


    }


    @Test
    public void getLocationsByJoinWithSegment(){

        double epsilon = 1E-6;
        long timeOne = 1;    long timeTwo = 2;
        long timeThree = 3; long timeFour = 4;

        Double lat1 = 45.0d; Double lon1 = 60.0d;
        Double lat2 = 46.0d; Double lon2 = 61.0d;
        Double lat3 = 47.0d; Double lon3 = 62.0d;
        Double lat4 = 48.0d; Double lon4 = 63.0d;

        // add two segments
        Uri segmentOneUri = Queries.createNewSegment(mContext);
        Segment segmentOne = Queries.getSegmentFromUri(mContext, segmentOneUri);
        Uri segmentTwoUri = Queries.createNewSegment(mContext);
        Segment segmentTwo = Queries.getSegmentFromUri(mContext, segmentTwoUri);

        ContentResolver contentResolver = mContext.getContentResolver();


        // Add one to segment one
        // add some locations
        ContentValues locationValues = createLocationRecord(timeOne,
                lat1, lon1, segmentOne.getId().toString());

        contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI,
                locationValues);


        // add three locations to segment two
        locationValues = createLocationRecord(timeTwo,
                lat2, lon2, segmentTwo.getId().toString());

        contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI,
                locationValues);

        locationValues = createLocationRecord(timeThree,
                lat3, lon3, segmentTwo.getId().toString());

        contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI,
                locationValues);

        locationValues = createLocationRecord(timeFour,
                lat4, lon4, segmentTwo.getId().toString());

        contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI,
                locationValues);

        //=============

        LocationCursor locCursor = getLocationsForSegment(mContext, segmentTwoUri);

        locCursor.moveToFirst();
        Assert.assertEquals(locCursor.getLocation().getTimeStamp(), timeTwo);
        locCursor.moveToNext();
        Assert.assertEquals(locCursor.getLocation().getTimeStamp(), timeThree);
        locCursor.moveToNext();
        Assert.assertEquals(locCursor.getLocation().getTimeStamp(), timeFour);
        Assert.assertTrue(abs(locCursor.getLocation().getLatitude() - lat4) < epsilon);

        locCursor.close();

    }



}
