package com.keyeswest.trackme.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.keyeswest.trackme.SortPreferenceEnum;

import static android.content.Context.MODE_PRIVATE;

public class SortSharedPreferences {

    private static final String SORT_PREFERENCES = "sortPreferences";
    private static final String SORT_PREFERENCES_KEY = "sortPreferencesKey";
    private static final SortPreferenceEnum DEFAULT_SORT = SortPreferenceEnum.NEWEST;

    /**
     * Save default sorting preferences.
     * @param force - if true overwrite existing preferences, otherwise only save preferences if
     *              they have not previously been saved.
     */
    @SuppressLint("ApplySharedPref")
    public static void saveDefaultSortPreferences(Context context, boolean force){

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SORT_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (force) {
            // save the default values regardless of what has previously been saved

            editor.putString(SORT_PREFERENCES_KEY, SortSharedPreferences.DEFAULT_SORT.getCode());

            editor.commit();
        }else{

            boolean updated = false;
            // don't save defaults if preferences have previously been saved
            if (! sharedPreferences.contains(SORT_PREFERENCES_KEY)){
                editor.putString(SORT_PREFERENCES_KEY, SortSharedPreferences.DEFAULT_SORT.getCode());
                updated = true;
            }

            if (updated){
                editor.commit();
            }
        }

    }


    public static  String getSortByCode(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SORT_PREFERENCES, MODE_PRIVATE);

        return sharedPreferences.getString(SORT_PREFERENCES_KEY,
                SortSharedPreferences.DEFAULT_SORT.getCode());
    }


    @SuppressLint("ApplySharedPref")
    public static void setSortOrder(Context context, String sortOrder){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SORT_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SORT_PREFERENCES_KEY, sortOrder);
        editor.commit();
    }
}
