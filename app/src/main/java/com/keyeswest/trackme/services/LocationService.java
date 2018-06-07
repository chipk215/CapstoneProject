package com.keyeswest.trackme.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.keyeswest.trackme.NewTripActivity;
import com.keyeswest.trackme.R;

import timber.log.Timber;

import static com.keyeswest.trackme.utilities.BatteryStatePreferences.BATTERY_PREFERENCES;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.BATTERY_STATE_EXTRA;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.getLowBatteryState;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.setServiceAborted;
import static com.keyeswest.trackme.utilities.LocationPreferences.setRequestingLocationUpdates;

public abstract class LocationService extends Service
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PACKAGE_NAME =
            "com.keyeswest.trackme.services.LocationService";

    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";


    private static final String NOTIFICATION_CHANNEL_ID = "channel_01";

    static final int NOTIFICATION_ID = 14;

    private final IBinder mBinder = new LocalBinder();

    private SharedPreferences mBatteryPreferences;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    boolean mChangingConfiguration = false;


    private NotificationManager mNotificationManager;

    public abstract void removeLocationUpdates();

    public abstract void requestLocationUpdates();

    protected abstract Intent getNotificationIntent();

    HandlerThread mHandlerThread;


    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("Entering LocationService onCreate");

        mHandlerThread = new HandlerThread("LocationServiceHandler");
        mHandlerThread.start();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();

        // register for changes in low battery state messages
        mBatteryPreferences = this.getSharedPreferences(BATTERY_PREFERENCES,
                Context.MODE_PRIVATE);

        if (mBatteryPreferences != null) {
            Timber.d("registering for shared prefs notification");
            mBatteryPreferences.registerOnSharedPreferenceChangeListener(this);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client  comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Timber.d("client is binding to location service.");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client  returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Timber.d( "client is rebinding to location service.");
        stopForeground(true);
        mChangingConfiguration = false;

    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name,
                    NotificationManager.IMPORTANCE_DEFAULT);

            // Register the channel with the system; you can't change the importance

            mNotificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d( "Location Service started via onStartCommand");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            Timber.d("User stopped tracking from notification");
            removeLocationUpdates();

            // update location tracking state
            setRequestingLocationUpdates(this, false);
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public void onDestroy() {
        if (mBatteryPreferences != null) {
            mBatteryPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        super.onDestroy();

    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    Notification getNotification(){
        Intent intent = getNotificationIntent();

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        Intent activityIntent = new Intent(this, NewTripActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                activityIntent, 0);

        //TODO change text to contain last location update info
        CharSequence text = getResources().getString(R.string.sampling_service_notice);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_stat_track)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        return builder.build();

    }


    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    protected boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);

        if (manager == null){
            return false;
        }

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(BATTERY_STATE_EXTRA)){

            if (getLowBatteryState(this)){

                setServiceAborted(this, true);

                if (serviceIsRunningInForeground(this)){

                    // NewTri[Activity handles low battery state if service not in foreground
                    removeLocationUpdates();
                }

            }

        }
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }
}
