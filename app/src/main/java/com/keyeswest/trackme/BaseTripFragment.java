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

import com.keyeswest.trackme.receivers.ProcessedLocationSampleReceiver;
import com.keyeswest.trackme.tasks.StartSegmentTask;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public abstract class BaseTripFragment extends Fragment
        implements ProcessedLocationSampleReceiver.OnSamplesReceived {

    private Unbinder mUnbinder;

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

}
