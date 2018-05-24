package com.keyeswest.trackme.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.keyeswest.trackme.utilities.SortSharedPreferences;
import com.keyeswest.trackme.SortPreferenceEnum;

import java.util.List;

import timber.log.Timber;

import static com.keyeswest.trackme.utilities.FilterSharedPreferences.getEndDate;
import static com.keyeswest.trackme.utilities.FilterSharedPreferences.getFavoriteFilterSetting;
import static com.keyeswest.trackme.utilities.FilterSharedPreferences.getStartDate;
import static com.keyeswest.trackme.utilities.FilterSharedPreferences.isDateRangeSet;
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

        String dateRangeSelectionClause=null;
        String [] dateRangeArgs= null;
        boolean dateRangeSelected = isDateRangeSet(context);
        long startDate;
        long endDate;
        if (dateRangeSelected){
            startDate = getStartDate(context);
            endDate = getEndDate(context);

            dateRangeSelectionClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP +
                    " BETWEEN ? AND ? ";
            dateRangeArgs = new String[] {Long.toString(startDate), Long.toString(endDate)};
        }


        String favoriteSelectionClause = null;
        String[] favoriteSelectionArgs = null;
        boolean filterFavorites = getFavoriteFilterSetting(context);
        if (filterFavorites){
            favoriteSelectionClause = SegmentSchema.SegmentTable.COLUMN_FAVORITE + " = ?";
            favoriteSelectionArgs = new String[] {Integer.toString(1)};
        }


        String selectionClause;
        String[] selectionArgs;
        if (dateRangeSelected && filterFavorites){
            // both date range and favorite filter
            selectionClause = dateRangeSelectionClause + " AND " + favoriteSelectionClause;
            selectionArgs = new String[] {dateRangeArgs[0], dateRangeArgs[1],
                    favoriteSelectionArgs[0]};

        }else if (dateRangeSelected && ! filterFavorites){
            // date range and no favorite filter
            selectionClause = dateRangeSelectionClause;
            selectionArgs = dateRangeArgs;

        }else if(filterFavorites && ! dateRangeSelected){
            // favorites only no date range
            selectionClause = favoriteSelectionClause;
            selectionArgs = favoriteSelectionArgs;
        }else{
            // no filter just sort
            selectionClause = null;
            selectionArgs = null;
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
