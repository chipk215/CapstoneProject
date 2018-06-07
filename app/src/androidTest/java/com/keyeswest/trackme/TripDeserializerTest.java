package com.keyeswest.trackme;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;


import com.keyeswest.trackme.models.Location;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.models.Trip;
import com.keyeswest.trackme.utilities.TripDeserializer;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class TripDeserializerTest {

    // Context used to access various parts of the system
    private final Context mContext = InstrumentationRegistry.getTargetContext();

    @Test
    public void deserializeTripsTest(){
        Trip[] trips = TripDeserializer.readJson(mContext);

        String expectedSegmentId = "27903b5e-65dc-11e8-adc0-fa7ae01bbebc";
        long expectedSegmentTimeStamp = 1524406854;
        long expectedDuration = 1105000;
        double expectedMinLat = 43.6754564;
        double expectedMinLon = -116.35398;
        double expectedMaxLat = 43.6935548;
        double expectedMaxLon = -116.3092006;
        int expectedLocationCount = 222;

        Assert.assertNotNull(trips);
        Segment segment = trips[0].getSegment();
        Assert.assertNotNull(segment);
        Assert.assertTrue(segment.getId().toString().equals(expectedSegmentId));
        Assert.assertEquals(expectedSegmentTimeStamp, segment.getTimeStamp());
        Assert.assertEquals(expectedDuration, segment.getElapsedTime());
        Assert.assertEquals(expectedMaxLat, segment.getMaxLatitude());
        Assert.assertEquals(expectedMinLat, segment.getMinLatitude());
        Assert.assertEquals(expectedMaxLon, segment.getMaxLongitude());
        Assert.assertEquals(expectedMinLon, segment.getMinLongitude());

        Location[] locations = trips[0].getLocations();
        Assert.assertNotNull(locations);
        Assert.assertEquals(expectedLocationCount, locations.length);


    }
}
