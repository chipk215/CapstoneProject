package com.keyeswest.trackme;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LocationPermissionTest {

    // Launch the TrackerActivity prior to each test
    @Rule
    public ActivityTestRule<TrackerActivity> mActivityTestRule =
            new ActivityTestRule<>(TrackerActivity.class, false, false);

    @Test
    public void launchMainActivityTest()  {

        mActivityTestRule.launchActivity(null);
    }
}
