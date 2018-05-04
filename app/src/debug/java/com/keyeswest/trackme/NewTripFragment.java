package com.keyeswest.trackme;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keyeswest.trackme.services.LocationService;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewTripFragment extends BaseTripFragment {


    public NewTripFragment() {
        // Required empty public constructor
    }


    @Override
    protected void startUpdates() {
        Intent intent = LocationService.getStartUpdatesIntent(getContext());
        getActivity().startService(intent);
    }

    @Override
    protected void stopUpdates() {
        Intent intent = LocationService.getStopUpdatesIntent(getContext());
        getActivity().startService(intent);
    }
}
