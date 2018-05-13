package com.keyeswest.trackme;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.facebook.stetho.Stetho;
import com.keyeswest.trackme.data.LocationSchema;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.data.TrackerBaseHelper;
import com.keyeswest.trackme.models.Location;
import com.keyeswest.trackme.models.Segment;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.keyeswest.trackme.data.TrackerBaseHelper.createLocationRecord;
import static com.keyeswest.trackme.data.TrackerBaseHelper.createSegmentRecord;

@RunWith(AndroidJUnit4.class)
public abstract class TracksContentProviderBaseTest {

    // Context used to access various parts of the system
    protected final Context mContext = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp(){
        Stetho.initializeWithDefaults(mContext);
        deleteTables();

    }


    @After
    public void cleanUp(){
        deleteTables();
    }


     //Test Helper Methods
    private void deleteTables(){
        TrackerBaseHelper dbHelper = new TrackerBaseHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.execSQL("PRAGMA foreign_keys = OFF;");
        //boolean isOk = database.isDatabaseIntegrityOk();
        database.delete(LocationSchema.LocationTable.TABLE_NAME, null , null);
        database.delete(SegmentSchema.SegmentTable.TABLE_NAME, null , null);
        database.execSQL("PRAGMA foreign_keys = ON;");
        database.close();
    }



    protected List<Location> makeLocationRecords(int numberToInsert, UUID segmentId){
        List<Location> locations = new ArrayList<Location>();
        for (int i=0; i< numberToInsert; i++){
            Location location = new Location();
            location.setSegmentId(segmentId);
            location.setTimeStamp(i);
            location.setLatitude(45.0d);
            location.setLongitude(60.0d);
            locations.add(location);
        }

        return locations;
    }


    protected List<Location> insertLocationRecords(SQLiteDatabase database, int numberToInsert, UUID[] segmentIds){
        List<Location> locations = new ArrayList<>();
        for (UUID segmentId : segmentIds){
            locations.addAll(makeLocationRecords(numberToInsert, segmentId));
        }

        for (Location location : locations){
            ContentValues values = createLocationRecord(location.getTimeStamp(),
                    location.getLatitude(), location.getLongitude(),
                    location.getSegmentId().toString());
            database.insert(LocationSchema.LocationTable.TABLE_NAME, null, values);
        }

        return locations;
    }

    protected List<Segment> insertSegmentRecords(SQLiteDatabase database,int numberToInsert){
        List<Segment> segments = makeSegments(numberToInsert);
        for (Segment s : segments){

            ContentValues testValues = createSegmentRecord(s.getId().toString(),
                    s.getTimeStamp());

            database.insert(SegmentSchema.SegmentTable.TABLE_NAME,
                    null,
                    testValues);
        }

        return segments;
    }

    protected List<Segment> makeSegments(int numberToInsert){
        List<Segment> segmentList = new ArrayList<>();
        for (int i=0; i< numberToInsert; i++){
            UUID id = UUID.randomUUID();
            Segment segment = new Segment(id);
            segment.setTimeStamp(i);
            segment.setFavorite(0);
            segmentList.add(segment);
        }

        return segmentList;
    }


}
