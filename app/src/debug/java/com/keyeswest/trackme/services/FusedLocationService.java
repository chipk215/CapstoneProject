package com.keyeswest.trackme.services;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.keyeswest.trackme.utilities.LocationPreferences;

import timber.log.Timber;

import static com.keyeswest.trackme.services.LocationProcessorService.LOCATIONS_EXTRA_KEY;
import static com.keyeswest.trackme.services.LocationProcessorService.SEGMENT_ID_EXTRA_KEY;
import static com.keyeswest.trackme.tasks.StartSegmentTask.SEGMENT_ID_KEY;

public class FusedLocationService extends LocationService {


    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 3000;

    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL_IN_MILLISECONDS ;

    /**
     * Provides the entry point to the Fused Location Provider API.
     *
     * Note: This service is designed to wrap a single instance of a FusedLocationProviderClient.
     *       The service ignores consecutive start requests and stops updates on the singleton
     *       FusedLocationProviderClient when the invoking service client issues a stop update
     *       intent.
     */
    private static FusedLocationProviderClient mFusedLocationClient;

    private LocationRequest mLocationRequest;


    public FusedLocationService(){}


    @Override
    public void onCreate() {

        super.onCreate();
        Timber.d("Entering FusedLocationService (debug) onCreate");

        mServiceHandler = new Handler(mHandlerThread.getLooper());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();


    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.d( "Last client unbound from service ");

        // Called when the last client unbinds from this
        // service. If this method is called due to a configuration change, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && LocationPreferences.requestingLocationUpdates(this)) {
            Timber.d( "Starting foreground service");


            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                this.startForegroundService(new Intent(this, FusedLocationService.class));

            }

            startForeground(NOTIFICATION_ID, getNotification());


        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }



    /**
     * Removes location updates.
     *
     */
    @Override
    public void removeLocationUpdates() {
        Timber.d( "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Timber.d("Locations updates successfully stopped.");
                    }else{
                        Timber.d("Locations updates failed to stop.");
                    }
                }
            });
            LocationPreferences.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            LocationPreferences.setRequestingLocationUpdates(this, true);
            Timber.e(unlikely, "Lost location permission. Could not remove updates. " );
        }
    }

    @Override
    public void requestLocationUpdates() {
        Timber.d("Requesting location updates");
        LocationPreferences.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), FusedLocationService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            LocationPreferences.setRequestingLocationUpdates(this, false);
            Timber.e( unlikely,"Lost location permission. Could not request updates." );
        }
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }



    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }




    private void onNewLocation(Location location) {
        Timber.d("New location: " + location);

        SharedPreferences prefs = this.getSharedPreferences(SEGMENT_ID_KEY,
                Context.MODE_PRIVATE);
        String segmentId = prefs.getString(SEGMENT_ID_KEY,null);


        Intent locationIntent = new Intent(this, LocationProcessorService.class);
        locationIntent.putExtra(LOCATIONS_EXTRA_KEY, location);
        locationIntent.putExtra(SEGMENT_ID_EXTRA_KEY, segmentId);
        this.startService(locationIntent);

        // Notify anyone listening for broadcasts about the new location.
      //  Intent intent = new Intent(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
      //  intent.putExtra(EXTRA_LOCATION, location);
      //  sendBroadcast(intent);

        // Update notification content if running as a foreground service.
       // if (serviceIsRunningInForeground(this)) {
       //     mNotificationManager.notify(NOTIFICATION_ID, getNotification());
       // }
    }




}