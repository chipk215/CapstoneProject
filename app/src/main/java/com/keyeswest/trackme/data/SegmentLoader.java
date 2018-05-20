package com.keyeswest.trackme.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.keyeswest.trackme.utilities.SortSharedPreferences;
import com.keyeswest.trackme.SortPreferenceEnum;

import java.util.List;

import timber.log.Timber;

import static com.keyeswest.trackme.utilities.FilterSharedPreferences.getFavoriteFilterSetting;
import static com.keyeswest.trackme.utilities.SortSharedPreferences.SORT_PREFERENCES;
import static com.keyeswest.trackme.utilities.SortSharedPreferences.SORT_PREFERENCES_KEY;


public class SegmentLoader extends CursorLoader {



    public static SegmentLoader newAllSegmentsSortedByPreferences(Context context){

        Timber.d("newAllSegmentsSortedByPreferences invoked");

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SORT_PREFERENCES, context.MODE_PRIVATE);

        String sortByCode = sharedPreferences.getString(SORT_PREFERENCES_KEY,
                SortSharedPreferences.DEFAULT_SORT.getCode());

        String orderByClause;

        SortPreferenceEnum sortPreference = SortPreferenceEnum.lookupByCode(sortByCode);

        switch(sortPreference){
            case NEWEST:
                orderByClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " DESC ";
                break;

            case OLDEST:
                orderByClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " ASC ";
                break;

            case LONGEST:
                orderByClause = SegmentSchema.SegmentTable.COLUMN_DISTANCE + " DESC ";
                break;

            case SHORTEST:
                orderByClause = SegmentSchema.SegmentTable.COLUMN_DISTANCE + " ASC ";
                break;

            default: orderByClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " DESC ";
        }


        // check filter preferences
        String selectionClause = null;
        String[] selectionArgs = null;
        // check for favorite filter
        boolean filterFavorites = getFavoriteFilterSetting(context);
        if (filterFavorites){
            selectionClause = SegmentSchema.SegmentTable.COLUMN_FAVORITE + " = ?";
            selectionArgs = new String[] {Integer.toString(1)};
        }

        return new SegmentLoader(context, SegmentSchema.SegmentTable.CONTENT_URI, null,
                selectionClause, selectionArgs, orderByClause);

    }



    public static SegmentLoader newSegmentsFromUriList(Context context, List<Uri> segments){
        String selectionClause = SegmentSchema.SegmentTable._ID + " IN ( ";
        for (int i=0; i< segments.size(); i++){
            String segmentRowId = segments.get(i).getLastPathSegment();
            if (i== (segments.size() -1 )){
                selectionClause = selectionClause.concat(segmentRowId + " ");
            }else{
                selectionClause = selectionClause.concat(segmentRowId + ", ");
            }

        }
        selectionClause = selectionClause.concat(")");

        Uri queryUri = SegmentSchema.SegmentTable.CONTENT_URI;
        return new SegmentLoader(context, queryUri, null, selectionClause, null, null);
    }

    // request a particular segment
    public static SegmentLoader newSegmentInstance(Context context, Uri segmentUri){
        return new SegmentLoader(context, segmentUri, null, null, null, null);
    }

    private SegmentLoader(Context context, Uri uri, String[] projection, String selection,
                          String[] selectionArgs, String sortOrder){
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }
}
