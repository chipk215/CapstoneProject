package com.keyeswest.trackme;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.keyeswest.trackme.data.Queries;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.models.Segment;

import junit.framework.Assert;

import org.junit.Test;

import java.util.UUID;

import static com.keyeswest.trackme.data.TrackerBaseHelper.createSegmentRecord;
import static java.lang.Math.abs;


public class TracksContentProviderUpdateTests extends TracksContentProviderBaseTest {

    @Test
    public void testUpdateSegmentWhenAddingLocations(){

        // insert a segment
        UUID segmentId = UUID.randomUUID();
        long timeStamp = 1524406840;


        ContentValues segmentValues = createSegmentRecord(segmentId.toString(), timeStamp);
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = contentResolver.insert(SegmentSchema.SegmentTable.CONTENT_URI, segmentValues);
        Assert.assertNotNull(uri);

        //update the segment
        double minLat = 43.0d;
        double maxLat = 44.0d;
        double minLon = -116.0d;
        double maxLon = -115.0d;
        double distance = 10d;
        long elpasedTime = 10L;

        int rowsUpdated = Queries.updateSegmentBoundsDistanceElapsedTime(mContext,segmentId.toString(), minLat, maxLat,
                minLon,maxLon, distance, elpasedTime);

        Assert.assertEquals(1, rowsUpdated);


        Segment segment = Queries.getSegmentFromSegmentId(mContext,segmentId.toString());
        Assert.assertNotNull(segment);

        double epsilon = 1E-6;
        Assert.assertTrue(abs(minLat - segment.getMinLatitude()) <= epsilon);
        Assert.assertTrue(abs(maxLat - segment.getMaxLatitude()) <= epsilon);
        Assert.assertTrue(abs(minLon - segment.getMinLongitude()) <= epsilon);
        Assert.assertTrue(abs(maxLon - segment.getMaxLongitude()) <= epsilon);
        Assert.assertTrue(abs(distance - segment.getDistance()) <= epsilon);


    }
}
