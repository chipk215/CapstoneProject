package com.keyeswest.trackme;

import android.content.Context;
import android.content.SharedPreferences;

public class FilterPreferences {

    public static final String FILTER_PREFERENCES = "filterPreferences";
    public static final String SORT_PREFERENCES_KEY = "sortPreferencesKey";
    public static final SortPreference DEFAULT_FILTER = SortPreference.NEWEST;
    public static final String FAVORITE_PREFERENCES_KEY = "favoritePreferencesKey";
    private static final boolean DEFAULT_FAVORITES_ONLY_FILTER = false;

    /**
     * Save default sorting and filtering preferences.
     * @param force - if true overwrite existing preferences, otherwise only save preferences if
     *              they have not previously been saved.
     */
    public static void saveDefaultPreferences(Context context, boolean force){

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (force) {
            // save the default values regardless of what has previously been saved

            editor.putString(SORT_PREFERENCES_KEY, FilterPreferences.DEFAULT_FILTER.getCode());
            editor.putBoolean(FAVORITE_PREFERENCES_KEY, DEFAULT_FAVORITES_ONLY_FILTER);
            editor.commit();
        }else{

            boolean updated = false;
            // don't save defaults if preferences have previously been saved
            if (! sharedPreferences.contains(SORT_PREFERENCES_KEY)){
                editor.putString(SORT_PREFERENCES_KEY, FilterPreferences.DEFAULT_FILTER.getCode());
                updated = true;
            }

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
