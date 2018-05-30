package com.keyeswest.trackme;

import android.annotation.SuppressLint;
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
import com.keyeswest.trackme.utilities.LocationPreferences;
import com.keyeswest.trackme.widget.TrackMeWidgetService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.services.LocationProcessorService.LOCATION_BROADCAST_PLOT_SAMPLE;
import static com.keyeswest.trackme.utilities.LocationPreferences.requestingLocationUpdates;


/**
 * Usage Notes
 *
 *  1) NewTripActivity started from TripListActivity in response to user selecting "New Trip" menu
 *     item.
 *
 *     - normal usage with a new trip/segment created, normal fragment lifecycle progression.
 *
 *
 *  2) User selects app using the Overview button (https://support.google.com/nexus/answer/6073614)
 *     when the app is Stopped and the location service is in the foreground.
 *
 *     - in onResume the database is checked to read any location samples collected while the
 *       activity is Stopped so that the corresponding trip plot depict all the the locations
 *       collected since the trip was started.
 *
 *     - in this scenario the app may not be removed from memory and when resumed only onStart
 *       and onResume is invoked. Neither of these methods process savedInstance state bundle.
 *
 *  3) User stops the location service via the notification posted when the location service is
 *     in the foreground.
 *
 *     - in onStart this state is detected by looking at the start tracking button state in
 *       conjunction with the requesting location state item saved in shared preferences. If
 *       the start button state is disabled (indicating tracking has been started) but the
 *       shared preference requesting location state is false then the location service has
 *       been stopped and the button states need to be corrected.
 *
 *  4) User open the app via the notification posted when the location service is in the foreground.
 *
 *     - if the user opens the app via the notification posted when the location service is in
 *     the foreground, the intent flag "FLAG_ACTIVITY_REORDER_TO_FRONT" in combination with the
 *     activity "singleTop" launch mode brings the NewTripActivity back into view.
 *
 *  5) Widget starts activity when no instance of activity exists.
 *
 *  6) Widget starts activity when activity instance exists.
 *
 *  -------------------------
 *
 *  Why is NotifyBackPressed implemented in this fragment as a method invoked from the
 *  TripListActivity?
 *
 *  If the user hits the back button on the screen while tracking a new trip, without first stopping
 *  the tracking update, the NotifyBackPressed method provided an opportunity to shut down the
 *  location service.
 */


