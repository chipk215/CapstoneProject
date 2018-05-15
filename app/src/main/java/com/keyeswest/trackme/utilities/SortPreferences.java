package com.keyeswest.trackme.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.keyeswest.trackme.SortPreferenceEnum;

public class SortPreferences {

    public static final String SORT_PREFERENCES = "sortPreferences";
    public static final String SORT_PREFERENCES_KEY = "sortPreferencesKey";
    public static final SortPreferenceEnum DEFAULT_SORT = SortPreferenceEnum.NEWEST;

    /**
     * Save default sorting preferences.
     * @param force - if true overwrite existing preferences, otherwise only save preferences if
     *              they have not previously been saved.
     */
    public static void saveDefaultSortPreferences(Context context, boolean force){

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SORT_PREFERENCES, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (force) {
            // save the default values regardless of what has previously been saved

            editor.putString(SORT_PREFERENCES_KEY, SortPreferences.DEFAULT_SORT.getCode());

            editor.commit();
        }else{

            boolean updated = false;
            // don't save defaults if preferences have previously been saved
            if (! sharedPreferences.contains(SORT_PREFERENCES_KEY)){
                editor.putString(SORT_PREFERENCES_KEY, SortPreferences.DEFAULT_SORT.getCode());
                updated = true;
            }


            if (updated){
                editor.commit();
            }
        }

    }
}
