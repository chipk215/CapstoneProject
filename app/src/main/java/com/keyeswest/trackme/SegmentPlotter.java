package com.keyeswest.trackme;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;
import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.models.Location;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import timber.log.Timber;

public class SegmentPlotter<T> extends HandlerThread {
    private static final String TAG= "SegmentPlotter";
    private static final int MESSAGE_PLOT = 0;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, LocationCursor> mRequestMap = new ConcurrentHashMap<>();

    private Handler mResponseHandler;

    private SegmentPlotterListener<T> mSegmentPlotterListener;


    public interface SegmentPlotterListener<T>{
        void plotLocation(T target, LatLng locationSample);
    }

    public void setSegmentPlotterListener(SegmentPlotterListener<T> listener){
        mSegmentPlotterListener = listener;
    }

    public SegmentPlotter(Handler responseHandler){
        super(TAG);
        mResponseHandler = responseHandler;
    }


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
    public boolean quit(){
        mHasQuit = true;
        return super.quit();
    }



    public void queueSegment(T target, LocationCursor locationCursor){
        Timber.d("Received request to plot segment");

        mRequestMap.put(target, locationCursor);
        mRequestHandler.obtainMessage(MESSAGE_PLOT, target).sendToTarget();
    }


    private void handleRequest(final T target){
        LocationCursor locationCursor = mRequestMap.get(target);

        while (locationCursor.moveToNext()) {
            Location location = locationCursor.getLocation();
            final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

             mResponseHandler.post(new Runnable(){
                 @Override
                 public void run() {
                     mSegmentPlotterListener.plotLocation(target, latLng);
                 }
             });

            try{
                Thread.sleep(500);
            }catch(InterruptedException e){
                e.printStackTrace();
            }

        }
        mRequestMap.remove(target);
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_PLOT);
        mRequestMap.clear();
    }

}
