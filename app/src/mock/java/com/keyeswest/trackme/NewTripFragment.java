package com.keyeswest.trackme;


import android.content.Intent;
import android.support.v4.app.Fragment;

import com.keyeswest.trackme.services.LocationMockService;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewTripFragment extends BaseTripFragment {


    public NewTripFragment() {
        // Required empty public constructor
    }


    @Override
    protected void startUpdates() {
        Intent intent = LocationMockService.getStartUpdatesIntent(getContext());
        getActivity().startService(intent);
    }

    @Override
    protected void stopUpdates() {
        Timber.d("Stopping Mock Location Service");
        Intent intent = LocationMockService.getStopUpdatesIntent(getContext());
        getActivity().startService(intent);
    }
}
