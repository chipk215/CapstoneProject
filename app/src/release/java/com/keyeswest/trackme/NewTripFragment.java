package com.keyeswest.trackme;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;

import com.keyeswest.trackme.services.FusedLocationService;

import java.util.Objects;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewTripFragment extends BaseTripFragment {

    public static NewTripFragment newInstance(Boolean startTrip){
        NewTripFragment fragment = new NewTripFragment();
        Bundle args = new Bundle();
        args.putBoolean(INITIAL_NEW_TRIP_STARTED_EXTRA, startTrip);
        fragment.setArguments(args);
        return fragment;
    }

    public NewTripFragment() {
        // Required empty public constructor
    }

    @Override
    protected ServiceConnection getServiceConnection(){
        return new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Timber.d("onServiceConnected");
                FusedLocationService.LocalBinder binder = (FusedLocationService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Timber.d("onServiceDisconnected");
                mService = null;
                mBound = false;
            }
        };
    }

    @Override
    public void onStart(){
        super.onStart();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        Objects.requireNonNull(getContext()).bindService(new Intent(getContext(), FusedLocationService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }


}
