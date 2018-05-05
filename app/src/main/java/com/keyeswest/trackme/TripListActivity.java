package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class TripListActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, TripListActivity.class);
        return intent;
    }
    @Override
    protected Fragment createFragment() {
        return new TripListFragment();
    }
}
