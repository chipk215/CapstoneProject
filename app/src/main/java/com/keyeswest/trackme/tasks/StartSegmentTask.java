package com.keyeswest.trackme.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;

import com.keyeswest.trackme.data.Queries;
import com.keyeswest.trackme.models.Segment;

public class StartSegmentTask extends AsyncTask<Void, Void, String> {

    public static final String SEGMENT_ID_KEY = "com.keyeswest.fleetracker.com.segmentId";


    public interface ResultsCallback{
        void onComplete(String segmentId);
    }

    private Context mContext;
    private ResultsCallback mCallback;

    public StartSegmentTask(Context context, ResultsCallback callback){
        mContext = context;
        mCallback = callback;
    }


    @Override
    protected String doInBackground(Void... voids) {

        Uri segmentUri = Queries.createNewSegment(mContext);
        Segment segment = Queries.getSegmentFromUri(mContext, segmentUri);

        if (segment != null) {
            String segmentId = segment.getId().toString();
            // This is a workaround for not being able to send the segment id in the intent.
            SharedPreferences sharedPref = mContext.getSharedPreferences(SEGMENT_ID_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(SEGMENT_ID_KEY, segmentId);
            editor.commit();

            return segmentId;

        }

        return null;

    }

    @Override
    protected void onPostExecute(String segmentId) {
        mCallback.onComplete(segmentId);
    }


}