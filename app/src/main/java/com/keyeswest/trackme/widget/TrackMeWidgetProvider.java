package com.keyeswest.trackme.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.keyeswest.trackme.NewTripActivity;
import com.keyeswest.trackme.R;
import com.keyeswest.trackme.TripListActivity;

import timber.log.Timber;

import static com.keyeswest.trackme.NewTripActivity.NEW_TRIP_EXTRA;
import static com.keyeswest.trackme.NewTripActivity.STOP_TRIP_EXTRA;

/**
 * Implementation of App Widget functionality.
 */
public class TrackMeWidgetProvider extends AppWidgetProvider {

    static boolean sIsTracking = false;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.track_me_widget);

        Timber.d("Set Track Button State: " + Boolean.toString(sIsTracking));
        // enable or disable buttons based upon tracking state
        views.setBoolean(R.id.start_track_id, "setEnabled", ! sIsTracking);
        views.setBoolean(R.id.stop_track_id, "setEnabled", sIsTracking);


        //Setup intent to start a new trip
        Intent startTripIntent =new Intent(context, NewTripActivity.class);
        startTripIntent.putExtra(NEW_TRIP_EXTRA, true);
        PendingIntent pendingStartTrip = PendingIntent.getActivity(context, 0,
                startTripIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.start_track_id, pendingStartTrip);


        //Setup intent to send a new trip (stop tracking)
        Intent stopTripIntent = new Intent(context, NewTripActivity.class);
        stopTripIntent.putExtra(STOP_TRIP_EXTRA,true);
        PendingIntent pendingStopTrip = PendingIntent.getActivity(context, 1,
                stopTripIntent,PendingIntent.FLAG_UPDATE_CURRENT );
        views.setOnClickPendingIntent(R.id.stop_track_id, pendingStopTrip);


        //Setup the intent to open the app
        Intent activityIntent = new Intent(context, TripListActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent openTripList = PendingIntent.getActivity(context, 2,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.open_list_id, openTripList);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("onUpdate invoked");
        TrackMeWidgetService.getTrackingState(context);

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void updateTrackingState(Context context, AppWidgetManager appWidgetManager,
                                           boolean isTracking, int[] appWidgetIds ){

        Timber.d("updateTrackingState invoked");
        sIsTracking = isTracking;
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}

