package com.keyeswest.trackme.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;


public class FilterSharedPreferences {

    private static final String FILTER_PREFERENCES = "filterPreferences";
    private static final String FAVORITE_PREFERENCES_KEY = "favoritePreferencesKey";
    private static final String DATE_RANGE_PREFERENCES_KEY = "dateRangePreferencesKey";
    private static final String START_DATE_KEY = "startDateKey";
    private static final String END_DATE_KEY = "endDateKey";


    private static final boolean DEFAULT_FAVORITES_ONLY_FILTER = false;
    private static final boolean DEFAULT_USE_DATE_RANGE = false;


    public static void clearFilters(Context context, boolean force){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (force) {
            // save the default values regardless of what has previously been saved

            editor.putBoolean(FAVORITE_PREFERENCES_KEY, DEFAULT_FAVORITES_ONLY_FILTER);
            editor.putBoolean(DATE_RANGE_PREFERENCES_KEY, DEFAULT_USE_DATE_RANGE);
            editor.commit();
        }else{

            boolean updated = false;
            // don't save defaults if preferences have previously been saved


            if (! sharedPreferences.contains(FAVORITE_PREFERENCES_KEY)){
                editor.putBoolean(FAVORITE_PREFERENCES_KEY, DEFAULT_FAVORITES_ONLY_FILTER);
                updated = true;
            }

            if (! sharedPreferences.contains(DATE_RANGE_PREFERENCES_KEY)){
                editor.putBoolean(DATE_RANGE_PREFERENCES_KEY, DEFAULT_USE_DATE_RANGE);
                updated = true;

            }

            if (updated){
                editor.commit();
            }
        }

    }

    public static void saveFavoriteFilter(Context context, boolean isSelected){
        Timber.d("Saving favorite filter setting to shared prefs: %s", Boolean.toString(isSelected));
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FAVORITE_PREFERENCES_KEY, isSelected);
        editor.commit();

    }

    public static void saveDateRangeFilter(Context context, long startDate, long endDate){

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(DATE_RANGE_PREFERENCES_KEY, true);

        editor.putLong(START_DATE_KEY, startDate);
        editor.putLong(END_DATE_KEY, endDate);
        editor.commit();

    }

    public static boolean getFavoriteFilterSetting(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE);
        boolean isSet = sharedPreferences.getBoolean(FAVORITE_PREFERENCES_KEY, false);
        Timber.d("Retrieving favorite filter setting from shared prefs:  %s", Boolean.toString(isSet));
        return isSet;
    }

    public static boolean isDateRangeSet(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE);

        return sharedPreferences.getBoolean(DATE_RANGE_PREFERENCES_KEY, false);
    }

    public static long getStartDate(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE);

        return sharedPreferences.getLong(START_DATE_KEY,0l);

    }

    public static long getEndDate(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE);

        return sharedPreferences.getLong(END_DATE_KEY,0l);

    }


}
