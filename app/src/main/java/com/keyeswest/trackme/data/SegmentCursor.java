package com.keyeswest.trackme.data;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.keyeswest.trackme.models.Segment;

import java.util.UUID;


public class SegmentCursor extends CursorWrapper {

    public SegmentCursor(Cursor cursor){ super(cursor);}

    public Segment getSegment() {

        String segmentId = getString(getColumnIndex(SegmentSchema.SegmentTable.COLUMN_ID));
        long timeStamp = getLong(getColumnIndex(SegmentSchema.SegmentTable.COLUMN_TIME_STAMP));
        int mocked = getInt(getColumnIndex(SegmentSchema.SegmentTable.COLUMN_MOCKED));
        Double distance = getDouble(getColumnIndex(SegmentSchema.SegmentTable.COLUMN_DISTANCE));
        Double minLat = getDouble (getColumnIndex(SegmentSchema.SegmentTable.COLUMN_MIN_LAT));
        Double maxLat = getDouble (getColumnIndex(SegmentSchema.SegmentTable.COLUMN_MAX_LAT));
        Double minLon = getDouble (getColumnIndex(SegmentSchema.SegmentTable.COLUMN_MIN_LON));
        Double maxLon = getDouble (getColumnIndex(SegmentSchema.SegmentTable.COLUMN_MAX_LON));
        Long rowId = getLong(getColumnIndex(SegmentSchema.SegmentTable._ID));


        UUID id = UUID.fromString(segmentId);
        Segment segment = new Segment(id);
        segment.setTimeStamp(timeStamp);
        segment.setMocked(mocked);
        segment.setDistance(distance);
        segment.setMinLatitude(minLat);
        segment.setMaxLatitude(maxLat);
        segment.setMinLongitude(minLon);
        segment.setMaxLongitude(maxLon);
        segment.setRowId(rowId);
        return segment;
    }


}
