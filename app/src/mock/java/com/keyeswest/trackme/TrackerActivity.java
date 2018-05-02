package com.keyeswest.trackme;

import android.content.Intent;

import com.keyeswest.trackme.services.LocationMockService;

import timber.log.Timber;

public class TrackerActivity extends TrackerBaseActivity {

    @Override
    protected void startUpdates() {
        Intent intent = LocationMockService.getStartUpdatesIntent(this);
        startService(intent);
    }

    @Override
    protected void stopUpdates() {
        Timber.d("Stopping Mock Location Service");
        Intent intent = LocationMockService.getStopUpdatesIntent(this);
        startService(intent);
    }


}
