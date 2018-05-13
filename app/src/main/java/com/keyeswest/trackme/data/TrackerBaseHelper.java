package com.keyeswest.trackme.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.keyeswest.trackme.models.Segment;

public class TrackerBaseHelper extends SQLiteOpenHelper {

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
    private static final String CREATE_TABLE = "CREATE TABLE ";

    private static final String DATABASE_NAME = "TrackerBase.db";

    private static final int DATABASE_VERSION = 2;


    public TrackerBaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            //TODO Revisit
           // db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        final String SQL_CREATE_LOCATION_TABLE =
                CREATE_TABLE + LocationSchema.LocationTable.TABLE_NAME +
                        " (" +
                        " _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        LocationSchema.LocationTable.COLUMN_TIME_STAMP + " INTEGER NOT NULL," +
                        LocationSchema.LocationTable.COLUMN_LATITUDE + " REAL NOT NULL," +
                        LocationSchema.LocationTable.COLUMN_LONGITUDE + " REAL NOT NULL," +
                        LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " TEXT  NOT NULL," +
                        " FOREIGN KEY ( " + LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " ) REFERENCES " +
                        SegmentSchema.SegmentTable.TABLE_NAME + " (COLUMN_ID)" + " ON DELETE CASCADE " +
                        ");";

        db.execSQL(SQL_CREATE_LOCATION_TABLE);


        final String SQL_CREATE_SEGMENT_TABLE =
                CREATE_TABLE + SegmentSchema.SegmentTable.TABLE_NAME +
                        " (" +
                        SegmentSchema.SegmentTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SegmentSchema.SegmentTable.COLUMN_ID + " TEXT UNIQUE NOT NULL, " +
                        SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " INTEGER NOT NULL," +
                        SegmentSchema.SegmentTable.COLUMN_FAVORITE + " INTEGER NOT NULL, " +
                        SegmentSchema.SegmentTable.COLUMN_DISTANCE + " REAL, " +
                        SegmentSchema.SegmentTable.COLUMN_MIN_LAT + " REAL, " +
                        SegmentSchema.SegmentTable.COLUMN_MAX_LAT + " REAL, " +
                        SegmentSchema.SegmentTable.COLUMN_MIN_LON + " REAL, " +
                        SegmentSchema.SegmentTable.COLUMN_MAX_LON + " REAL, " +
                        SegmentSchema.SegmentTable.COLUMN_ELAPSED_TIME + " INTEGER NOT NULL," +
                        SegmentSchema.SegmentTable.COLUMN_MAX_SPEED + " REAL NOT NULL " +
                        ");";

        db.execSQL(SQL_CREATE_SEGMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // For now simply drop the table and create a new one. This means if you change the
        // DATABASE_VERSION the table will be dropped.
        // In a production app, this method might be modified to ALTER the table
        // instead of dropping it, so that existing data is not deleted.
        db.execSQL(DROP_TABLE + LocationSchema.LocationTable.TABLE_NAME);
        db.execSQL(DROP_TABLE + SegmentSchema.SegmentTable.TABLE_NAME);

        onCreate(db);

    }


    public static ContentValues createLocationRecord(long timeStamp,
                                                     double lat, double lon,
                                                     String segmentId){

        ContentValues values = new ContentValues();
        values.put(LocationSchema.LocationTable.COLUMN_TIME_STAMP, timeStamp);
        values.put(LocationSchema.LocationTable.COLUMN_LATITUDE, lat);
        values.put(LocationSchema.LocationTable.COLUMN_LONGITUDE, lon);
        values.put(LocationSchema.LocationTable.COLUMN_SEGMENT_ID, segmentId);

        return values;

    }

    public static ContentValues createSegmentRecord(String id, long timeStamp){
        ContentValues values = new ContentValues();
        values.put(SegmentSchema.SegmentTable.COLUMN_ID, id);
        values.put(SegmentSchema.SegmentTable.COLUMN_TIME_STAMP, timeStamp);
        values.put(SegmentSchema.SegmentTable.COLUMN_FAVORITE, 0);
        values.put(SegmentSchema.SegmentTable.COLUMN_DISTANCE,0f);
        values.put(SegmentSchema.SegmentTable.COLUMN_ELAPSED_TIME,0);
        values.put(SegmentSchema.SegmentTable.COLUMN_MAX_SPEED,0f);


        return values;
    }

    public static ContentValues updateSegmentRecordBoundsAndDistance(Double minLat, Double maxLat,
                                                                     Double minLon, Double maxLon,
                                                                     Double distance){

        ContentValues values = new ContentValues();
        values.put(SegmentSchema.SegmentTable.COLUMN_MIN_LAT, minLat);
        values.put(SegmentSchema.SegmentTable.COLUMN_MAX_LAT,maxLat);
        values.put(SegmentSchema.SegmentTable.COLUMN_MIN_LON, minLon);
        values.put(SegmentSchema.SegmentTable.COLUMN_MAX_LON, maxLon);
        values.put(SegmentSchema.SegmentTable.COLUMN_DISTANCE, distance);

        return values;

    }

    public static ContentValues updateSegmentRecordFavoriteStatus(boolean favoriteStatus){
        ContentValues values = new ContentValues();

        values.put(SegmentSchema.SegmentTable.COLUMN_FAVORITE, favoriteStatus ? 1: 0);

        return values;
    }


}
