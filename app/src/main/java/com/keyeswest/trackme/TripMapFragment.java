package com.keyeswest.trackme;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.data.LocationLoader;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.interfaces.UpdateMap;
import com.keyeswest.trackme.models.DurationRecord;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.tasks.EmailMapTask;
import com.keyeswest.trackme.utilities.LatLonBounds;
import com.keyeswest.trackme.utilities.PluralHelpers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.TripMapActivity.EXTRA_URI;
import static com.keyeswest.trackme.utilities.ZoomLevels.CITY_ZOOM;
import static com.keyeswest.trackme.utilities.ZoomLevels.STREET_ZOOM;


/**
 * Displays trips selected from trip list on a map.
 */
public class TripMapFragment extends Fragment  implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor> , UpdateMap {

    public static TripMapFragment newInstance(Boolean isTwoPane, ArrayList<Uri> tripList){
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_URI, tripList);
        args.putBoolean(TWO_PANE_EXTRA, isTwoPane);
        TripMapFragment fragment = new TripMapFragment();
        fragment.setArguments(args);

        return fragment;
    }


    public TripMapFragment(){}

    private static final String TWO_PANE_EXTRA = "twoPaneExtra";


    //available colors for up to 4 plotted segments
    private static final int[] plotLineColorResources = {R.color.plotOne,
            R.color.plotTwo, R.color.plotThree, R.color.plotFour};

    private Unbinder mUnbinder;
    @BindView(R.id.segment_one_view)
    View mSegmentOne;

    @BindView(R.id.switch1)
    Switch mShowHideSegmentOne;

    @BindView(R.id.segment_two_view)
    View mSegmentTwo;

    @BindView(R.id.switch2)
    Switch mShowHideSegmentTwo;

    @BindView(R.id.segment_three_view)
    View mSegmentThree;

    @BindView(R.id.switch3)
    Switch mShowHideSegmentThree;

    @BindView(R.id.segment_four_view)
    View mSegmentFour;

    @BindView(R.id.switch4)
    Switch mShowHideSegmentFour;

    private TextView[] mShowHide;

    // these views are just the colored line segments in the legend
    private View[] mTripViews;

    // List of all segment Uris to be plotted on map
    private List<Uri> mMasterSegmentUriList;

    // List of segments to load from database (supports tablet mode where trips can be added after
    // map is drawn)
    private List<Uri> mLoadSegmentUriList;

    // list of polylines currently plotted on the map
    private List<Polyline> mPolyLines = new ArrayList<>();

    // generates plot data on a background thread
    private SegmentPlotter<Polyline> mSegmentPlotter;

    private SegmentCursor mSegmentCursor;

    // list of retrieved segments from the database
    private List<Segment> mSegmentList;

    private boolean mSegmentDataReady=false;

    // a separate load is used to load locations for each segment, this tracks the location loads
    // that have completed.
    private int mLocationLoadsFinishedCount;

    private boolean mMapReady=false;

    // Look up segments by URI
    private Hashtable<Uri, LocationCursor> mSegmentToLocationsMap = new Hashtable<>();

    // Look up segment associated with locations being loaded by location loader
    private Hashtable<Integer, Uri> mLocationLoaderToSegmentUri = new Hashtable<>();

    private GoogleMap mMap;

    private LayoutInflater mInflater;

    private View mRootView;

    // Indicates when current location has been returned
    private boolean mCurrentLocationReady = false;
    private Location mLastLocation;

    private Boolean mIsTwoPane;

    // serves as a queue for plot line line colors
    LinkedList<Integer> mPlotLineColors = new LinkedList<>();

    //used to generate loader ids
    private Random mRandom = new Random();

    // Set of ids associated with segment loaders
    private Set<Integer> mSegmentLoaderIds = new HashSet<>();

    private List<File> mEmailAttachments = new ArrayList<>();

    private Menu mMenu;

    private PopupWindow mPopupWindow;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate TripMapFragment invoked");

        setHasOptionsMenu(true);

        mMasterSegmentUriList = getArguments().getParcelableArrayList(EXTRA_URI);
        mIsTwoPane = getArguments().getBoolean(TWO_PANE_EXTRA);

        // Handles plotting a batch of location points
        Handler responseHandler = new Handler();

        // SegmentPlotter implements algorithm for plotting trip
        mSegmentPlotter = new SegmentPlotter<>(responseHandler);

        mSegmentPlotter.setSegmentPlotterListener(new SegmentPlotter.SegmentPlotterListener<Polyline>() {
            @Override
            public void plotLocation(Polyline plotLine, List<LatLng> newPoints ){
               // Timber.d("Line color= " + Integer.toString(plotLine.getColor()));
                List<LatLng> points = plotLine.getPoints();
                points.addAll(newPoints);
                plotLine.setPoints(points);

            }
        });

        mSegmentPlotter.start();
        mSegmentPlotter.getLooper();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        mInflater = inflater;

        View view = inflater.inflate(R.layout.fragment_map,
                container, false);

        for (int i : plotLineColorResources){
            //append colors to list (enqueue)
            mPlotLineColors.add(getResources().getColor(i));
        }

        mUnbinder = ButterKnife.bind(this, view);
        mTripViews =  new View[] {mSegmentOne, mSegmentTwo, mSegmentThree, mSegmentFour};

        mShowHide = new TextView[] {mShowHideSegmentOne, mShowHideSegmentTwo, mShowHideSegmentThree,
                mShowHideSegmentFour};

        mShowHideSegmentOne.setOnClickListener(mTripOneListener);

        mShowHideSegmentTwo.setOnClickListener(mTripTwoListener);

        mShowHideSegmentThree.setOnClickListener(mTripThreeListener);

        mShowHideSegmentFour.setOnClickListener(mTripFourListener);


        displayLegend();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mLoadSegmentUriList = mMasterSegmentUriList;

        // Initialize the loader that will retrieve the segments corresponding to the
        // list of segment URIs provided to the Activity. Segments will be retrieved
        // and then the corresponding location data will be loaded.
        if ( mLoadSegmentUriList.size() > 0) {
            Timber.d("Initializing Segment Loader");

           // Bundle args = createLoaderArgument(true);
            int loaderId = mRandom.nextInt();
            mSegmentLoaderIds.add(loaderId);
            getLoaderManager().initLoader(loaderId, null, this);
        }else{
            //Initially, when on a tablet in landscape mode an empty map with just the user's position
            // is displayed until the user selects a trip to plot

            // no need to load segments or location data
            mSegmentList = new ArrayList<>();
            setSegmentDataReady(true);
            mLocationLoadsFinishedCount = 0;
            if (getDataReady() && getMapReady()){
                displayMap();
            }
        }

        mRootView = view;

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.fragment_trip_map, menu);
        mMenu = menu;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.share:
                captureScreen();
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onDestroyView(){
        // Handle case where user exits activity with trip dialog showing. Eliminate memory leak.
        if ((mPopupWindow != null ) && (mPopupWindow.isShowing())) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }

        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy(){
        Timber.d("onDestroy TripMapFragment");
        mSegmentPlotter.quitSafely();

        for(Polyline plotLine : mPolyLines){
            plotLine.setTag(null);
        }

        for (File file : mEmailAttachments){
            file.delete();
        }

        super.onDestroy();
    }


    /**
     * Allows user to hide and show individual trip plots when on single pane (phone) devices.
     */
    private void displayLegend(){

        if (! mIsTwoPane) {
            int segmentsToShow = mMasterSegmentUriList.size();

            if (segmentsToShow < 2) {
                // no need to show if only 1 plot is being displayed
                return;
            }

            for (int i = 0; i < segmentsToShow; i++) {
                mTripViews[i].setVisibility(View.VISIBLE);
                mShowHide[i].setVisibility(View.VISIBLE);
            }

            for (int i = segmentsToShow; i < TripListFragment.MAX_TRIP_SELECTIONS; i++) {
                mTripViews[i].setVisibility(View.INVISIBLE);
                mShowHide[i].setVisibility(View.INVISIBLE);
            }
        }

    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Timber.d("Create Loader id= %s", Integer.toString(id));


        boolean isSegment = mSegmentLoaderIds.contains(id);
        if (isSegment) {
            // All the segments in the segment list will be loaded with a single database query
            Timber.d("Loading segments");

            return SegmentLoader.newSegmentsFromUriList(getContext(), mLoadSegmentUriList);

        } else {

            Timber.d("Loading locations. Location Loader id= %s", Integer.toString(id));
           // Timber.d("Request load of segment uri = "+ mLoadSegmentUriList.get(id - LOCATION_LOADER).toString());

            return LocationLoader.newLocationsForSegment(getContext(),
                    mLocationLoaderToSegmentUri.get(id));
        }

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Timber.d("onLoadFinished, loader id = %s", Integer.toString(loader.getId()));
        boolean isSegment = mSegmentLoaderIds.contains(loader.getId());
        if (isSegment){
            mSegmentLoaderIds.remove(loader.getId());
            mSegmentCursor = new SegmentCursor(data);
            Timber.d("Number segments loaded = %s", Integer.toString(mSegmentCursor.getCount()));
            mSegmentList = new ArrayList<>();

            mSegmentCursor.moveToPosition(-1);
            while(mSegmentCursor.moveToNext()){
                Segment segment = mSegmentCursor.getSegment();
                Timber.d("Loaded Segment Info");
                Timber.d("Segment uuid= " + segment.getId().toString());
                Timber.d("Segment rowid= " + segment.getRowId());
                mSegmentList.add(segment);
            }

            setSegmentDataReady(true);

            getLoaderManager().destroyLoader(loader.getId());

            // start loading the locations for each segment
            mLocationLoadsFinishedCount = 0;

            for (int i=0; i< mSegmentList.size(); i++){

                int loaderId = mRandom.nextInt();
                Timber.d("Creating Location Loader with id= %s",loaderId );
                mLocationLoaderToSegmentUri.put(loaderId,mSegmentList.get(i).getSegmentUri() );

                getLoaderManager().initLoader(loaderId,null, this);
            }

        } else {
            Timber.d("Location Loader finished");

            // handle the completed location data loads
            LocationCursor locationCursor = new LocationCursor(data);
            Timber.d("Number locations loaded = %s", Integer.toString(locationCursor.getCount()));

            Uri segmentUri =  mLocationLoaderToSegmentUri.get(loader.getId());

            //Timber.d("adding segment and location cursor to hash table");
            mSegmentToLocationsMap.put(segmentUri, locationCursor);
            //debugSegmentToLocationsMap();

            // increment the count of location loaders that have completed - one load for each segment
            mLocationLoadsFinishedCount++;

        }

        if (getDataReady() && getMapReady()){
            displayMap();
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Timber.d("onLoaderReset invoked");
        boolean isSegment = mSegmentLoaderIds.contains(loader.getId());
        if (isSegment){
            setSegmentDataReady(false);
            mSegmentCursor.close();
        } else {
            List<LocationCursor> cursors = (List<LocationCursor>) mSegmentToLocationsMap.values();

            for(LocationCursor cursor : cursors){
                cursor.close();
            }

            mLocationLoadsFinishedCount = 0;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Timber.d("onMap Ready TripMapFragment");
        setMapReady(true);
        mMap = googleMap;

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener(){

            @Override
            public void onPolylineClick(Polyline polyline) {

                // disable clicking on polylines while the pop up is being displayed
                disablePolylineClicks();

                final Segment segment = (Segment)polyline.getTag();

                View customView = mInflater.inflate(R.layout.trip_popup,null);

                mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                Button okButton = customView.findViewById(R.id.ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopupWindow.dismiss();
                        enablePolylineClicks();
                    }
                });


                TextView dateView = customView.findViewById(R.id.date_tv);
                dateView.setText(segment.getDate());

                TextView distanceView = customView.findViewById(R.id.distance_tv);
                distanceView.setText(segment.getDistanceMiles());
                TextView distanceDimension = customView.findViewById(R.id.distance_unit_lbl);

                distanceDimension.setText(getContext().getResources()
                        .getQuantityString(R.plurals.miles_plural,
                                PluralHelpers.getPluralQuantity(segment.getDistance())));


                TextView startTimeView = customView.findViewById(R.id.start_time_tv);
                startTimeView.setText(segment.getTime());

                final TextView durationView = customView.findViewById(R.id.duration_tv);
                final TextView durationDimension =
                        customView.findViewById(R.id.duration_dimension_tv);

                Timber.d("Segment duration retrieved from db.");
                DurationRecord record = segment.getSegmentDuration(getContext());
                durationView.setText(record.getValue());
                durationDimension.setText(record.getDimension());


                mPopupWindow.showAtLocation(mRootView.findViewById(R.id.map), Gravity.CENTER, 0, 0);

            }
        });
        if (getDataReady()){
            displayMap();
        }
    }


    @Override
    public void addSegment(Segment segment) {
        Timber.d("Adding segment id= " + segment.getId().toString());
        Uri segmentUri = segment.getSegmentUri();
        mMasterSegmentUriList.add(segmentUri);
        mLoadSegmentUriList.clear();
        mLoadSegmentUriList.add(segmentUri);
        int loaderId = mRandom.nextInt();
        mSegmentLoaderIds.add(loaderId);
        getLoaderManager().initLoader(loaderId, null, this);
    }

    @Override
    public void removeSegment(Segment segment) {
        Timber.d("Request to Remove segment id= " + segment.getId().toString());
        Uri segmentUri = segment.getSegmentUri();
        mMasterSegmentUriList.remove(segmentUri);
        Polyline removeLine = null;
        for (Polyline pLine : mPolyLines){
            Segment pSegment = (Segment)pLine.getTag();
            if (segment.getId().equals(pSegment.getId())){
                removeLine = pLine;
            }
        }

        if (removeLine != null){
            Timber.d("Matched polyline to remove");
            // remove from map
            removeLine.remove();
            // remove from list of plotted segments
            mPolyLines.remove(removeLine);
            // make the color available
            mPlotLineColors.add(removeLine.getColor());
        }
    }

    private View.OnClickListener mTripOneListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mShowHideSegmentOne.isChecked()){
                // user requested the plot be hidden
                mPolyLines.get(0).setVisible(false);
            }else{
                mPolyLines.get(0).setVisible(true);
            }
        }
    };

    private View.OnClickListener mTripTwoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mShowHideSegmentTwo.isChecked()){
                // user requested the plot be hidden
                mPolyLines.get(1).setVisible(false);
            }else{
                mPolyLines.get(1).setVisible(true);
            }
        }
    };

    private View.OnClickListener mTripThreeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mShowHideSegmentThree.isChecked()){
                // user requested the plot be hidden
                mPolyLines.get(2).setVisible(false);
            }else{
                mPolyLines.get(2).setVisible(true);
            }
        }
    };

    private View.OnClickListener mTripFourListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mShowHideSegmentFour.isChecked()){
                // user requested the plot be hidden
                mPolyLines.get(3).setVisible(false);
            }else{
                mPolyLines.get(3).setVisible(true);
            }
        }
    };


    private  boolean getSegmentDataReady(){
        return mSegmentDataReady;
    }

    private  void setSegmentDataReady(boolean value){
        mSegmentDataReady = value;
    }

    private  boolean getLocationDataReady(){
        return (mLocationLoadsFinishedCount == mLoadSegmentUriList.size());
    }


    private  boolean getDataReady(){
        return getSegmentDataReady() && getLocationDataReady();
    }

    private boolean getMapReady(){
        return mMapReady;
    }

    private void displayMap(){
        LatLngBounds bounds = computeBoundingBoxForSegments();
        if (bounds != null) {
            Timber.d("Bounds: maxLat= %s", Double.toString(bounds.northeast.latitude));
            Timber.d("Bounds: maxLon= %s", Double.toString(bounds.northeast.longitude));
            Timber.d("Bounds: minLat= %s", Double.toString(bounds.southwest.latitude));
            Timber.d("Bounds: minLon= %s", Double.toString(bounds.southwest.longitude));

            int zoomLevel;

            // On a table in landscape mode a map with the user's current position is shown on the
            // right side of the screen until a trip is selected for viewing.
            if (mSegmentList.size() == 0){
                zoomLevel = CITY_ZOOM;
            }else{
                // zoom in a bit more if we are displaying trip tracts
                zoomLevel = STREET_ZOOM;
            }

            //  mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, zoomLevel));
            mMap.moveCamera( CameraUpdateFactory.zoomTo( zoomLevel ) );
            // begin new code:
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.moveCamera(cu);


            //debugSegmentToLocationsMap();
            for (Segment segment : mSegmentList) {

                //Timber.d("Constructing segment uri, segment rowid= " + Long.toString(segment.getRowId()));
                Uri segmentUri = SegmentSchema.SegmentTable.buildItemUri(segment.getRowId());
                //Timber.d("Constructed segment uri= " + segmentUri.toString());

                LocationCursor locationCursor = mSegmentToLocationsMap.get(segmentUri);
                if (locationCursor == null){
                    // Error state
                    Timber.d("Invalid cursor for segemnt uri: " +  segmentUri.toString());
                }

                PolylineOptions options = new PolylineOptions()
                        .color( mPlotLineColors.remove())
                        .clickable(true);

                Polyline plotLine = mMap.addPolyline(options);
                // setting the tag create association between polyline and the segment it corresponds to
                plotLine.setTag(segment);
                mPolyLines.add(plotLine);

                locationCursor.moveToPosition(-1);
                mSegmentPlotter.queueSegment(plotLine, locationCursor);

            }
        }
    }

