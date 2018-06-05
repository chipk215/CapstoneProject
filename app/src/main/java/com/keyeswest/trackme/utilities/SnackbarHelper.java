package com.keyeswest.trackme.utilities;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.keyeswest.trackme.R;

public class SnackbarHelper {

    public static void showSnackbar(View view, String message, int duration){
        // Create snackbar
        final Snackbar snackbar = Snackbar.make(view, message, duration);

        // Set an action on it, and a handler
        snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}
