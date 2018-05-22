package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.stetho.Stetho;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.models.Segment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripListActivity extends AppCompatActivity implements TripListFragment.TripListListener {

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, TripListActivity.class);
        return intent;
    }

    @Nullable
    @BindView(R.id.map_divider_view)
    View mTwoPaneDivider;
    private boolean mTwoPane;

    // List of currently selected/checked trips
    private List<Segment> mSelectedSegments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_triplist);
        Stetho.initializeWithDefaults(this);
        ButterKnife.bind(this);

        mSelectedSegments = new ArrayList<>();


        // Add fragment for displaying list of trips
        TripListFragment tripListFragment = TripListFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.trip_list_container, tripListFragment)
                .commit();



        mTwoPane = (mTwoPaneDivider != null);
        if (mTwoPane){
            // Add fragment for displaying selected trips
            ArrayList<Uri> tripList = new ArrayList<>();
            TripMapFragment mapFragment = TripMapFragment.newInstance(tripList);

            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.trip_map_container, mapFragment)
                    .commit();

        }
    }

    @Override
    public void onTripSelected(Segment segment) {
        mSelectedSegments.add(segment);
    }

    @Override
    public void onTripUnselected(Segment segment) {
        mSelectedSegments.remove(segment);
    }

    @Override
    public void plotSelectedTrips() {
        List<Uri> selectedTrips = new ArrayList<>();
        for (Segment segment : mSelectedSegments){
            Uri itemUri = SegmentSchema.SegmentTable.buildItemUri(segment.getRowId());
            selectedTrips.add(itemUri);
        }

        if (selectedTrips.size() > 0){
            // plot them
            Intent intent = TripMapActivity.newIntent(this, selectedTrips);
            startActivity(intent);
        }

    }
}
