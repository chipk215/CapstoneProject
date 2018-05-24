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

/**
 * This service receives location samples and the segment identifier corresponding to the segment
 * that the locations belong to. Arriving Location samples are packaged in androids LocationResult
 * objects.
 *
 * The service computes the bounding box for the location samples and the incremental segment
 * distance increase associated with each location sample.
 *
 * After the segment computations have been made the segment data is updated in the database and
 * each location sample is saved in the database.
 *
 * Finally, the location samples are broadcast back to the NewTripActivity for plotting on the map.
 *
 */
public class LocationProcessorService extends IntentService {

    private final static String NAME="LocationProcessorService";

    // Location sample must be at least 9 meters from last location in order to save the sample
    private final static int DISTANCE_THRESHOLD = 9;

    // Extra keys for accessing the location and segment data
    public final static String LOCATIONS_EXTRA_KEY = "locationsKey";
    public final static String SEGMENT_ID_EXTRA_KEY = "segmentIdKey";

    // Intent and extra for sending locations to be plotted
    public final static String LOCATION_BROADCAST_PLOT_SAMPLE =
            "com.keyeswest.trackme.location.samples.intent.action.PLOT_SAMPLE";
    public final static String PLOT_SAMPLES_EXTRA_KEY = "sampleData";

    // Debug counter  TODO remove from release
    public static long debugSampleCount =0;

    public LocationProcessorService() {
        super(NAME);
    }


    /**
     * A new location sample (or samples) has arrived.
     *
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Double segmentDistance=0d;

        // holds location returned from database
        com.keyeswest.trackme.models.Location previousLocation;

        Timber.d("Entering LocationProcessorService onHandleIntent");

        if (intent != null) {
            // retrieve the LocationResult data from intent
            Location location = intent.getParcelableExtra(LOCATIONS_EXTRA_KEY);

            if (location != null)  {

                String segmentId = intent.getStringExtra(SEGMENT_ID_EXTRA_KEY);
                Timber.d("SegmentId= " + segmentId);


                // retrieve the last location belonging to this segment from the db
                LocationCursor previousLocationCursor = Queries.getLatestLocationBySegmentId(
                        this, segmentId);

                LatLonBounds bounds=null;

                // We are going to need to read the segment record
                Segment segment = Queries.getSegmentFromSegmentId(this,segmentId);
                if (segment == null){
                    Timber.d("Failed to get segment from database");
                    return;
                }

                if (previousLocationCursor.getCount() == 1){
                    // a previous location exists for the segment, this is not the first location
                    previousLocationCursor.moveToFirst();
                    previousLocation = previousLocationCursor.getLocation();


                    // need the distance between lastSample and previousLocation
                    float[] results = new float[1];
                    Location.distanceBetween(previousLocation.getLatitude(),
                            previousLocation.getLongitude(),
                            location.getLatitude(),
                            location.getLongitude(),
                            results);
                    double incrementDistance = results[0];
                    Timber.d("Distance measured between: %s %s %s %s ",
                            Double.toString(previousLocation.getLatitude()),
                            Double.toString(previousLocation.getLongitude()),
                            Double.toString(location.getLatitude()),
                            Double.toString(location.getLongitude()));

                    Timber.d("Segment increment distance = %s",
                            Double.toString(incrementDistance));

                    if (incrementDistance > DISTANCE_THRESHOLD  ){

                        // save the location samples to the db and compute the bounding box
                        saveLocationSamples(location, segmentId);
                        bounds = adjustSegmentBounds(segment, location);

                        segmentDistance = segment.getDistance() + incrementDistance;

                    }else{
                        Timber.d("Discarding location sample. Location change not greater than threshold");
                    }


                }else{
                    // first sample so save it
                    // save the location samples to the db and compute the bounding box
                    saveLocationSamples(location, segmentId);
                    // the database is returning 0's for uninitialized segment max,min coordinates
                    bounds = adjustSegmentBounds(segment, location);

                }


                //update segment with distance and bounding box data
                if (bounds != null) {
                    Queries.updateSegmentBoundsDistance(this, segmentId,
                            bounds.getMinLat(), bounds.getMaxLat(),
                            bounds.getMinLon(), bounds.getMaxLon(),
                            segmentDistance);

                    // broadcast the location samples for plotting
                    broadcastLocationSamples(location);
                }
            }
        }
        Timber.d("Sample Count = %s", Long.toString(debugSampleCount));
    }



    private LatLonBounds adjustSegmentBounds(Segment segment, Location location){

        LatLonBounds bounds = new LatLonBounds();
        if (segment.getMinLatitude() != null) {
            bounds.setMinLat(min(location.getLatitude(), segment.getMinLatitude()));
        }else{
            bounds.setMinLat(location.getLatitude());
        }

        if (segment.getMinLongitude() != null) {
            bounds.setMinLon(min(location.getLongitude(), segment.getMinLongitude()));
        }else{
            bounds.setMinLon(location.getLongitude());
        }

        if (segment.getMaxLatitude() != null) {
            bounds.setMaxLat(max(location.getLatitude(), segment.getMaxLatitude()));
        }else{
            bounds.setMaxLat(location.getLatitude());
        }

        if (segment.getMaxLongitude() != null) {
            bounds.setMaxLon(max(location.getLongitude(), segment.getMaxLongitude()));
        }else{
            bounds.setMaxLon(location.getLongitude());
        }

        return bounds;
    }


    /**
     *
     * Write the location sample to the database.
     *
     *
     * @param location
     * @param segmentId
     * @return
     */
    private void saveLocationSamples(Location location, String segmentId){



        // save the new location samples
        Timber.d("Displaying locations from Service");
        Timber.d("++++++++++++++++");


        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Date d = new Date(location.getTime());
        Timber.d("Date: " + d.toString());
        Timber.d("Lat: " + Double.toString(latitude));
        Timber.d("Lon: " + Double.toString(longitude));
        Timber.d("++++++++++++++++");

        // save location sample to db
        createNewLocationFromSample(this, location, segmentId);
        debugSampleCount++;


    }


    // Send the samples back to NewTrip activity to be plotted.
    private void broadcastLocationSamples(Location location){
        Intent intent = new Intent();
        intent.setAction(LOCATION_BROADCAST_PLOT_SAMPLE);

        intent.putExtra(PLOT_SAMPLES_EXTRA_KEY, location);

        Timber.d("Broadcasting locations samples for plotting");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

}