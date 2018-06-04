package com.keyeswest.trackme.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;


public class BatteryLevelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BATTERY_LOW.equalsIgnoreCase(intent.getAction())){
            Timber.d("Received low battery notice");
        }else if (Intent.ACTION_BATTERY_OKAY.equalsIgnoreCase(intent.getAction())){
            Timber.d("Received battery ok notice");
        }

    }
}
