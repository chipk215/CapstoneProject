package com.keyeswest.trackme.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.location.LocationResult;
import com.keyeswest.trackme.services.LocationProcessorService;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.keyeswest.trackme.services.LocationProcessorService.LOCATIONS_EXTRA_KEY;
import static com.keyeswest.trackme.services.LocationProcessorService.SEGMENT_ID_EXTRA_KEY;
import static com.keyeswest.trackme.tasks.StartSegmentTask.SEGMENT_ID_KEY;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_PROCESS_UPDATES =
            "com.keyeswest.trackme.locationupdatespendingintent.action.PROCESS_UPDATES";

    public static final String ACTION_PROCESS_MOCK_UPDATES =
            "com.keyeswest.trackme.locationupdatespendingintent.action.PROCESS_MOCK_UPDATES";

    public static final String MOCK_LOCATION_EXTRA_KEY = "mockLocationExtraKey";


    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("LocationUpdate Broadcast Message Received");

        if (intent != null) {

            SharedPreferences prefs = context.getSharedPreferences(SEGMENT_ID_KEY,
                    Context.MODE_PRIVATE);
            String segmentId = prefs.getString(SEGMENT_ID_KEY,null);

            Timber.d("Segment ID: " + segmentId);

            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {

                    Timber.d("Sending locations to LocationProcessorService");
                    Intent locationIntent = new Intent(context, LocationProcessorService.class);
                    locationIntent.putExtra(LOCATIONS_EXTRA_KEY, result);
                    locationIntent.putExtra(SEGMENT_ID_EXTRA_KEY, segmentId);

                    context.startService(locationIntent);

                }
            }else if(ACTION_PROCESS_MOCK_UPDATES.equals(action)){

                Timber.d("Handling mocked location");

                Location location = intent.getParcelableExtra(MOCK_LOCATION_EXTRA_KEY);
                if (location != null){
                    Timber.d("Recovered location from intent");
                    Timber.d("Lat:  " + Double.toString(location.getLatitude()));
                    //put location in list
                    List<Location> locations = new ArrayList<>();
                    locations.add(location);
                    LocationResult locationResult = LocationResult.create(locations);
                    Intent locationIntent = new Intent(context, LocationProcessorService.class);
                    locationIntent.putExtra(LOCATIONS_EXTRA_KEY, locationResult);
                    locationIntent.putExtra(SEGMENT_ID_EXTRA_KEY, segmentId);
                    context.startService(locationIntent);

                }

            }
        }
    }
}