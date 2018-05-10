package com.keyeswest.trackme.tasks;

import android.content.Context;

import android.os.AsyncTask;

import com.keyeswest.trackme.data.Queries;

import java.util.UUID;

public class DeleteTripTask extends AsyncTask<UUID, Void, Void> {


    private Context mContext;
    public DeleteTripTask(Context context){
        mContext = context;

    }


    @Override
    protected Void doInBackground(UUID... uuids) {
        UUID segmentId = uuids[0];

        Queries.deleteTrip(mContext, segmentId);

        return null;

    }



}
