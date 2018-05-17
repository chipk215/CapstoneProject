package com.keyeswest.trackme.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.keyeswest.trackme.R;

import timber.log.Timber;

public abstract class ForegroundServiceBase extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    protected static final int NOTIFICATION_ID = 14;

    protected Looper mServiceLooper;
    protected HandlerThread mHandlerThread;

    protected  NotificationCompat.Builder mBuilder;


    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("Entering ForegroundServiceBase onCreate");

        createNotificationChannel();

        mBuilder= new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_track)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.sampling_service_notice))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        startForeground(NOTIFICATION_ID,mBuilder.build() );

        mHandlerThread = new HandlerThread("LocationServiceHandler",
                Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        mServiceLooper = mHandlerThread.getLooper();


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
