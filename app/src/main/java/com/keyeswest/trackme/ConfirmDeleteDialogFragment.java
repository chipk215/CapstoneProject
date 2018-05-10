package com.keyeswest.trackme;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.keyeswest.trackme.tasks.DeleteTripTask;

import java.util.UUID;


public class ConfirmDeleteDialogFragment extends DialogFragment {

    private UUID mSegmentId;

    public void setSegmentId(UUID segmentId){
        mSegmentId = segmentId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.confirm_trip_delete)
                .setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mSegmentId != null){
                            DeleteTripTask task = new DeleteTripTask(getContext());
                            task.execute(mSegmentId);
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // show snackbar
                        Toast.makeText(getContext(), "Delete Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });



        return builder.create();
    }

    private void showSnackbar(View view, String message, int duration){
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
