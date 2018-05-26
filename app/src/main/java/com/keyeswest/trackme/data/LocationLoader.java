package com.keyeswest.trackme.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import timber.log.Timber;

import static com.keyeswest.trackme.data.TracksContentProvider.CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS;


public class LocationLoader extends CursorLoader {

    public static LocationLoader newLocationsForSegment(Context context, Uri segmentUri){

        String segmentRowId = segmentUri.getLastPathSegment();
        Uri requestUri = CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS;
        requestUri = requestUri.buildUpon().appendPath(segmentRowId).build();

        return new LocationLoader(context, requestUri);
    }

    public static LocationLoader getLocationsForSegmentByRowId(Context context, long rowId){
        Timber.d("entering getLocationsForSegmentByRowId");
        String segmentRowId = Long.toString(rowId);
        Uri requestUri = CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS;

        requestUri = requestUri.buildUpon().appendPath(segmentRowId).build();
        Timber.d("Join request Uri: " + requestUri.toString());
        return new LocationLoader(context, requestUri);
    }

    private LocationLoader(Context context, Uri uri){
        super(context, uri, null, null, null, null);
    }
}
