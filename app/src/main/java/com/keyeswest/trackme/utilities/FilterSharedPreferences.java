package com.keyeswest.trackme.utilities;

import android.content.Context;
import android.content.SharedPreferences;


public class FilterSharedPreferences {

    public static final String FILTER_PREFERENCES = "filterPreferences";
    public static final String FAVORITE_PREFERENCES_KEY = "favoritePreferencesKey";
    private static final boolean DEFAULT_FAVORITES_ONLY_FILTER = false;

    public static void clearFilters(Context context, boolean force){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (force) {
            // save the default values regardless of what has previously been saved

            editor.putBoolean(FAVORITE_PREFERENCES_KEY, DEFAULT_FAVORITES_ONLY_FILTER);
            editor.commit();
        }else{

            boolean updated = false;
            // don't save defaults if preferences have previously been saved


            if (! sharedPreferences.contains(FAVORITE_PREFERENCES_KEY)){
                editor.putBoolean(FAVORITE_PREFERENCES_KEY, DEFAULT_FAVORITES_ONLY_FILTER);
                updated = true;
            }

            if (updated){
                editor.commit();
            }
        }

    }

}
