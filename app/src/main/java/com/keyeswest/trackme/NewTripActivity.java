package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import timber.log.Timber;


public class NewTripActivity  extends AppCompatActivity {

    public static final String NEW_TRIP_EXTRA = "newTripExtra";
    public static final String OPTIONS_EXTRA = "optionsExtra";

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


    public static boolean startTrip(Intent intent){
        return intent.getBooleanExtra(NEW_TRIP_EXTRA,false);
    }

    public interface NotifyBackPressed{
        void backPressed();
    }

    Fragment mFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        Intent intent = getIntent();
        if (intent != null){
            Bundle bundle = intent.getBundleExtra(OPTIONS_EXTRA);
            if (bundle != null){
                Timber.d("NewTripActivity startTrip= " + Boolean.toString(bundle.getBoolean(NEW_TRIP_EXTRA)));
            }

        }



        FragmentManager fm = getSupportFragmentManager();
        mFragment = fm.findFragmentById(R.id.fragment_container);

        if (mFragment == null) {
            mFragment = new NewTripFragment();
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
        if (mFragment instanceof NotifyBackPressed) {
            ((NotifyBackPressed) mFragment).backPressed();
        }

        super.onBackPressed();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent invoked");
        super.onNewIntent(intent);

        setIntent(intent);

        FragmentManager fm = getSupportFragmentManager();
        mFragment = new NewTripFragment();
        fm.beginTransaction()
                .replace(R.id.fragment_container, mFragment)
                .commit();
    }


}
