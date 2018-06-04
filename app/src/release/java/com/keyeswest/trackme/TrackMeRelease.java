package com.keyeswest.trackme;

import android.app.Application;

import timber.log.Timber;

public class TrackMeRelease extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        Timber.plant(new CrashlyticsTree());
    }
}
