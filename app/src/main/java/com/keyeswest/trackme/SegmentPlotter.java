package com.keyeswest.trackme;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;
import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.models.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import timber.log.Timber;

public class SegmentPlotter<T> extends HandlerThread {
    private static final String TAG= "SegmentPlotter";
    private static final int MESSAGE_PLOT = 0;

    private static final int NUMBER_LOCATION_BATCHES = 10;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, LocationCursor> mRequestMap = new ConcurrentHashMap<>();

    private Handler mResponseHandler;

    private SegmentPlotterListener<T> mSegmentPlotterListener;


    public interface SegmentPlotterListener<T>{
        void plotLocation(T target, List<LatLng> points);
    }

    public void setSegmentPlotterListener(SegmentPlotterListener<T> listener){
        mSegmentPlotterListener = listener;
    }

    SegmentPlotter(Handler responseHandler){
        super(TAG);
        mResponseHandler = responseHandler;
    }


    @SuppressWarnings("unchecked")  //https://stackoverflow.com/a/2592661/9128441
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared(){
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg){
                if (msg.what == MESSAGE_PLOT){
                    T target = (T) msg.obj;
                    handleRequest(target);
                }
            }

        };
    }

    @Override
    public boolean quitSafely(){
        mHasQuit = true;
        clearQueue();
        return super.quitSafely();
    }



    public void queueSegment(T target, LocationCursor locationCursor){
        Timber.d("Received request to plot segment");

        mRequestMap.put(target, locationCursor);
        mRequestHandler.obtainMessage(MESSAGE_PLOT, target).sendToTarget();
    }



    private void handleRequest(final T target){

        LocationCursor locationCursor = mRequestMap.get(target);
        locationCursor.moveToPosition(-1);
        int numberLocationSamples = locationCursor.getCount();
        Timber.d("Location samples= %s", numberLocationSamples);

        // create 10 batches of locations
        int batchSize = numberLocationSamples / NUMBER_LOCATION_BATCHES;

        // process all but the last batch whose size may be bigger
        for (int batch =0; batch < NUMBER_LOCATION_BATCHES-1; batch++){

            if (mHasQuit){
                return;
            }
            final List<LatLng> batchList = new ArrayList<>();
            // add locations to batch
            for (int i=0; i< batchSize; i++){
                locationCursor.moveToNext();
                Location location = locationCursor.getLocation();
                batchList.add(new LatLng(location.getLatitude(), location.getLongitude()));
            }
            // plot the batch
            mResponseHandler.post(new Runnable(){
                @Override
                public void run() {

                    mSegmentPlotterListener.plotLocation(target, batchList);
                }
            });

            try{
                Thread.sleep(150);
            }catch(InterruptedException e){
                e.printStackTrace();
            }

        }

        //process the last batch
        final List<LatLng> batchList = new ArrayList<>();
        while (locationCursor.moveToNext()) {
            Location location = locationCursor.getLocation();
            batchList.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        mResponseHandler.post(new Runnable(){
            @Override
            public void run() {

                mSegmentPlotterListener.plotLocation(target, batchList);
            }
        });


        mRequestMap.remove(target);
    }

    private void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_PLOT);
        mRequestMap.clear();
    }

}
