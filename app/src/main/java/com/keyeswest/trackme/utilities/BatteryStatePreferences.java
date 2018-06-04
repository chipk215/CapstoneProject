package com.keyeswest.trackme.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class BatteryStatePreferences {

    private static final String BATTERY_PREFERENCES = "batteryPreferences";
    private static final String BATTERY_STATE_EXTRA = "batteryStateExtra";

    public static void setLowBatteryLowState(Context context, boolean isLow){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(BATTERY_STATE_EXTRA, isLow);

    }

    public static boolean getLowBatteryLowState(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);
        boolean isLow = sharedPreferences.getBoolean(BATTERY_STATE_EXTRA, false);

       return isLow;

    }





}
