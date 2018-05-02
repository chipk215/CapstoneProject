package com.keyeswest.trackme;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.facebook.stetho.Stetho;
import com.keyeswest.trackme.data.LocationSchema;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.data.TrackerBaseHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

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


}
