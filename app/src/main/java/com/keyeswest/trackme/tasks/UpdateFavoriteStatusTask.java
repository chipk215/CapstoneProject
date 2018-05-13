package com.keyeswest.trackme.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.keyeswest.trackme.data.Queries;

import java.util.UUID;

public class UpdateFavoriteStatusTask extends AsyncTask<Void, Void, Void> {

    Context mContext;
    UUID mSegmentId;
    boolean mFavoriteStatus;


    public UpdateFavoriteStatusTask(Context context, UUID segmentId, boolean favoriteStatus){
        mContext = context;
        mSegmentId = segmentId;
        mFavoriteStatus = favoriteStatus;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Queries.updateSegmentFavoriteStatus(mContext, mSegmentId, mFavoriteStatus);

        return null;
    }
}
