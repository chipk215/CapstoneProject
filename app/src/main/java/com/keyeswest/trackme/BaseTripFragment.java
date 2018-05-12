package com.keyeswest.trackme;

import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.keyeswest.trackme.receivers.ProcessedLocationSampleReceiver;
import com.keyeswest.trackme.tasks.StartSegmentTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.services.LocationProcessorService.LOCATION_BROADCAST_PLOT_SAMPLE;

public abstract class BaseTripFragment extends Fragment
        implements ProcessedLocationSampleReceiver.OnSamplesReceived, OnMapReadyCallback {

    private static String IS_TRACKING_EXTRA = "isTrackingExtra";
    private static String PLOTTED_POINTS_EXTRA = "plottedPointsExtra";

    private Unbinder mUnbinder;

    private GoogleMap mMap;

    private Location mLastLocation;

    private FusedLocationProviderClient mFusedLocationClient;

    private boolean mMapReady = false;
    private boolean mLocationReady = false;

    private PolylineOptions mPolylineOptions;
    private Polyline mPlot;

    private ProcessedLocationSampleReceiver mSampleReceiver;

    @BindView(R.id.request_updates_button)
    Button mStartUpdatesButton;

    @BindView(R.id.remove_updates_button)
    Button mStopUpdatesButton;

    boolean mResumePlot;

    List<LatLng> mPlottedPoints;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_base_trip, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mResumePlot = false;

        mStartUpdatesButton.setEnabled(true);
        mStopUpdatesButton.setEnabled(false);
        if (savedInstanceState != null) {
            boolean isTracking = savedInstanceState.getByte(IS_TRACKING_EXTRA) != 0;
            if (isTracking) {
                mStartUpdatesButton.setEnabled(false);
                mStopUpdatesButton.setEnabled(true);

                mPlottedPoints = savedInstanceState.getParcelableArrayList(PLOTTED_POINTS_EXTRA);
                mResumePlot = true;

            }


        }


        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = view.getContext();
                try {
                    Timber.i( "Starting location updates");

                    mStartUpdatesButton.setEnabled(false);
                    mStopUpdatesButton.setEnabled(true);

                    // create a segment record in the db to hold the location samples
                    StartSegmentTask task = new StartSegmentTask(context,
                            new StartSegmentTask.ResultsCallback() {
                        @Override
                        public void onComplete(String segmentId) {
                            Timber.d("Starting track segment id: " + segmentId);
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


    protected abstract void startUpdates();
    protected abstract void stopUpdates();


    @Override
    public void updatePlot(List<Location> locations) {
        Timber.d("Received Sample Broadcast message in updatePlot");

        if (mPolylineOptions == null){
            mPolylineOptions = new PolylineOptions();
            mPlot = mMap.addPolyline(mPolylineOptions);
            if (mResumePlot){
                mPlot.setPoints(mPlottedPoints);
                mResumePlot = false;
            }
        }

        List<LatLng> points = mPlot.getPoints();


        for (Location location : locations){
            Timber.d("Lat: " + Double.toString(location.getLatitude()) + "  Lon: " +
                    Double.toString(location.getLongitude()));
            points.add(new LatLng(location.getLatitude(), location.getLongitude()));

        }

        mPlot.setPoints(points);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMapReady = true;
        if (mLocationReady){
            displayMap();
        }


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        Timber.d("onSaveInstanceState invoked");

        // save the state of the buttons
        boolean isTracking = mStopUpdatesButton.isEnabled();
        savedInstanceState.putByte(IS_TRACKING_EXTRA, (byte)(isTracking ? 1 : 0));

        if (isTracking){
            // save the points in the track
            savedInstanceState.putParcelableArrayList(PLOTTED_POINTS_EXTRA, (ArrayList<LatLng>)mPlot.getPoints());
        }

        super.onSaveInstanceState(savedInstanceState);

    }


    private void displayMap(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),
                mLastLocation.getLongitude() ), 15));
    }


    @SuppressWarnings("MissingPermission")
    private void getLastLocation(){
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(getActivity(),new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null){
                            mLastLocation = task.getResult();
                            mLocationReady = true;
                            if (mMapReady){
                                displayMap();
                            }
                        }else{
                            // handle error case where location not known
                        }

                    }
                });
    }
}
