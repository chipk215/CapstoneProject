package com.keyeswest.trackme.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationResult;
import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.data.Queries;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.utilities.LatLonBounds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

import static com.keyeswest.trackme.data.Queries.createNewLocationFromSample;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class LocationProcessorService extends IntentService {

    private final static String NAME="LocationProcessorService";
    public final static String LOCATIONS_EXTRA_KEY = "locationsKey";
    public final static String SEGMENT_ID_EXTRA_KEY = "segmentIdKey";

    public final static String LOCATION_BROADCAST_PLOT_SAMPLE =
            "com.keyeswest.trackme.location.samples.intent.action.PLOT_SAMPLE";

    public final static String PLOT_SAMPLES_EXTRA_KEY = "sampleData";


    public static long debugSampleCount =0;



    public LocationProcessorService() {
        super(NAME);
    }


    /**
     * A new location sample (or samples) has arrived.
     *
     * Generally, we want to save the location in the db and update the segment to which the
     * location belongs.
     *
     * Updating the segment includes updating the segment distance by computing the distance
     * the new location is from the last location sample (unless this is the first location of
     * the segment, and then updating the bounding lat/lon box associated with the segment.
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Double segmentDistance=0d;


        Timber.d("Entering LocationProcessorService onHandleIntent");
        if (intent != null) {
            LocationResult result = intent.getParcelableExtra(LOCATIONS_EXTRA_KEY);

            //get the location samples
            List<Location> locations = result.getLocations();

            if ((locations != null) && (locations.size() > 0)) {

                String segmentId = intent.getStringExtra(SEGMENT_ID_EXTRA_KEY);
                Timber.d("SegmentId= " + segmentId);

                com.keyeswest.trackme.models.Location previousLocation = null;

                // retrieve the last location belonging to this segment from the db
                LocationCursor previousLocationCursor = Queries.getLatestLocationBySegmentId(
                        this, segmentId);

                // bounding box for new samples
                LatLonBounds bounds = saveLocationSamples(locations, segmentId);

                // broadcast the location samples for plotting
                broadcastLocationSamples(locations);

                if (previousLocationCursor.getCount() == 1){
                    // a previous location exists for the segment
                    previousLocationCursor.moveToFirst();
                    previousLocation = previousLocationCursor.getLocation();

                    Date d = new Date(previousLocation.getTimeStamp());
                    Timber.d("Previous timestamp: " + d.toString() );

                    // we are going to need to read the segment record
                    Segment segment = Queries.getSegmentFromSegmentId(this,segmentId);

                    bounds = adjustSegmentBounds(segment, bounds);

                    // get the last location sample in this sample batch
                    Location lastSample = locations.get(locations.size()-1);
                    // need the distance between lastSample and previousLocation
                    float[] results = new float[1];
                    Location.distanceBetween(previousLocation.getLatitude(),
                            previousLocation.getLongitude(),
                            lastSample.getLatitude(),
                            lastSample.getLongitude(),
                            results);
                    double incrementDistance = results[0];
                    Timber.d("Distance measured between: %s %s %s %s ",
                            Double.toString(previousLocation.getLatitude()),
                            Double.toString(previousLocation.getLongitude()),
                            Double.toString(lastSample.getLatitude()),
                            Double.toString(lastSample.getLongitude()));

                    Timber.d("Segment increment distance = %s",
                            Double.toString(incrementDistance));

                    segmentDistance = segment.getDistance() + incrementDistance;


                }

                //update segment with distance and bounding box data
                Queries.updateSegmentBoundsDistance(this, segmentId,
                        bounds.getMinLat(), bounds.getMaxLat(),
                        bounds.getMinLon(), bounds.getMaxLon(),
                        segmentDistance);

            }
        }

        Timber.d("Sample Count = %s", Long.toString(debugSampleCount));

    }


    private LatLonBounds adjustSegmentBounds(Segment segment, LatLonBounds bounds){

        if (segment.getMinLatitude() != null) {
            bounds.setMinLat(min(bounds.getMinLat(), segment.getMinLatitude()));
        }

        if (segment.getMinLongitude() != null) {
            bounds.setMinLon(min(bounds.getMinLon(), segment.getMinLongitude()));
        }

        if (segment.getMaxLatitude() != null) {
            bounds.setMaxLat(max(bounds.getMaxLat(), segment.getMaxLatitude()));
        }

        if (segment.getMaxLongitude() != null) {
            bounds.setMaxLon(max(bounds.getMaxLon(), segment.getMaxLongitude()));
        }


        return bounds;
    }


    private LatLonBounds saveLocationSamples(List<Location> locations, String segmentId){

        LatLonBounds bounds = new LatLonBounds();

        // save the new location samples
        Timber.d("Displaying locations from Service");
        Timber.d("++++++++++++++++");
        for (Location l : locations) {

            double latitude = l.getLatitude();
            double longitude = l.getLongitude();

            Date d = new Date(l.getTime());
            Timber.d("Date: " + d.toString());
            Timber.d("Lat: " + Double.toString(latitude));
            Timber.d("Lon: " + Double.toString(longitude));
            Timber.d("++++++++++++++++");

            // save location sample to db
            debugSampleCount++;
            createNewLocationFromSample(this, l, segmentId);

            bounds.update(latitude, longitude);

        }

        return bounds;

    }


    private void broadcastLocationSamples(List<Location> locations){
        Intent intent = new Intent();
        intent.setAction(LOCATION_BROADCAST_PLOT_SAMPLE);

        ArrayList<Location> arrayList = new ArrayList<>(locations);

        intent.putParcelableArrayListExtra(PLOT_SAMPLES_EXTRA_KEY, arrayList);

        Timber.d("Broadcasting locations samples for plotting");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }


}