package com.keyeswest.trackme.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.keyeswest.trackme.utilities.LocationPreferences;

import timber.log.Timber;


public class TrackMeWidgetService extends IntentService {

    private static final String ACTION_GET_TRACKING_STATE = "com.keyeswest.trackme.action.get_tracking_state";


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public TrackMeWidgetService() {
        super("TrackMeWidgetService");
    }


    public static void getTrackingState(Context context){
        Intent intent = new Intent(context, TrackMeWidgetService.class);
        intent.setAction(ACTION_GET_TRACKING_STATE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Timber.d("onHandleIntent");
        if (intent != null){
            final String action = intent.getAction();
            if (ACTION_GET_TRACKING_STATE.equals(action)){
                handleGetTrackingState();
            }
        }
    }


    private void handleGetTrackingState(){
        boolean isTracking = LocationPreferences.requestingLocationUpdates(this);
        Timber.d("Tracking is : %s", Boolean.toString(isTracking));
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TrackMeWidgetProvider.class));
        TrackMeWidgetProvider.updateTrackingState(this, appWidgetManager, isTracking, appWidgetIds);


    }
}
