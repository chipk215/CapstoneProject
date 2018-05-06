package com.keyeswest.trackme.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import java.util.List;

public class SegmentLoader extends CursorLoader {


    /**
     * Load all segments and order by time stamp
     * @param context
     * @return
     */
    public static SegmentLoader newAllSegmentsInstanceOrderByDate(Context context){
        String orderByClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " DESC ";
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
