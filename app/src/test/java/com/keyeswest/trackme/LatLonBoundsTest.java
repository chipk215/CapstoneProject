package com.keyeswest.trackme;

import com.keyeswest.trackme.utilities.LatLonBounds;

import junit.framework.Assert;

import org.junit.Test;

import static java.lang.Math.abs;

public class LatLonBoundsTest {

    private static Double epsilon = 1E-6;

    @Test
    public void computeBoundsTest(){
        Double lat1 = 43.6755846;
        Double lon1 = -116.3092006;

        Double lat2 = 43.6754564;
        Double lon2 = -116.3093951;

        LatLonBounds bounds = new LatLonBounds();

        bounds.update(lat1, lon1);
        bounds.update(lat2, lon2);

        // min lat
        Assert.assertTrue(abs(bounds.getMinLat() - lat2) < epsilon);
        // max lat
        Assert.assertTrue(abs(bounds.getMaxLat() - lat1) < epsilon);

        //min lon
        Assert.assertTrue(abs(bounds.getMinLon() - lon2) < epsilon);
        //max lon
        Assert.assertTrue(abs(bounds.getMaxLon() - lon1) < epsilon);

    }
}
