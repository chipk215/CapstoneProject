package com.keyeswest.trackme;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import timber.log.Timber;


/* **** ATTRIBUTION ****
 *
 * SingleFragmentActivity encapsulates the initialization of a
 * fragment when it is the only fragment used in an Activity.
 *
 * The concept and code came from:
 * 3rd Edition Android Programming
 * Big Nerd Ranch Guide by Philips, Stewart, and Marsicano
 *
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        Timber.d("onCreate SingleFragmentActivity invoked");

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