public abstract class BaseTripFragment extends Fragment
        implements ProcessedLocationSampleReceiver.OnSamplesReceived, OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>, NewTripActivity.NotifyBackPressed{


    private static final String TRACKED_SEGMENT_EXTRA = "trackedSegmentIdExtra";
    protected static final String INITIAL_NEW_TRIP_STARTED_EXTRA = "initialNewTripStartedExtra";


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

    // Holds previously plotted points (locations) read from the database when the activity is
    // resumed. This is the key data object for distinguishing when the app is restarted without
    // being removed from memory.
    List<LatLng> mPlottedPoints = new ArrayList<>();

    protected LocationService mService = null;

    // Tracks the bound state of the service.
    protected boolean mBound = false;

    // Created when a new trip is started and corresponds to the segment that contains the locations
    private Segment mTrackingSegment;

    protected abstract ServiceConnection getServiceConnection();


    // Monitors the state of the connection to the service.
    final protected ServiceConnection mServiceConnection = getServiceConnection();

    private FusedLocationProviderClient mFusedLocationClient;

    private boolean mStartTrip = false;
    private boolean mTripStarted = false;

    private boolean mMapDisplayed = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate Trip Fragment");
    }



    @Override
    public void onStart(){
        Timber.d("onStart invoked");
        super.onStart();

        // if the start tracking button is disabled but we are not tracking fix the start
        // button state. This state occurs when the user stops tracking using the posted
        // notification when the location service was in the foreground.
        if ((! mStartUpdatesButton.isEnabled()) && (! requestingLocationUpdates(getContext())) ) {
            if (getActivity() != null) {
                Timber.d("Entering NewTrip Activity due to user terminating location service");
                setTrackButtonState(true);
            }
        }
    }




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Timber.d("onCreateView Trip Fragment");

        View view = inflater.inflate(R.layout.fragment_base_trip, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mResumeReady = false;

        setTrackButtonState(true);

        if (savedInstanceState != null) {
            mTrackingSegment = savedInstanceState.getParcelable(TRACKED_SEGMENT_EXTRA);
            boolean isTracking = requestingLocationUpdates(getContext());
            if (isTracking) {
                setTrackButtonState(false);

                mTripStarted = savedInstanceState.getBoolean(INITIAL_NEW_TRIP_STARTED_EXTRA);

            }else{
                //TODO if a plotted trip was showing after the user stopped tracking it should
                // be redisplayed on a rotation
                if (mTrackingSegment != null){
                    // replot trip
                }

            }
        }

        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewTrip();
            }
        });



        mStopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopUpdates();
            }
        });


        // In all cases put the current location on the map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        getLastLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.plot_map);

        mapFragment.getMapAsync(this);

        return view;
    }



    @Override
    public void onPause() {
        Timber.d("Trip tracking paused, unregistering plot receiver");
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mSampleReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("Trip tracking resumed, re-registering plot receiver");

        if (requestingLocationUpdates(getContext())) {
            mSampleReceiver = new ProcessedLocationSampleReceiver();
            mSampleReceiver.registerCallback(this);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mSampleReceiver,
                    new IntentFilter(LOCATION_BROADCAST_PLOT_SAMPLE));
        }


        Timber.d("mMapDisplayed= " + Boolean.toString(mMapDisplayed));

        // Check the database and load any locations associated with the segment being plotted.
        // This ensures that the plotted trip always begins at the start even if this activity
        // is paused.
        if (mTrackingSegment != null){
            Timber.d("initloader for locations");
            getLoaderManager().initLoader(LOCATION_LOADER ,null, this);
            mResumeReady = false;


        }else{
            mResumeReady = true;
        }



        mStartTrip = NewTripActivity.startTrip(getActivity().getIntent());
        if (mStartTrip){

            // if started from the widget, after NewTripActivity had already started but not plotting
            // then invoke display map to start a new trip
            if (mTrackingSegment == null) {

                if (mMapDisplayed) {
                    // NewTripActivity started but not tracking when widget starts tracking
                    startNewTrip();
                }

            }else{
                mTripStarted = true;
            }
        }
        Timber.d("Retrieved start trip extra from intent: " + Boolean.toString(mStartTrip));
    }




    @Override
    public void onStop() {
        Timber.d("Trip tracking fragment stopped, unbinding service");
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            getContext().unbindService(mServiceConnection);
            mBound = false;
        }

        super.onStop();
    }


    /**
     * Entry conditions:
     *
     * 1) Initial entry when user starts tracking
     *     - mPolylineOptions will be null, mPlot is created and will have no points
     *     - mPlottedPoints containing previously plotted locations points will be null
     *
     * 2) Normal update with new location
     *     - mPolyline will not be null, mPlot contains track (plotted points)
     *     - mPlottedPoints containing previously plotted locations points will be null
     *
     * 3) Initial invocation after a configuration (rotation) change
     *    - mPolylineOptions will be null
     *    - mPLot will be recreated and be empty
     *    - mPlottedPoints containing previously plotted locations points will not be null
     *
     * 4) App is restarted after user pauses and then restarts without the app being removed
     * from memory
     *    - mPolyline options will not be null
     *    - mPlot will have plotted data up to the point where the app was paused
     *    - mPlottedPoints containing previously plotted locations points will not be null
     *
     *    - in this case we should dump the mPlot points and replace with mPlottedPoints
     *
     * @param location
     */
    @Override
    public void updatePlot(Location location) {
        Timber.d("Received Sample Broadcast message in updatePlot");

        if (mPolylineOptions == null){
            mPolylineOptions = new PolylineOptions();
            mPlot = mMap.addPolyline(mPolylineOptions);
        }

        List<LatLng> points ;
        if (mPlottedPoints != null){
            // mPlottedPoints were read from database on configuration change
            points = mPlottedPoints;
            mPlottedPoints = null;  // don't include them again
            //points.addAll(mPlot.getPoints());
        }else{
            points = mPlot.getPoints();
        }



        Timber.d("Lat: " + Double.toString(location.getLatitude()) + "  Lon: " +
                    Double.toString(location.getLongitude()));

        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        points.add(point);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

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


        // save the segment information associated with the sample locations
        savedInstanceState.putParcelable(TRACKED_SEGMENT_EXTRA, mTrackingSegment);


        // TODO revisit do we need this?
        savedInstanceState.putBoolean(INITIAL_NEW_TRIP_STARTED_EXTRA, mTripStarted);

        super.onSaveInstanceState(savedInstanceState);
    }



    private void displayMap(){
        Timber.d("displayMap invoked");
        if (mMapReady && mResumeReady && mCurrentLocationReady) {


            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude()), 15));

            mMapDisplayed = true;

            // Handle the widget new trip request
            if (mStartTrip && !mTripStarted){
                startNewTrip();
                mTripStarted = true;
            }


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
        Timber.d("entering onCreateLoader");
        return LocationLoader.getLocationsForSegmentByRowId(getContext(), mTrackingSegment.getRowId());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Timber.d("on load finished ");
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
            getLoaderManager().destroyLoader(LOCATION_LOADER);
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


    private void startUpdates() {

        // start listening for location sample updates
        mSampleReceiver = new ProcessedLocationSampleReceiver();
        mSampleReceiver.registerCallback(this);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mSampleReceiver,
                new IntentFilter(LOCATION_BROADCAST_PLOT_SAMPLE));

        LocationPreferences.setRequestingLocationUpdates(getContext(), true);
        mService.requestLocationUpdates();
        setTrackButtonState(false);

        // Force an update to the widget provider
        TrackMeWidgetService.getTrackingState(getContext());
    }

    private void stopUpdates() {
        Timber.d("Entering stopUpdates");

        //Stop listening for new location samples
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mSampleReceiver);

        LocationPreferences.setRequestingLocationUpdates(getContext(), false);

 /*
        if (mBound) {

            Timber.d("mBound is true.. invoking unbindService");
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            getContext().unbindService(mServiceConnection);
            mBound = false;
        }
 */

        Timber.d("Invoking location service to stop updates");
        mService.removeLocationUpdates();
        setTrackButtonState(true);
        // Force an update to the widget provider
        TrackMeWidgetService.getTrackingState(getContext());
    }

    private void startNewTrip(){
        Timber.d("New Trip Started");

        try {
            Timber.d( "Starting location updates");
            if ((mMap!= null ) && (mPlot != null)){
                mPlot.remove();
                mPolylineOptions = null;
            }

            setTrackButtonState(false);

            // create a segment record in the db to hold the location samples
            StartSegmentTask task = new StartSegmentTask(getContext(),
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


    private void setTrackButtonState(boolean enableTracking){

        mStartUpdatesButton.setEnabled(enableTracking);
        mStopUpdatesButton.setEnabled(!enableTracking);
    }


}
