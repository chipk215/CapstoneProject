package com.keyeswest.trackme.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

public class SegmentLoader extends CursorLoader {


    public static SegmentLoader newAllSegmentsInstance(Context context){
        return new SegmentLoader(context, SegmentSchema.SegmentTable.CONTENT_URI);

    }

    public static SegmentLoader newSegmentInstance(Context context, Uri segmentUri){
        return new SegmentLoader(context, segmentUri);
    }

    private SegmentLoader(Context context, Uri uri){
        super(context, uri, null, null, null, null);
    }
}
