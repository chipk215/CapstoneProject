package com.keyeswest.trackme.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.keyeswest.trackme.SortPreference;

import java.util.List;

import static com.keyeswest.trackme.TripListFragment.DEFAULT_FILTER;
import static com.keyeswest.trackme.TripListFragment.FILTER_PREFERENCES;
import static com.keyeswest.trackme.TripListFragment.SORT_PREFERENCES_KEY;

public class SegmentLoader extends CursorLoader {



    public static SegmentLoader newAllSegmentsSortedFilteredByPreferences(Context context){

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, context.MODE_PRIVATE);

        String sortByCode = sharedPreferences.getString(SORT_PREFERENCES_KEY, DEFAULT_FILTER.getCode());

        SortPreference sortPreference = SortPreference.lookupByCode(sortByCode);

        switch(sortPreference){
            case NEWEST: return newAllSegmentsInstanceOrderByNewestDate(context);

            case OLDEST: return newAllSegmentsInstanceOrderByOldestDate(context);

            case LONGEST: return newAllSegmentsInstanceOrderByLongestDistance(context);

            case SHORTEST: return newAllSegmentsInstanceOrderByShortestDistance(context);

            default: return newAllSegmentsInstanceOrderByNewestDate(context);
        }

    }


    public static SegmentLoader newAllSegmentsInstanceOrderByShortestDistance(Context context){
        String orderByClause = SegmentSchema.SegmentTable.COLUMN_DISTANCE + " DESC ";
        return new SegmentLoader(context, SegmentSchema.SegmentTable.CONTENT_URI, null,
                null, null, orderByClause);

    }


    public static SegmentLoader newAllSegmentsInstanceOrderByLongestDistance(Context context){
        String orderByClause = SegmentSchema.SegmentTable.COLUMN_DISTANCE + " DESC ";
        return new SegmentLoader(context, SegmentSchema.SegmentTable.CONTENT_URI, null,
                null, null, orderByClause);

    }


    public static SegmentLoader newAllSegmentsInstanceOrderByNewestDate(Context context){
        String orderByClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " DESC ";
        return new SegmentLoader(context, SegmentSchema.SegmentTable.CONTENT_URI, null,
                null, null, orderByClause);

    }

    public static SegmentLoader newAllSegmentsInstanceOrderByOldestDate(Context context){
        String orderByClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " ASC ";
        return new SegmentLoader(context, SegmentSchema.SegmentTable.CONTENT_URI, null,
                null, null, orderByClause);

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
