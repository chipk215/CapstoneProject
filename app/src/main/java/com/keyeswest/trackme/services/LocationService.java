package com.keyeswest.trackme.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.keyeswest.trackme.PermissionActivity;
import com.keyeswest.trackme.R;

import timber.log.Timber;

public abstract class LocationService extends Service {

    private static final String PACKAGE_NAME =
            "com.keyeswest.trackme.services.LocationService";

    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";


    public static final String NOTIFICATION_CHANNEL_ID = "channel_01";

    protected static final int NOTIFICATION_ID = 14;

    private final IBinder mBinder = new LocalBinder();

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    protected boolean mChangingConfiguration = false;


    protected NotificationManager mNotificationManager;


    public abstract void removeLocationUpdates();

    public abstract void requestLocationUpdates();

    protected abstract Intent getNotificationIntent();

    protected HandlerThread mHandlerThread;


    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("Entering LocationService onCreate");

        mHandlerThread = new HandlerThread("LocationServiceHandler");
        mHandlerThread.start();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();

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





    protected void createNotificationChannel() {
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

            // how will
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
        //mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    protected Notification getNotification(){
        Intent intent = getNotificationIntent();

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, PermissionActivity.class), 0);

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
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
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
