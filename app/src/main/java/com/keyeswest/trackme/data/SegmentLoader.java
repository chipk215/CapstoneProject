package com.keyeswest.trackme.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

public class SegmentLoader extends CursorLoader {


    public static SegmentLoader newAllSegmentsInstanceOrderByDate(Context context){
        String orderByClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " DESC ";
        return new SegmentLoader(context, SegmentSchema.SegmentTable.CONTENT_URI, orderByClause);

    }

    // request a particular segment
    public static SegmentLoader newSegmentInstance(Context context, Uri segmentUri){
        return new SegmentLoader(context, segmentUri, null);
    }

    private SegmentLoader(Context context, Uri uri, String sortOrder){
        super(context, uri, null, null, null, null);
    }
}