/*
    private void debugSegmentToLocationsMap(){
        Timber.d("Size of SegmentToLocationsMap= " + Integer.toString(mSegmentToLocationsMap.size()));
        int index = 0;
        for (Map.Entry<Uri,LocationCursor> entry : mSegmentToLocationsMap.entrySet()) {
            Timber.d("Index= " + Integer.toString(index));
            Timber.d("Key = " + entry.getKey().toString());
            Timber.d("Value = "  + entry.getValue().getCount());
        }
    }
    */

    //Permissions were granted when app first installed.
    @SuppressLint("MissingPermission")
    private LatLngBounds computeBoundingBoxForSegments(){
        LatLngBounds bounds = null;
        if (mSegmentList.size() == 0){

            // Obtain the current position of the user so an empty map with their
            // position is shown until a trip is selected for viewing
            if (mCurrentLocationReady){
                // the user's position has been obtained (asynch method)
                bounds = new LatLngBounds(new LatLng(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude()), new LatLng(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude()));
                mMap.setMyLocationEnabled(true);
            }else{

                // get the user's position
                getLastLocation();
            }

        }else {

            // the bounding box for the map is determined by the trip plot data
            LatLonBounds boundingBox = new LatLonBounds();

            for (Segment segment : mSegmentList) {
                boundingBox.update(segment.getMinLatitude(), segment.getMinLongitude());
                boundingBox.update(segment.getMaxLatitude(), segment.getMaxLongitude());
            }

            bounds = new LatLngBounds(new LatLng(boundingBox.getMinLat(),
                    boundingBox.getMinLon()), new LatLng(boundingBox.getMaxLat(),
                    boundingBox.getMaxLon()));
        }

        return bounds;
    }


    //Permissions were granted when app first installed.
    @SuppressWarnings("MissingPermission")
    private void getLastLocation(){

        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient =LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(getActivity(),new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null){
                            mLastLocation = task.getResult();
                            Timber.d("Current Location lat= %s", Double.toString(mLastLocation.getLatitude()));
                            Timber.d("Current Location lon= %s", Double.toString(mLastLocation.getLongitude()));

                            mCurrentLocationReady = true;
                            displayMap();

                        }else{
                            // handle error case where location not known
                        }
                    }
                });
    }

    private  void setMapReady(boolean value){
        mMapReady = value;
    }

    private void disablePolylineClicks(){
        for (Polyline p: mPolyLines){
            p.setClickable(false);
        }
    }

    private void enablePolylineClicks(){
        for (Polyline p: mPolyLines){
            p.setClickable(true);
        }
    }


    private void captureScreen(){
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {

                new EmailMapTask(getContext(), bitmap, new EmailMapTask.ResultsCallback() {
                    @Override
                    public void onComplete(File file) {
                        if (file != null){
                            mEmailAttachments.add(file);
                        }
                    }
                }).execute();

            }
        };

        // take a picture of the map
        if ( (mMap != null) && getMapReady()){
            mMap.snapshot(callback);
        }
    }

}
