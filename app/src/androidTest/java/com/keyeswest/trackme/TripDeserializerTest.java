package com.keyeswest.trackme;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.models.Trip;
import com.keyeswest.trackme.utilities.TripDeserializer;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.Scanner;

import timber.log.Timber;

@RunWith(AndroidJUnit4.class)
public class TripDeserializerTest {

    // Context used to access various parts of the system
    protected final Context mContext = InstrumentationRegistry.getTargetContext();

    @Test
    public void deserializeTripsTest(){
        Trip[] trips = TripDeserializer.readJson(mContext);

        Assert.assertNotNull(trips);
        Segment segment = trips[0].getSegment();
        Assert.assertNotNull(segment);
        Assert.assertTrue(segment.getId().toString().equals("27903b5e-65dc-11e8-adc0-fa7ae01bbebc"));


    }
}
