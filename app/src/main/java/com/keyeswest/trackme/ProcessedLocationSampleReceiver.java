package com.keyeswest.trackme;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.keyeswest.trackme.services.LocationProcessorService;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.keyeswest.trackme.services.LocationProcessorService.PLOT_SAMPLES_EXTRA_KEY;

public class ProcessedLocationSampleReceiver extends BroadcastReceiver {

    public interface OnSamplesReceived{
        void updatePlot(List<Location> locations);
    }


    private OnSamplesReceived mCallback;

    public void registerCallback(OnSamplesReceived callback){
        mCallback = callback;
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Received plot location broadcast message");
        if (intent != null){
            final String action = intent.getAction();
            if (LocationProcessorService.LOCATION_BROADCAST_PLOT_SAMPLE.equals(action)){
                Timber.d("Extracting locations from broadcast intent");
                ArrayList<Location> locations = intent.getParcelableArrayListExtra(PLOT_SAMPLES_EXTRA_KEY);
                List<Location> list = locations;
                mCallback.updatePlot(list);

            }
        }

    }
}