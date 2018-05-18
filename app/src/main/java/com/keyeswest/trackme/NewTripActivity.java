package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;



public class NewTripActivity extends SingleFragmentActivity{

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, NewTripActivity.class);
        return intent;
    }

    public interface NotifyBackPressed{
        void backPressed();
    }

    Fragment mFragment;

    @Override
    protected Fragment createFragment() {
        mFragment =  new NewTripFragment();
        return mFragment;
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
}
