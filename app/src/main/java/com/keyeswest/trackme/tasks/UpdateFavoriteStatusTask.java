package com.keyeswest.trackme.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.keyeswest.trackme.data.Queries;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class UpdateFavoriteStatusTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> mContext;
    private UUID mSegmentId;
    private boolean mFavoriteStatus;


    public UpdateFavoriteStatusTask(Context context, UUID segmentId, boolean favoriteStatus){
        mContext =  new WeakReference<>(context);
        mSegmentId = segmentId;
        mFavoriteStatus = favoriteStatus;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Queries.updateSegmentFavoriteStatus(mContext.get(), mSegmentId, mFavoriteStatus);

        return null;
    }
}
