package com.keyeswest.trackme.tasks;

import android.content.Context;

import android.os.AsyncTask;

import com.keyeswest.trackme.data.Queries;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class DeleteTripTask extends AsyncTask<UUID, Void, Void> {


    private WeakReference<Context> mContext;
    //private  Context mContext;
    public DeleteTripTask(Context context){
        mContext =  new WeakReference<>(context);

    }

    @Override
    protected Void doInBackground(UUID... uuids) {
        UUID segmentId = uuids[0];

        Queries.deleteTrip(mContext.get(), segmentId);

        return null;

    }



}
