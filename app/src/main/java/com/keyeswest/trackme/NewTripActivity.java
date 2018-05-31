package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import timber.log.Timber;

/**
 * Manages the functionality associated with tracking the user's position and creating a trip.
 */
public class NewTripActivity  extends AppCompatActivity {

    // Extra keys used by App Widget to start activity with a request to start a trip
    // or stop tracking the user and end the trip.
    private static final String NEW_TRIP_EXTRA = "newTripExtra";
    private static final String STOP_TRIP_EXTRA = "stopTripExtra";

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, NewTripActivity.class);
        intent.putExtra(NEW_TRIP_EXTRA, false);
        return intent;
    }

    public static Intent newTripIntent(Context packageContext){
        Intent intent = new Intent(packageContext, NewTripActivity.class);
        intent.putExtra(NEW_TRIP_EXTRA, true);
        return intent;
    }

    public static Intent stopTripIntent(Context packageContext){
        Intent intent = new Intent(packageContext, NewTripActivity.class);
        intent.putExtra(STOP_TRIP_EXTRA, true);
        return intent;
    }


    private static boolean isStartTrip(Intent intent){
        return intent.getBooleanExtra(NEW_TRIP_EXTRA,false);
    }

    private static boolean isStopTrip(Intent intent){
        return intent.getBooleanExtra(STOP_TRIP_EXTRA, false);
    }

    NewTripFragment mFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        Intent intent= getIntent();
        boolean newTrip = isStartTrip(intent);
        Timber.d("Start Trip: " + Boolean.toString(newTrip));
        boolean stopTrip = isStopTrip(intent);
        Timber.d("Stop Trip: " + Boolean.toString(stopTrip));


        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            mFragment = NewTripFragment.newInstance(newTrip);
            fm.beginTransaction()
                    .add(R.id.fragment_container, mFragment)
                    .commit();
        }
    }



    @Override
    public void onBackPressed(){

        // Allow the fragment to stop location service
        // We only want to stop the background service on a back press otherwise keep
        // the service running.
        //
        // This handles the case where the user does not stop tracking before hitting the back button
        if (mFragment != null) {
             mFragment.backPressed();
        }

        super.onBackPressed();

    }

    /**
     * Handle the case where the user has navigated to the new trip screen and has not started
     * tracking. The user then pauses the app and goes to the home screen and uses the app widget
     * to start tracking a new trip.
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent invoked");
        super.onNewIntent(intent);

        boolean newTrip = isStartTrip(intent);
        if (newTrip){

            // Replace the existing fragment with a new fragment with an argument that causes
            // trip tracking to be started without the user having to press the start
            // tracking button.
            FragmentManager fm = getSupportFragmentManager();
            mFragment = NewTripFragment.newInstance(true);
            fm.beginTransaction()
                    .replace(R.id.fragment_container, mFragment)
                    .commit();
        }else if (isStopTrip(intent)){
            // Handle the app widget request to stop tracking
            mFragment.stopUpdates();
        }

        setIntent(intent);
    }

}
