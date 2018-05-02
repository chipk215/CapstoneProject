package com.keyeswest.trackme;


import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.keyeswest.trackme.tasks.StartSegmentTask;

import java.util.List;

import timber.log.Timber;

import static com.keyeswest.trackme.services.LocationProcessorService.LOCATION_BROADCAST_PLOT_SAMPLE;

public abstract class TrackerBaseActivity extends AppCompatActivity
        implements ProcessedLocationSampleReceiver.OnSamplesReceived {

    private Button mStartUpdatesButton;
    private Button mStopUpdatesButton;

    private ProcessedLocationSampleReceiver mSampleReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tracker);

        mSampleReceiver = new ProcessedLocationSampleReceiver();
        mSampleReceiver.registerCallback(this);
        IntentFilter filter = new IntentFilter(LOCATION_BROADCAST_PLOT_SAMPLE);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(mSampleReceiver, filter);


        // Locate the UI widgets.
        mStartUpdatesButton =  findViewById(R.id.request_updates_button);
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

        mStopUpdatesButton = findViewById(R.id.remove_updates_button);
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
}
