package com.keyeswest.trackme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.data.LocationLoader;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.receivers.ProcessedLocationSampleReceiver;
import com.keyeswest.trackme.services.LocationService;
import com.keyeswest.trackme.tasks.StartSegmentTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.services.LocationProcessorService.LOCATION_BROADCAST_PLOT_SAMPLE;

public abstract class BaseTripFragment extends Fragment
        implements ProcessedLocationSampleReceiver.OnSamplesReceived, OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>, NewTripActivity.NotifyBackPressed{

    private static String IS_TRACKING_EXTRA = "isTrackingExtra";
    private static String TRACKED_SEGMENT_EXTRA = "trackedSegmentIdExtra";

    private static final int LOCATION_LOADER = 1;

    private Unbinder mUnbinder;

    private GoogleMap mMap;

    private Location mLastLocation;

    // Indicates when google map is initialized and ready to use (async initialization)
    private boolean mMapReady = false;

    // Indicates when current location has been returned
    private boolean mCurrentLocationReady = false;

    // When fragment is resumed, a call to the database is made to retrieve location samples
    // that were delivered while the fragment was paused.
    private boolean mResumeReady = false;

    private PolylineOptions mPolylineOptions;
    private Polyline mPlot;

    private ProcessedLocationSampleReceiver mSampleReceiver;

    @BindView(R.id.request_updates_button)
    Button mStartUpdatesButton;

    @BindView(R.id.remove_updates_button)
    Button mStopUpdatesButton;


    List<LatLng> mPlottedPoints = new ArrayList<>();

    protected LocationService mService = null;

    // Tracks the bound state of the service.
    protected boolean mBound = false;

    private Segment mTrackingSegment;

    protected abstract void startUpdates();
    protected abstract void stopUpdates();
    protected abstract ServiceConnection getServiceConnection();

    // Monitors the state of the connection to the service.
    final protected ServiceConnection mServiceConnection = getServiceConnection();

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Timber.d("onCreate Trip Fragment");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Timber.d("onCreateView Trip Fragment");

        View view = inflater.inflate(R.layout.fragment_base_trip, container, false);
        mUnbinder = ButterKnife.bind(this, view);


        mResumeReady = false;

        mStartUpdatesButton.setEnabled(true);
        mStopUpdatesButton.setEnabled(false);
        if (savedInstanceState != null) {
            boolean isTracking = savedInstanceState.getByte(IS_TRACKING_EXTRA) != 0;
            if (isTracking) {
                mStartUpdatesButton.setEnabled(false);
                mStopUpdatesButton.setEnabled(true);
                mTrackingSegment = savedInstanceState.getParcelable(TRACKED_SEGMENT_EXTRA);

            }
        }

        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = view.getContext();
                try {
                    Timber.i( "Starting location updates");
                    if ((mMap!= null ) && (mPlot != null)){
                        mPlot.remove();
                        mPolylineOptions = null;
                    }
                    mStartUpdatesButton.setEnabled(false);
                    mStopUpdatesButton.setEnabled(true);

                    // create a segment record in the db to hold the location samples
                    StartSegmentTask task = new StartSegmentTask(context,
                            new StartSegmentTask.ResultsCallback() {


                        @Override
                        public void onComplete(Segment segment) {

                            Timber.d("Starting track segment id: %s", segment.getId().toString());
                            mTrackingSegment = segment;
                            // jumping off point to location service
                            startUpdates();
                        }
                    });
                    task.execute();
                } catch (SecurityException e) {
                    Timber.e(e);
                }
            }
        });



        mStopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.i( "Stopping location updates");
                stopUpdates();

                mStopUpdatesButton.setEnabled(false);
                mStartUpdatesButton.setEnabled(true);
            }
        });


        // In all cases put the current location on the map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        getLastLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.plot_map);

        mapFragment.getMapAsync(this);

        mSampleReceiver = new ProcessedLocationSampleReceiver();
        mSampleReceiver.registerCallback(this);
        IntentFilter filter = new IntentFilter(LOCATION_BROADCAST_PLOT_SAMPLE);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.registerReceiver(mSampleReceiver, filter);

        return view;
    }



    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);

        Timber.d("onViewStateRestored Trip Fragment");
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Timber.d("onActivityCreated Trip Fragment");
    }

    @Override
    public void onPause() {
        Timber.d("Trip tracking paused, unregistering plot receiver");
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mSampleReceiver);

        super.onPause();
    }

    @Override
    public void onResume() {
        Timber.d("Trip tracking resumed, re-registering plot receiver");
        super.onResume();


        // Check the database and load any locations associated with the segment being plotted.
        // This ensures that the plotted trip always begins at the start even if this activity
        // is paused.
        if (mTrackingSegment != null){
            getActivity().getSupportLoaderManager().initLoader(LOCATION_LOADER ,null, this);
        }else{
            mResumeReady = true;
        }


        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mSampleReceiver,
                new IntentFilter(LOCATION_BROADCAST_PLOT_SAMPLE));
    }

    @Override
    public void onStop() {
        Timber.d("Trip tracking stopped, unbinding service");
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            getContext().unbindService(mServiceConnection);
            mBound = false;
        }

       // PreferenceManager.getDefaultSharedPreferences(getContext())
       //         .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }


    @Override
    public void updatePlot(Location location) {
        Timber.d("Received Sample Broadcast message in updatePlot");

        if (mPolylineOptions == null){
            mPolylineOptions = new PolylineOptions();
            mPlot = mMap.addPolyline(mPolylineOptions);

        }

        if (mPlottedPoints.size() > 0) {
            mPlot.setPoints(mPlottedPoints);
            mPlottedPoints.clear();
        }

        List<LatLng> points = mPlot.getPoints();



        Timber.d("Lat: " + Double.toString(location.getLatitude()) + "  Lon: " +
                    Double.toString(location.getLongitude()));

        points.add(new LatLng(location.getLatitude(), location.getLongitude()));

        mPlot.setPoints(points);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMapReady = true;
        mMap.setMyLocationEnabled(true);
        displayMap();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        Timber.d("onSaveInstanceState invoked");

        // save the state of the buttons
        boolean isTracking = mStopUpdatesButton.isEnabled();
        savedInstanceState.putByte(IS_TRACKING_EXTRA, (byte)(isTracking ? 1 : 0));

        if (isTracking){

            savedInstanceState.putParcelable(TRACKED_SEGMENT_EXTRA, mTrackingSegment);
        }

        super.onSaveInstanceState(savedInstanceState);

    }


    private void displayMap(){
        if (mMapReady && mResumeReady && mCurrentLocationReady) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude()), 15));
        }
    }


    @SuppressWarnings("MissingPermission")
    private void getLastLocation(){
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(getActivity(),new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null){
                            mLastLocation = task.getResult();
                            mCurrentLocationReady = true;
                            displayMap();

                        }else{
                            // handle error case where location not known
                        }

                    }
                });
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return LocationLoader.getLocationsForSegmentByRowId(getContext(), mTrackingSegment.getRowId());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Timber.d("Resumed locations loaded from db");
        if (data.getCount() > 0){
            mPlottedPoints = new ArrayList<>();
            LocationCursor cursor = new LocationCursor(data);
            cursor.moveToPosition(-1);
            while(cursor.moveToNext()){
                com.keyeswest.trackme.models.Location location = cursor.getLocation();
                LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
                mPlottedPoints.add(point);
            }
            Timber.d("Resumed point count= %s", Long.toString(mPlottedPoints.size()));
            cursor.close();
            getActivity().getSupportLoaderManager().destroyLoader(LOCATION_LOADER);
            mResumeReady = true;
            displayMap();

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }


    // This handles the case where the user does not stop tracking before hitting the back button
    @Override
    public void backPressed() {
        Timber.i( "Stopping location updates");
        stopUpdates();
    }
}
