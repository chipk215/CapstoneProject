package com.keyeswest.trackme.services;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.keyeswest.trackme.LocationWrapper;
import com.keyeswest.trackme.R;
import com.keyeswest.trackme.receivers.LocationUpdatesBroadcastReceiver;

import java.io.InputStream;
import java.util.Scanner;

import timber.log.Timber;

import static com.keyeswest.trackme.receivers.LocationUpdatesBroadcastReceiver.MOCK_LOCATION_EXTRA_KEY;

public class LocationMockService extends LocationService {

    private static final String UPDATE_EXTRA_KEY = "updateKey";

    private static final int START_CODE = 1;
    private static final int STOP_CODE = 0;
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

            LocationWrapper[] locations=null;

            if (jsonString != null) {
                Gson gson = new Gson();
                locations = gson.fromJson(jsonString, LocationWrapper[].class);
            }

            if (locations != null) {
                Timber.d("Moc Location Count= " + Integer.toString(locations.length));

                for (LocationWrapper mockSample : locations) {

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

                    Intent intent = new Intent(mContext, LocationUpdatesBroadcastReceiver.class);
                    intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_MOCK_UPDATES);

                    intent.putExtra(MOCK_LOCATION_EXTRA_KEY, location);
                    Timber.d("Sending location intent to LocationUpdatesBroadcastReceiver");
                    sendBroadcast(intent);

                    try {
                        Thread.sleep(1000);
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
    public void removeLocationUpdates() {
        mStopped = true;
        stopSelf();
    }

    @Override
    public void requestLocationUpdates() {
        Timber.d("Requesting locations");
        startService(new Intent(getApplicationContext(), LocationMockService.class));
        mStopped = false;
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = sRequestId++;
        message.arg2 = START_CODE;
        mServiceHandler.sendMessage(message);
        message.what = WHAT_CODE;

    }


}
