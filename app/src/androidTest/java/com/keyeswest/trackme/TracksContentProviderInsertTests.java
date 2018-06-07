package com.keyeswest.trackme;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.SQLException;
import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import com.keyeswest.trackme.data.LocationSchema;
import com.keyeswest.trackme.data.Queries;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.utilities.TestUtilities;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static com.keyeswest.trackme.data.TrackerBaseHelper.createLocationRecord;
import static com.keyeswest.trackme.data.TrackerBaseHelper.createSegmentRecord;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class TracksContentProviderInsertTests  extends TracksContentProviderBaseTest{
    // Test Inserts via ContentResolver

    @Test
    public void testSegmentInsert(){

        UUID segmentId = UUID.randomUUID();
        int timeStamp = 1000;


        // create a segment to insert
        ContentValues testValues = createSegmentRecord(segmentId.toString(), timeStamp);
        TestUtilities.TestContentObserver segmentObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        /* Register a content observer to be notified of changes to data at a given URI (movie) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                SegmentSchema.SegmentTable.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                segmentObserver);

        Uri uri = contentResolver.insert(SegmentSchema.SegmentTable.CONTENT_URI, testValues);
        Assert.assertNotNull(uri);

        /*
         * If this fails, it's likely you didn't call notifyChange in your insert method from
         * your ContentProvider.
         */
        segmentObserver.waitForNotificationOrFail();
        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(segmentObserver);

    }


    @Test
    public void testLocationInSegmentInsert(){

        // insert a segment
        Uri segmentUri = Queries.createNewSegment(mContext);
        Segment segment = Queries.getSegmentFromUri(mContext, segmentUri);
        Assert.assertNotNull(segment);
        String segmentId = segment.getId().toString();

        //insert a location
        ContentValues locationValues = createLocationRecord(segment.getTimeStamp(),
                45.0d, 45.0d,segmentId);

        ContentResolver contentResolver = mContext.getContentResolver();


        TestUtilities.TestContentObserver locationObserver = TestUtilities.getTestContentObserver();
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                LocationSchema.LocationTable.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                locationObserver);


        Uri locationUri = contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI, locationValues);
        Assert.assertNotNull(locationUri);

        locationObserver.waitForNotificationOrFail();
        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(locationObserver);


    }


    @Test
    public void testInsertLocationNullForeignKey(){
        int timeStamp = 1000;

        //insert a location
        ContentValues locationValues = createLocationRecord(timeStamp,
                45.0d, 45.0d,null);

        ContentResolver contentResolver = mContext.getContentResolver();

        Boolean result = FALSE;
        try {
            contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI, locationValues);
        }catch(SQLException e){
            result = TRUE;
        }finally {
            Assert.assertTrue(result);
        }

    }

    @Test
    @Ignore
    public void testInsertInvalidForeignKey(){
        int timeStamp = 1000;


        //insert a location
        ContentValues locationValues = createLocationRecord(timeStamp, 45.0d, 45.0d,"Invalid");

        ContentResolver contentResolver = mContext.getContentResolver();


        Boolean result = FALSE;
        try {
            contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI, locationValues);
            fail("Record with invalid key was inserted into database");
        }catch(SQLException e){
            result = TRUE;
        }finally {
            Assert.assertTrue(result);
        }

    }
}
