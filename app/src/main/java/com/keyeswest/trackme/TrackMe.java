package com.keyeswest.trackme;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import com.keyeswest.trackme.receivers.BatteryLevelReceiver;




@SuppressLint("Registered")
public abstract class  TrackMe extends Application {

    private BatteryLevelReceiver mBatteryLevelReceiver = new BatteryLevelReceiver();
    @Override
    public void onCreate(){
        super.onCreate();

        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        // FYI - Not able to get receiver to work when registering by manifest.
        registerReceiver(mBatteryLevelReceiver, filter);
    }


}