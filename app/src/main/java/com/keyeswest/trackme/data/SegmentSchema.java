package com.keyeswest.trackme.data;

import android.net.Uri;

public class SegmentSchema {
    public static final String AUTHORITY = "com.keyeswest.trackme";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_SEGMENT = SegmentSchema.SegmentTable.TABLE_NAME;

    public static final class SegmentTable{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEGMENT).build();

        public static Uri buildItemUri(long _id){
            return CONTENT_URI.buildUpon().appendPath(Long.toString(_id)).build();
        }

        public static final String TABLE_NAME = "segment";
        public static final String _ID = "_id";
        public static final String COLUMN_ID = "segment_id";
        public static final String COLUMN_TIME_STAMP = "timestamp";
        public static final String COLUMN_MOCKED = "mocked";
        public static final String COLUMN_MIN_LAT = "minLat";
        public static final String COLUMN_MAX_LAT = "maxLat";
        public static final String COLUMN_MIN_LON = "minLon";
        public static final String COLUMN_MAX_LON = "maxLon";
        public static final String COLUMN_DISTANCE = "distance";

    }
}
