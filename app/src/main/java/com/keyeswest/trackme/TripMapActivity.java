package com.keyeswest.trackme;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Used when target is phone (not tablet).
 */
public class TripMapActivity extends AppCompatActivity
 {

    public static final String EXTRA_URI = "com.keyeswest.fleetracker.extra_uri";


    /**
     * Activity requires an intent with a list of segment URIs to be plotted on the map.
     * @param packageContext - client context
     * @param segments - segment URIs
     * @return Intent to start Activity
     */
    public static Intent newIntent(Context packageContext, List<Uri> segments){
        if ((segments == null) || (segments.size() < 1 )){
            return null;
        }

        Intent intent = new Intent(packageContext, TripMapActivity.class);
        ArrayList<Uri> arrayList = new ArrayList<>(segments);
        intent.putParcelableArrayListExtra(EXTRA_URI, arrayList);

        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_map);

        // get a list of segment Uris corresponding to the trips to plot
        ArrayList<Uri> tripList = getIntent().getParcelableArrayListExtra(EXTRA_URI);
        TripMapFragment mapFragment = TripMapFragment.newInstance(false,tripList);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.trip_map_container, mapFragment)
                .commit();
    }


}
