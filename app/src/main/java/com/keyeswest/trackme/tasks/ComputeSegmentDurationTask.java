package com.keyeswest.trackme.tasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.keyeswest.trackme.MapsActivity;
import com.keyeswest.trackme.data.Queries;

import static com.keyeswest.trackme.data.Queries.updateSegmentDuration;

public class ComputeSegmentDurationTask extends AsyncTask<String,Void, Long> {

    public interface ResultsCallback{
        void onComplete(Long duration);
    }

    private Context mContext;
    private ResultsCallback mCallback;

    public ComputeSegmentDurationTask(Context context, ResultsCallback callback){

        mContext = context;
        mCallback = callback;
    }
    @Override
    protected Long doInBackground(String... args) {
        if (args.length != 1){
            return 0L;
        }
        String segmentId = args[0];
        Cursor cursor = Queries.getSegmentLocationFirstLastTimeStamps(mContext, segmentId);
        cursor.moveToFirst();
        long min = cursor.getLong(0);
        long max = cursor.getLong(1);


        // update segment with duration
        updateSegmentDuration(mContext, segmentId, max-min);

        return max - min;


    }

    @Override
    protected void onPostExecute(Long duration) {
        mCallback.onComplete(duration);
    }
}
