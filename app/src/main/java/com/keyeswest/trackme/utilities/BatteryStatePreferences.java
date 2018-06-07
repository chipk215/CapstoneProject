package com.keyeswest.trackme.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;


import java.util.Objects;

import timber.log.Timber;

public class BatteryStatePreferences {

    public static final String BATTERY_PREFERENCES = "batteryPreferences";
    public static final String BATTERY_STATE_EXTRA = "batteryStateExtra";
    private static final String SERVICE_ABORTED_LOW_BATTERY = "serviceAborted";

    public static final float LOW_BATTERY_THRESHOLD = 0.15f;

    @SuppressLint("ApplySharedPref")
    public static void setLowBatteryState(Context context, boolean isLow){
        Timber.d("Setting battery state in shared prefs");
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(BATTERY_STATE_EXTRA, isLow);
        editor.commit();

    }

    public static boolean getLowBatteryState(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean(BATTERY_STATE_EXTRA, false);

    }



    public static float getCurrentBatteryPercentLevel(Activity activity){
        // Get the current battery level
        IntentFilter currentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = activity.registerReceiver(null, currentFilter);
        int level = Objects.requireNonNull(batteryStatus)
                .getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level / (float)scale;
    }


    /**
     * Sets an abort flag if the location service was stopped while in foreground due to low power
     * state.
     * @param context - context of invoker
     * @param set - true to set; false to clear
     */
    @SuppressLint("ApplySharedPref")
    public static void setServiceAborted(Context context, boolean set){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean( SERVICE_ABORTED_LOW_BATTERY, set);
        editor.commit();

    }

    public static boolean getServiceAborted(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean(SERVICE_ABORTED_LOW_BATTERY, false);

    }


}
