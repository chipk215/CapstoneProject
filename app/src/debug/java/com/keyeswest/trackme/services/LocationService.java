package com.keyeswest.trackme.services;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.keyeswest.trackme.receivers.LocationUpdatesBroadcastReceiver;

import timber.log.Timber;

public class LocationService extends Service {


    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 3000;

    private static final String UPDATE_EXTRA_KEY = "updateKey";


    private static final int START_CODE = 1;
    private static final int STOP_CODE = 0;


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

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private Context mContext;


    public LocationService(){
        mContext = this;
    }

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper){
            super(looper);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void handleMessage(Message msg) {
            Timber.d("LocationService handle message...");

            if ((msg.arg2 == START_CODE) && (mFusedLocationClient == null)){

                Timber.d("Starting location updates.");

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
                createLocationRequest();

                // permission was garnered prior to service being started
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());


            }else if ((msg.arg2 == STOP_CODE) && (mFusedLocationClient != null) ){

                Timber.d("Stopping location updates.");

                mFusedLocationClient.removeLocationUpdates(getPendingIntent());
                mFusedLocationClient = null;
                // notify location manager to stop updates
                stopSelf();
                return;
            }

            // should not reach here since LocationServices run until stopped
            stopSelf(msg.arg1);
        }

    }


    public static Intent getStartUpdatesIntent(Context context){
        Intent intent = new Intent(context, LocationService.class);
        intent.putExtra(UPDATE_EXTRA_KEY, START_CODE);
        return intent;
    }

    public static Intent getStopUpdatesIntent(Context context){
        Intent intent = new Intent(context, LocationService.class);
        intent.putExtra(UPDATE_EXTRA_KEY, STOP_CODE);
        return intent;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("Entering LocationService onCreate");

        HandlerThread thread = new HandlerThread("LocationServiceHandler",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("Entering LocationService onCreate");

        Message message = mServiceHandler.obtainMessage();
        message.arg1 = startId;

        if (intent != null){
            message.arg2 = intent.getIntExtra(UPDATE_EXTRA_KEY, STOP_CODE);
        }else{

            // an unexpected error state, just log and stop the service
            Timber.e("Unexpected or missing intent data in LocationService onStartCommand");
            message.arg2 = STOP_CODE;
        }

        mServiceHandler.sendMessage(message);


        return START_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
}