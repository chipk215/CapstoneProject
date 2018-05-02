package com.keyeswest.trackme;

import android.content.Intent;

import com.keyeswest.trackme.services.LocationService;

public class TrackerActivity extends TrackerBaseActivity {

    @Override
    protected void startUpdates() {
        Intent intent = LocationService.getStartUpdatesIntent(this);
        startService(intent);
    }

    @Override
    protected void stopUpdates() {
        Intent intent = LocationService.getStopUpdatesIntent(this);
        startService(intent);
    }


}

