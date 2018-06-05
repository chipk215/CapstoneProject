package com.keyeswest.trackme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
import com.keyeswest.trackme.interfaces.NotifyBackPressed;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.receivers.ProcessedLocationSampleReceiver;
import com.keyeswest.trackme.services.LocationService;
import com.keyeswest.trackme.tasks.StartSegmentTask;
import com.keyeswest.trackme.utilities.LocationPreferences;
import com.keyeswest.trackme.widget.TrackMeWidgetService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.services.LocationProcessorService.LOCATION_BROADCAST_PLOT_SAMPLE;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.BATTERY_PREFERENCES;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.BATTERY_STATE_EXTRA;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.getLowBatteryState;
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
 *  4) User opens the app via the notification posted when the location service is in the foreground.
 *
 *     - if the user opens the app via the notification posted when the location service is in
 *     the foreground, the intent flag "FLAG_ACTIVITY_REORDER_TO_FRONT" in combination with the
 *     activity "singleTop" launch mode brings the NewTripActivity back into view.
 *
 *  5) Widget starts activity.
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
        implements ProcessedLocationSampleReceiver.OnSamplesReceived,
        OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>,
        NotifyBackPressed,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TRACKED_SEGMENT_EXTRA = "trackedSegmentIdExtra";
    protected static final String INITIAL_NEW_TRIP_STARTED_EXTRA = "initialNewTripStartedExtra";

    private static final int LOCATION_LOADER = 1;

    // ButterKnife binder
    private Unbinder mUnbinder;

    // the map
    private GoogleMap mMap;

    // Current (last) location of user
    private Location mLastLocation;

    // Plot line for tracking user
    private PolylineOptions mPolylineOptions;
    private Polyline mPlot;

    // Receives location sample updates after they have been saved in the database
    private ProcessedLocationSampleReceiver mSampleReceiver;

    @BindView(R.id.request_updates_button)
    Button mStartUpdatesButton;

    @BindView(R.id.remove_updates_button)
    Button mStopUpdatesButton;

    // Holds previously plotted points (locations) read from the database when the activity is
    // resumed. This is the key data object for distinguishing when the app is restarted without
    // being removed from memory.
    List<LatLng> mPlottedPoints = new ArrayList<>();

    // The service which provides location updates. A mocked location service is used for the mock
    // variant of the app. The FusedLocationService is used for the real app.
    protected LocationService mService = null;

    // Tracks the bound state of the service.
    protected boolean mBound = false;

    // Created when a new trip is started and corresponds to the segment that contains the locations
    private Segment mTrackingSegment;

    // THe location services are accessed via a ServiceConnection.
    protected abstract ServiceConnection getServiceConnection();

    // Monitors the state of the connection to the service.
    final protected ServiceConnection mServiceConnection = getServiceConnection();

    // State variables used when tracking is initiated by the app widget.
    private boolean mStartTrip = false;
    private boolean mTripStarted = false;

    private SharedPreferences mBatteryPreferences;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate Trip Fragment");

        // Only the app widget sets the flag to begin tracking when the activity is started.
        mStartTrip = getArguments().getBoolean(INITIAL_NEW_TRIP_STARTED_EXTRA);
        Timber.d("Start Trip: " + Boolean.toString(mStartTrip));

        setHasOptionsMenu(true);
    }



    @Override
    public void onStart(){
        Timber.d("onStart invoked");
        super.onStart();

        // If the start tracking button is disabled but we are not tracking, fix the start
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

        setTrackButtonState(true);

        if (savedInstanceState != null) {
            mTrackingSegment = savedInstanceState.getParcelable(TRACKED_SEGMENT_EXTRA);
            boolean isTracking = requestingLocationUpdates(getContext());
            if (isTracking) {
                setTrackButtonState(false);

                mTripStarted = savedInstanceState.getBoolean(INITIAL_NEW_TRIP_STARTED_EXTRA);

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

        return view;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Stop tracking when the user exits the activity. The tracking service only
                //promotes to the foreground when the user pauses the app by going to the home
                //screen.
                stopUpdates();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onResume() {
        super.onResume();
        Timber.d("Trip tracking resumed, re-registering plot receiver");
        getLastLocation();

        // register for changes in low battery state messages
        mBatteryPreferences = Objects.requireNonNull(getContext())
                .getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);

        if (mBatteryPreferences != null) {
            Timber.d("registering for shared prefs notification");
            mBatteryPreferences.registerOnSharedPreferenceChangeListener(this);
        }

    }

    @Override
    public void onPause() {
        Timber.d("Trip tracking paused, unregistering plot receiver");
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mSampleReceiver);

        if (mBatteryPreferences != null) {
            mBatteryPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        super.onPause();
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
     *    - mPlot will be recreated and be empty
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
            Timber.d("New mPlot line created.");
            mPlot = mMap.addPolyline(mPolylineOptions);
        }

        List<LatLng> points ;
        if (mPlottedPoints != null){
            // mPlottedPoints were read from database on configuration change
            points = mPlottedPoints;
            mPlottedPoints = null;  // don't include them again
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
        Timber.d("onMapReady");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        // display the user's current position
        displayMap(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        // Handle the app widget request to start a trip
        if (mStartTrip && !mTripStarted){
            Timber.d("Starting new trip");
            startNewTrip();
            mTripStarted = true;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        Timber.d("onSaveInstanceState invoked");

        // save the segment information associated with the sample locations
        savedInstanceState.putParcelable(TRACKED_SEGMENT_EXTRA, mTrackingSegment);

        savedInstanceState.putBoolean(INITIAL_NEW_TRIP_STARTED_EXTRA, mTripStarted);

        super.onSaveInstanceState(savedInstanceState);
    }


    private void displayMap(LatLng cameraPosition){
        Timber.d("displayMap invoked");

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition, 15));
        if (mTrackingSegment != null){
            // load data from db
            getLoaderManager().initLoader(LOCATION_LOADER ,null, this);
        }
    }



    @SuppressWarnings("MissingPermission")
    private void getLastLocation(){
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(getContext());

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(getActivity(),new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null){

                            Timber.d("Obtained current location");
                            mLastLocation = task.getResult();

                            // request map to be generated
                            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.plot_map);
                            mapFragment.getMapAsync(BaseTripFragment.this);

                        }else{
                            // handle error case where location not known
                            Timber.e("Unable to get current location");
                        }
                    }
                });
    }


    /* ------------------  Loader Callbacks    -----------------------------*/

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Timber.d("entering onCreateLoader");
        //TODO consider posting a loading indicator for the operation
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
            //Timber.d("Resumed point count= %s", Long.toString(mPlottedPoints.size()));
            cursor.close();
            getLoaderManager().destroyLoader(LOCATION_LOADER);

            if (requestingLocationUpdates(getContext())) {
                //start listening for updates if in a tracking state
                mSampleReceiver = new ProcessedLocationSampleReceiver();
                mSampleReceiver.registerCallback(this);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mSampleReceiver,
                        new IntentFilter(LOCATION_BROADCAST_PLOT_SAMPLE));

                //note that the mPlottedPoints data just read from the db will be used when
                // the first new location sample arrives
            }else{
                // In this case the user has stopped tracking and perhaps rotated the device.
                // We need to re-plot the track since no new incoming samples will
                // trigger a re-plot.
                mPolylineOptions = new PolylineOptions();
                mPlot = mMap.addPolyline(mPolylineOptions);
                mPlot.setPoints(mPlottedPoints);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(mPlottedPoints
                        .get(mPlottedPoints.size()-1)));
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    /* ------------ Back button helper  ---------------------------*/

    // This handles the case where the user does not stop tracking before hitting the back button
    @Override
    public void backPressed() {
        Timber.i( "Stopping location updates");
        stopUpdates();
    }


    /* --------------- Private Methods ---------------------------*/

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

    public void stopUpdates() {
        Timber.d("Entering stopUpdates");

        //Stop listening for new location samples
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mSampleReceiver);

        LocationPreferences.setRequestingLocationUpdates(getContext(), false);

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(BATTERY_STATE_EXTRA)){
            boolean isLow = getLowBatteryState(Objects.requireNonNull(getContext()));
            if (isLow){
                // display a toast and exit
                Toast.makeText(getContext(),getString(R.string.low_battery_message),
                        Toast.LENGTH_SHORT).show();

                stopUpdates();
                mStartUpdatesButton.setEnabled(false);
                mStopUpdatesButton.setEnabled(false);

               // Objects.requireNonNull(getActivity()).finish();

            }
        }
    }
}
