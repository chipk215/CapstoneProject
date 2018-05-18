package com.keyeswest.trackme.services;


import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.keyeswest.trackme.receivers.LocationUpdatesBroadcastReceiver;
import com.keyeswest.trackme.utilities.LocationPreferences;

import timber.log.Timber;

public class FusedLocationService extends LocationService {


    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 3000;

    private LocationCallback mLocationCallback;

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


    /**
     * The current location.
     */
    private Location mLocation;


    public FusedLocationService(){}


    @Override
    public void onCreate() {

        super.onCreate();


        Timber.d("Entering FusedLocationService (debug) onCreate");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

    }



    /**
     * Removes location updates.
     *
     */
    @Override
    public void removeLocationUpdates() {
        Timber.d( "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
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
        startService(new Intent(getApplicationContext(), LocationService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            LocationPreferences.setRequestingLocationUpdates(this, false);
            Timber.e( unlikely,"Lost location permission. Could not request updates." );
        }
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


    // Android defect with adding extras to pending intent requesting location updates
    // https://tinyurl.com/y9gpvcoe
    // As a workaround put the segment id in a shared preferences file

    private PendingIntent getPendingIntent() {

        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);

        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void onNewLocation(Location location) {
        Timber.d("New location: " + location);

        mLocation = location;

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        intent.putExtra(EXTRA_LOCATION, location);
        sendBroadcast(intent);

        // Update notification content if running as a foreground service.
       // if (serviceIsRunningInForeground(this)) {
       //     mNotificationManager.notify(NOTIFICATION_ID, getNotification());
       // }
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Timber.w( "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Timber.e(unlikely, "Lost location permission."  );
        }
    }


}