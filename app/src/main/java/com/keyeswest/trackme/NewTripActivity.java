package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;



public class NewTripActivity extends SingleFragmentActivity{

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, NewTripActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new NewTripFragment();
    }
}
