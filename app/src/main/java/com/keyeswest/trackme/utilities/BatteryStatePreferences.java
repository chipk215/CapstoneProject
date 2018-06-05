package com.keyeswest.trackme.utilities;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

import timber.log.Timber;

public class BatteryStatePreferences {

    public static final String BATTERY_PREFERENCES = "batteryPreferences";
    public static final String BATTERY_STATE_EXTRA = "batteryStateExtra";

    public static final float LOW_BATTERY_THRESHOLD = 0.15f;

    public static void setLowBatteryState(Context context, boolean isLow){
        Timber.d("Setting battery state in shared prefs");
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);
       // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(BATTERY_STATE_EXTRA, isLow);
        editor.commit();

    }

    public static boolean getLowBatteryState(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);
        boolean isLow = sharedPreferences.getBoolean(BATTERY_STATE_EXTRA, false);

       return isLow;

    }


    public static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);
    }


    public static float getCurrentBatteryPercentLevel(Activity activity){
        // Get the current battery level
        IntentFilter currentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = activity.registerReceiver(null, currentFilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPercentage = level / (float)scale;
        return batteryPercentage;
    }


}
