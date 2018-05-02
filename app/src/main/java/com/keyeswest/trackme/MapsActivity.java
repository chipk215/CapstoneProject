package com.keyeswest.trackme;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.data.LocationLoader;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;
import com.keyeswest.trackme.models.Location;
import com.keyeswest.trackme.models.Segment;

import timber.log.Timber;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXTRA_URI = "com.keyeswest.fleetracker.extra_uri";

    private static final int SEGMENT_LOADER  = 0;
    private static final int LOCATION_LOADER = 1;

    public static Intent newIntent(Context packageContext, Uri segmentUri){
        Intent intent = new Intent(packageContext, MapsActivity.class);
        intent.putExtra(EXTRA_URI, segmentUri);
        return intent;
    }

    private boolean mMapReady=false;
    private boolean mSegmentDataReady=false;
    private boolean mLocationDataReady = false;

    private LocationCursor mLocationCursor;

    private GoogleMap mMap;
    private Uri mSegmentUri;
    private Segment mSegment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mSegmentUri = getIntent().getParcelableExtra(EXTRA_URI);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        getSupportLoaderManager().initLoader(SEGMENT_LOADER,null, this);
        getSupportLoaderManager().initLoader(LOCATION_LOADER,null, this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        setMapReady(true);
        mMap = googleMap;
        if (getDataReady()){
            displayMap();
        }

    }


    private void displayMap(){

        if (mSegment != null) {
            LatLngBounds bounds = new LatLngBounds(new LatLng(mSegment.getMinLatitude(),
                    mSegment.getMinLongitude()), new LatLng(mSegment.getMaxLatitude(),
                    mSegment.getMaxLongitude()));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 15));


            PolylineOptions options = new PolylineOptions();

            if (mLocationCursor != null) {
                while (mLocationCursor.moveToNext()) {
                    Location location = mLocationCursor.getLocation();
                    Double lat = location.getLatitude();
                    Double lon = location.getLongitude();
                    Timber.d("Lat: " + Double.toString(lat) + "  Lon: " + Double.toString(lon));
                    options.add(new LatLng(lat, lon));

                }

                mMap.addPolyline(options);

                mLocationCursor.close();
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == SEGMENT_LOADER) {
            return SegmentLoader.newSegmentInstance(this, mSegmentUri);
        } else if (id == LOCATION_LOADER) {
            return LocationLoader.newLocationsForSegment(this, mSegmentUri);
        }

        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == SEGMENT_LOADER){
            SegmentCursor segmentCursor = new SegmentCursor(data);
            segmentCursor.moveToFirst();
            mSegment = segmentCursor.getSegment();
            segmentCursor.close();
            setSegmentDataReady(true);

        } else if(loader.getId() == LOCATION_LOADER){
            mLocationCursor = new LocationCursor(data);
            setLocationDataReady(true);
        }

        if (getDataReady() && getMapReady()){
            displayMap();
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == SEGMENT_LOADER){
            mSegment = null;
            setSegmentDataReady(false);
        } else if (loader.getId() == LOCATION_LOADER){
            if (! mLocationCursor.isClosed()){
                mLocationCursor.close();
            }
            setLocationDataReady(false);

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
        return mLocationDataReady;
    }

    private  void setLocationDataReady(boolean value){
        mLocationDataReady = value;
    }

    private  boolean getDataReady(){
        return getSegmentDataReady() && getLocationDataReady();
    }
}
