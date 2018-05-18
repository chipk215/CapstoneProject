package com.keyeswest.trackme.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.android.gms.location.LocationResult;
import com.google.gson.Gson;
import com.keyeswest.trackme.LocationWrapper;
import com.keyeswest.trackme.R;
import com.keyeswest.trackme.utilities.LocationPreferences;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import timber.log.Timber;

import static com.keyeswest.trackme.services.LocationProcessorService.LOCATIONS_EXTRA_KEY;
import static com.keyeswest.trackme.services.LocationProcessorService.SEGMENT_ID_EXTRA_KEY;
import static com.keyeswest.trackme.tasks.StartSegmentTask.SEGMENT_ID_KEY;

public class LocationMockService extends LocationService {

    private static final String UPDATE_EXTRA_KEY = "updateKey";

    private static final int START_CODE = 1;
    private static final int WHAT_CODE = 3;

    private static int sRequestId = 1;

    private static final String MOCK_LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    private LocationMockService.ServiceHandler mServiceHandler;

    private Context mContext;

    private boolean mStopped;

    private Looper mServiceLooper;


    public LocationMockService(){
        mContext = this;
    }

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            Timber.d("Starting mock location updates.");

            SharedPreferences prefs = mContext.getSharedPreferences(SEGMENT_ID_KEY,
                    Context.MODE_PRIVATE);
            String segmentId = prefs.getString(SEGMENT_ID_KEY,null);

            // read the raw resource json file
            InputStream inputStream = getResources().openRawResource(R.raw.track1);
            String jsonString = null;
            Scanner scanner = new Scanner(inputStream);
            try {
                jsonString = scanner.useDelimiter("\\A").next();
            } catch (Exception ex) {
                Timber.e(ex, "Error reading mock track data"); } finally {
                scanner.close();
            }

            LocationWrapper[] locationsWrapped=null;

            if (jsonString != null) {
                Gson gson = new Gson();
                locationsWrapped = gson.fromJson(jsonString, LocationWrapper[].class);
            }

            if (locationsWrapped != null) {
                Timber.d("Mock Location Count= " + Integer.toString(locationsWrapped.length));

                for (LocationWrapper mockSample : locationsWrapped) {

                    double altitude = 100d;
                    float bearing = 0f;
                    float speed = 1f;
                    float accuracy = 5f;

                    Location location = new Location(MOCK_LOCATION_PROVIDER);
                    location.setLatitude(mockSample.getLatitude());
                    location.setLongitude(mockSample.getLongitude());

                    location.setAltitude(altitude);
                    location.setBearing(bearing);
                    location.setSpeed(speed);
                    location.setAccuracy(accuracy);
                    location.setTime(mockSample.getTimeStamp() * 1000);

                    //put the location in a LocationResult container

                    //Yes,  in order to use LocationResult we have to put the single location sample
                    // in a list
                    List<Location> locations = new ArrayList<>();
                    locations.add(location);
                    LocationResult locationResult = LocationResult.create(locations);
                    Intent locationIntent = new Intent(mContext, LocationProcessorService.class);
                    locationIntent.putExtra(LOCATIONS_EXTRA_KEY, locationResult);
                    locationIntent.putExtra(SEGMENT_ID_EXTRA_KEY, segmentId);
                    mContext.startService(locationIntent);


                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    if (mStopped){
                        return;
                    }
                }

            }
        }
    }



    @Override
    public void onCreate() {
        super.onCreate();

        Timber.d("Entering LocationMockService onCreate");

        mServiceLooper = mHandlerThread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mStopped = false;

    }


    @Override
    public void onDestroy() {
        Timber.d("onDestroy invoked");
        mServiceHandler.removeMessages(WHAT_CODE);
        mServiceHandler.removeCallbacks(mHandlerThread);
        super.onDestroy();
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
                this.startForegroundService(new Intent(this, LocationMockService.class));

            }

            startForeground(NOTIFICATION_ID, getNotification());


        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void removeLocationUpdates() {
        Timber.d("Stopping mocked location service");
        LocationPreferences.setRequestingLocationUpdates(this, false);
        mStopped = true;
        stopSelf();
    }

    @Override
    public void requestLocationUpdates() {
        Timber.d("Requesting locations");
        LocationPreferences.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationMockService.class));
        mStopped = false;
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = sRequestId++;
        message.arg2 = START_CODE;

        message.what = WHAT_CODE;
        mServiceHandler.sendMessage(message);

    }

}
