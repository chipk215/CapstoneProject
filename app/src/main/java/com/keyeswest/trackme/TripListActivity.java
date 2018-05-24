package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import timber.log.Timber;


public class TripListActivity extends AppCompatActivity implements TripListFragment.TripListListener {

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, TripListActivity.class);
        return intent;
    }

    public static final String ARG_SELECTED_TRIPS = "argSelectedTrips";

    @Nullable
    @BindView(R.id.map_divider_view)
    View mTwoPaneDivider;
    private boolean mTwoPane;

    // List of currently selected/checked trips
    private List<Segment> mSelectedSegments;

    private TripMapFragment mTripMapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate TripListActivity");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_triplist);
       // Stetho.initializeWithDefaults(this);
        ButterKnife.bind(this);

        mSelectedSegments = new ArrayList<>();
        mTwoPane = (mTwoPaneDivider != null);

        if (savedInstanceState == null) {
            // Add fragment for displaying list of trips
            TripListFragment tripListFragment = TripListFragment.newInstance(mTwoPane);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.trip_list_container, tripListFragment)
                    .commit();

            if (mTwoPane) {
                // Add fragment for displaying selected trips
                ArrayList<Uri> tripList = new ArrayList<>();
                mTripMapFragment = TripMapFragment.newInstance(mTwoPane, tripList);

                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(R.id.trip_map_container, mTripMapFragment)
                        .commit();

            }
        }else{
            Timber.d("Restoring mSelectedSegments (Activity) and filter state after config change");
            mSelectedSegments = savedInstanceState.getParcelableArrayList(ARG_SELECTED_TRIPS);
        }
    }

    @Override
    public void onTripSelected(Segment selectedSegment) {
       mSelectedSegments.add(selectedSegment);

        if (mTwoPane){
            if ((mTripMapFragment != null) && (mTripMapFragment.isVisible())){
                mTripMapFragment.addSegment(selectedSegment);
            }
        }
    }


    @Override
    public void onTripUnselected(Segment segment) {
        mSelectedSegments.remove(segment);

        if (mTwoPane){
            if ((mTripMapFragment != null) && (mTripMapFragment.isVisible())){
                mTripMapFragment.removeSegment(segment);
            }
        }
    }

    @Override
    public void plotSelectedTrips() {

        // Only needed if in single pane mode
        if (! mTwoPane) {
            List<Uri> selectedTrips = new ArrayList<>();
            for (Segment segment : mSelectedSegments) {
                Uri itemUri = SegmentSchema.SegmentTable.buildItemUri(segment.getRowId());
                selectedTrips.add(itemUri);
            }

            if (selectedTrips.size() > 0) {
                // plot them
                Intent intent = TripMapActivity.newIntent(this, selectedTrips);
                startActivity(intent);
            }
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
        Timber.d("onSaveInstanceState invoked");

        // save the set of selected trips
        savedInstanceState.putParcelableArrayList(ARG_SELECTED_TRIPS,
                (ArrayList<Segment>)mSelectedSegments);

        super.onSaveInstanceState(savedInstanceState);

    }

}
