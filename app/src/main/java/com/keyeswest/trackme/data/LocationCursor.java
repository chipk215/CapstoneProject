package com.keyeswest.trackme.data;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.keyeswest.trackme.models.Location;

import java.util.UUID;

public class LocationCursor extends CursorWrapper {

    public LocationCursor(Cursor cursor) {
        super(cursor);
    }


    public Location getLocation(){

        Location location = new Location();
        long timeStamp = getLong(getColumnIndex(LocationSchema.LocationTable.COLUMN_TIME_STAMP));

        UUID segmentId = UUID.fromString(getString(getColumnIndex(
                LocationSchema.LocationTable.COLUMN_SEGMENT_ID)));

        double latitude = getDouble(getColumnIndex(LocationSchema.LocationTable.COLUMN_LATITUDE));

        double longitude = getDouble(getColumnIndex(LocationSchema.LocationTable.COLUMN_LONGITUDE));

        location.setLongitude(longitude);
        location.setLatitude(latitude);
        location.setTimeStamp(timeStamp);
        location.setSegmentId(segmentId);

        return location;

    }
}
