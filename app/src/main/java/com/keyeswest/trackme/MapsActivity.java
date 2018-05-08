package com.keyeswest.trackme;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.data.LocationLoader;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;
import com.keyeswest.trackme.models.Location;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.utilities.LatLonBounds;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static java.lang.Thread.sleep;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXTRA_URI = "com.keyeswest.fleetracker.extra_uri";

    private static final int SEGMENT_LOADER  = 0;
    private static final int LOCATION_LOADER = 1;
    private static final int PLOT_MESSAGE = 999;
    private static final String POINT_KEY = "POINT_KEY";


    /**
     * Activity requires an intent with a list of segment URIs to be plotted on the map.
     * @param packageContext
     * @param segments - segment URIs
     * @return Intent to start Activity
     */
    public static Intent newIntent(Context packageContext, List<Uri> segments){
        if ((segments == null) || (segments.size() < 1 )){
            return null;
        }

        Intent intent = new Intent(packageContext, MapsActivity.class);
        ArrayList<Uri> arrayList = new ArrayList<>(segments);
        intent.putParcelableArrayListExtra(EXTRA_URI, arrayList);

        return intent;
    }


    private SegmentPlotter<Polyline> mSegmentPlotter;

    private boolean mMapReady=false;
    private boolean mSegmentDataReady=false;

    private SegmentCursor mSegmentCursor;

    private GoogleMap mMap;
    private Polyline mPlotLine;
    private List<Uri> mSegmentList;

    private int mLocationLoadsFinishedCount;
    List<LocationCursor> mPlotLocations = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Handler responseHandler = new Handler();

        mSegmentPlotter = new SegmentPlotter<>(responseHandler);
        mSegmentPlotter.setSegmentPlotterListener(new SegmentPlotter.SegmentPlotterListener<Polyline>() {
            @Override
            public void plotLocation(Polyline target, LatLng locationSample) {
                List<LatLng> points = mPlotLine.getPoints();
                points.add(locationSample);
                mPlotLine.setPoints(points);

            }
        });


        mSegmentPlotter.start();
        mSegmentPlotter.getLooper();
        Timber.d("Background segment plotter thread started");


        // get a list of segment Uris
        mSegmentList = getIntent().getParcelableArrayListExtra(EXTRA_URI);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        // Initialize the loader that will retrieve the segments corresponding to the
        // list of segment URIs provided to the Activity. Segments will be retrieved
        // and then the corresponding location data will be loaded.
        getSupportLoaderManager().initLoader(SEGMENT_LOADER,null, this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        setMapReady(true);
        mMap = googleMap;
        if (getDataReady()){
            displayMap();
        }

    }



    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {


        if (id == SEGMENT_LOADER) {
            // All the segments in the segment list will be loaded with a single database query
            return SegmentLoader.newSegmentsFromUriList(this, mSegmentList);
        } else if (id >= LOCATION_LOADER) {
            //See notes on id for location loader in onLoadFinished method below.
            return LocationLoader.newLocationsForSegment(this, mSegmentList.get(id - LOCATION_LOADER));
        }

        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == SEGMENT_LOADER){
            mSegmentCursor = new SegmentCursor(data);

            setSegmentDataReady(true);

            // start loading the locations for each segment
            mLocationLoadsFinishedCount = 0;
            for (int i=0; i< mSegmentList.size(); i++){
                // Each segment requires a separate load to obtain the location data
                // The first location loader will use the LOCATION_LOADER value and each
                // successive location load will increase the LOCATION_LOADER value by one
                getSupportLoaderManager().initLoader(LOCATION_LOADER + i,null, this);
            }

        } else if(loader.getId() >= LOCATION_LOADER){
            // handle the completed location data loads
            LocationCursor locationCursor = new LocationCursor(data);
            mPlotLocations.add(locationCursor);
            // increment the count of location loaders that have completed
            mLocationLoadsFinishedCount++;

        }

        if (getDataReady() && getMapReady()){
            displayMap();
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == SEGMENT_LOADER){
            setSegmentDataReady(false);
            mSegmentCursor.close();
        } else if (loader.getId() >= LOCATION_LOADER){

           for(LocationCursor cursor : mPlotLocations){
               cursor.close();
            }

            mLocationLoadsFinishedCount = 0;
        }

    }

    @Override
    public void onDestroy(){
        mSegmentPlotter.quit();

        super.onDestroy();
    }


    private LatLngBounds computeBoundingBoxForSegments(){
        LatLonBounds boundingBox = new LatLonBounds();
        mSegmentCursor.moveToPosition(-1);
        while(mSegmentCursor.moveToNext()){
            Segment segment = mSegmentCursor.getSegment();
            boundingBox.update(segment.getMinLatitude(), segment.getMinLongitude());
            boundingBox.update(segment.getMaxLatitude(), segment.getMaxLongitude());
        }


        LatLngBounds bounds = new LatLngBounds(new LatLng(boundingBox.getMinLat(),
                boundingBox.getMinLon()), new LatLng(boundingBox.getMaxLat(),
                boundingBox.getMaxLon()));

        return bounds;
    }



    private void displayMap(){

        if (mSegmentList != null) {

            LatLngBounds bounds = computeBoundingBoxForSegments();

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 15));

            for (LocationCursor locationCursor : mPlotLocations) {
                PolylineOptions options = new PolylineOptions();

                plotPolyLine(options, locationCursor);

            }
        }
    }



    private boolean getMapReady(){
        return mMapReady;
    }

    private  void setMapReady(boolean value){
        mMapReady = value;
    }

    private  boolean getSegmentDataReady(){
        return mSegmentDataReady;
    }

    private  void setSegmentDataReady(boolean value){
        mSegmentDataReady = value;
    }

    private  boolean getLocationDataReady(){
        return (mLocationLoadsFinishedCount == mSegmentList.size());
    }


    private  boolean getDataReady(){
        return getSegmentDataReady() && getLocationDataReady();
    }


    private void plotPolyLine(final PolylineOptions options, final LocationCursor cursor) {

        cursor.moveToPosition(-1);
        mPlotLine = mMap.addPolyline(options);

        mSegmentPlotter.queueSegment(mPlotLine, cursor);

    }



}
