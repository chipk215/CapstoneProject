package com.keyeswest.trackme.data;

import android.net.Uri;

public class LocationSchema {

    public static final String AUTHORITY = "com.keyeswest.trackme";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_LOCATION = LocationTable.TABLE_NAME;

    public static final class LocationTable{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String TABLE_NAME = "location";
        public static final String COLUMN_TIME_STAMP = "timestamp";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_SEGMENT_ID = "segmentid";

    }

}
