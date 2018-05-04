package com.keyeswest.trackme;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.keyeswest.trackme.receivers.ProcessedLocationSampleReceiver;
import com.keyeswest.trackme.tasks.StartSegmentTask;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public abstract class BaseTripFragment extends Fragment
        implements ProcessedLocationSampleReceiver.OnSamplesReceived, OnMapReadyCallback {

    private Unbinder mUnbinder;

    private GoogleMap mMap;

    private Location mLastLocation;

    private FusedLocationProviderClient mFusedLocationClient;

    private boolean mMapReady = false;
    private boolean mLocationReady = false;

    @BindView(R.id.request_updates_button)
    Button mStartUpdatesButton;

    @BindView(R.id.remove_updates_button)
    Button mStopUpdatesButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_base_trip, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mStartUpdatesButton.setEnabled(true);
        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = view.getContext();
                try {
                    Timber.i( "Starting location updates");

                    mStartUpdatesButton.setEnabled(false);
                    mStopUpdatesButton.setEnabled(true);

                    StartSegmentTask task = new StartSegmentTask(context, new StartSegmentTask.ResultsCallback() {
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


        mStopUpdatesButton.setEnabled(false);
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

        return view;
    }


    protected abstract void startUpdates();
    protected abstract void stopUpdates();


    @Override
    public void updatePlot(List<Location> locations) {
        Timber.d("Received Sample Broadcast message in updatePlot");
        for (Location location : locations){
            Timber.d("Lat: " + Double.toString(location.getLatitude()) + "  Lon: " +
                    Double.toString(location.getLongitude()));
        }
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
