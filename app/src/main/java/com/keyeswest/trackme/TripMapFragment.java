package com.keyeswest.trackme;

import android.database.Cursor;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.keyeswest.trackme.data.LocationCursor;
import com.keyeswest.trackme.data.LocationLoader;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.models.DurationRecord;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.tasks.ComputeSegmentDurationTask;
import com.keyeswest.trackme.utilities.LatLonBounds;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.TripMapActivity.EXTRA_URI;

public class TripMapFragment extends Fragment  implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static TripMapFragment newInstance(ArrayList<Uri> tripList){
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_URI, tripList);

        TripMapFragment fragment = new TripMapFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public TripMapFragment(){}


    private static final int SEGMENT_LOADER  = 0;
    private static final int LOCATION_LOADER = 1;

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

    private List<Uri> mSegmentUriList;

    private List<Polyline> mPolyLines = new ArrayList<>();

    private SegmentPlotter<Polyline> mSegmentPlotter;

    private SegmentCursor mSegmentCursor;

    private List<Segment> mSegmentList;

    private boolean mSegmentDataReady=false;

    private int mLocationLoadsFinishedCount;

    private boolean mMapReady=false;

    // Look up segments by URI
    private Hashtable<Uri, LocationCursor> mSegmentToLocationsMap = new Hashtable<>();

    private GoogleMap mMap;

    private LayoutInflater mInflater;

    private View mRootView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSegmentUriList = getArguments().getParcelableArrayList(EXTRA_URI);

        // Handles plotting a batch of location points
        Handler responseHandler = new Handler();

        // SegmentPlotter implements algorithm for plotting trip
        mSegmentPlotter = new SegmentPlotter<>(responseHandler);

        mSegmentPlotter.setSegmentPlotterListener(new SegmentPlotter.SegmentPlotterListener<Polyline>() {
            @Override
            public void plotLocation(Polyline plotLine, List<LatLng> newPoints ){
                Timber.d("Line color= " + Integer.toString(plotLine.getColor()));
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

        // Initialize the loader that will retrieve the segments corresponding to the
        // list of segment URIs provided to the Activity. Segments will be retrieved
        // and then the corresponding location data will be loaded.
        getActivity().getSupportLoaderManager().initLoader(SEGMENT_LOADER,null, this);

        mRootView = view;
        return view;
    }


    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy(){
        mSegmentPlotter.quitSafely();

        for(Polyline plotLine : mPolyLines){
            plotLine.setTag(null);
        }

        super.onDestroy();
    }


    private void displayLegend(){
        int segmentsToShow = mSegmentUriList.size();

        if (segmentsToShow < 2){
            return;
        }

        for (int i=0; i< segmentsToShow; i++){
            mTripViews[i].setVisibility(View.VISIBLE);
            mShowHide[i].setVisibility(View.VISIBLE);
        }

        for (int i=segmentsToShow; i< TripListFragment.MAX_TRIP_SELECTIONS; i++){
            mTripViews[i].setVisibility(View.INVISIBLE);
            mShowHide[i].setVisibility(View.INVISIBLE);
        }

    }



    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == SEGMENT_LOADER) {
            // All the segments in the segment list will be loaded with a single database query
            return SegmentLoader.newSegmentsFromUriList(getContext(), mSegmentUriList);
        } else if (id >= LOCATION_LOADER) {
            //See notes on id for location loader in onLoadFinished method below.


            return LocationLoader.newLocationsForSegment(getContext(),
                    mSegmentUriList.get(id - LOCATION_LOADER));
        }

        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == SEGMENT_LOADER){
            mSegmentCursor = new SegmentCursor(data);

            mSegmentList = new ArrayList<>();

            mSegmentCursor.moveToPosition(-1);
            while(mSegmentCursor.moveToNext()){
                Segment segment = mSegmentCursor.getSegment();
                mSegmentList.add(segment);
            }

            setSegmentDataReady(true);

            // start loading the locations for each segment
            mLocationLoadsFinishedCount = 0;

            for (int i=0; i< mSegmentList.size(); i++){

                // Each segment requires a separate load to obtain the location data
                // The first location loader will use the LOCATION_LOADER value and each
                // successive location load will increase the LOCATION_LOADER value by one
                getActivity().getSupportLoaderManager().initLoader(LOCATION_LOADER + i,null, this);
            }

        } else if(loader.getId() >= LOCATION_LOADER){
            // handle the completed location data loads
            LocationCursor locationCursor = new LocationCursor(data);
            Uri segmentUri = mSegmentUriList.get(loader.getId() - LOCATION_LOADER);

            mSegmentToLocationsMap.put(segmentUri, locationCursor);
            // mPlotLocations.add(locationCursor);
            // increment the count of location loaders that have completed
            mLocationLoadsFinishedCount++;

        }

        if (getDataReady() && getMapReady()){
            displayMap();
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == SEGMENT_LOADER){
            setSegmentDataReady(false);
            mSegmentCursor.close();
        } else if (loader.getId() >= LOCATION_LOADER){
            List<LocationCursor> cursors = (List<LocationCursor>) mSegmentToLocationsMap.values();

            for(LocationCursor cursor : cursors){
                cursor.close();
            }

            mLocationLoadsFinishedCount = 0;
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setMapReady(true);
        mMap = googleMap;

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener(){

            @Override
            public void onPolylineClick(Polyline polyline) {

                // disable clicking on polylines while the pop up is being displayed
                disablePolylineClicks();

                final Segment segment = (Segment)polyline.getTag();



                View customView = mInflater.inflate(R.layout.trip_popup,null);
                final PopupWindow popup = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                Button okButton = customView.findViewById(R.id.ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                        enablePolylineClicks();
                    }
                });


                TextView dateView = customView.findViewById(R.id.date_tv);
                dateView.setText(segment.getDate());

                TextView distanceView = customView.findViewById(R.id.distance_tv);
                distanceView.setText(segment.getDistanceMiles());

                TextView startTimeView = customView.findViewById(R.id.start_time_tv);
                startTimeView.setText(segment.getTime());

                final TextView durationView = customView.findViewById(R.id.duration_tv);
                final TextView durationDimension =
                        customView.findViewById(R.id.duration_dimension_tv);

                if (segment.getElapsedTime() == 0){
                    new ComputeSegmentDurationTask(getContext(),
                            new ComputeSegmentDurationTask.ResultsCallback() {
                                @Override
                                public void onComplete(Long duration) {
                                    segment.setElapsedTime(duration);
                                    DurationRecord record = segment.getSegmentDuration(getContext());
                                    durationView.setText(record.getValue());
                                    durationDimension.setText(record.getDimension());

                                }
                            }).execute(segment.getId().toString());
                }else{

                    Timber.d("Segment duration retrieved from db.");
                    DurationRecord record = segment.getSegmentDuration(getContext());
                    durationView.setText(record.getValue());
                    durationDimension.setText(record.getDimension());
                }


                popup.showAtLocation(mRootView.findViewById(R.id.map), Gravity.CENTER, 0, 0);

            }
        });
        if (getDataReady()){
            displayMap();
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
        return (mLocationLoadsFinishedCount == mSegmentUriList.size());
    }


    private  boolean getDataReady(){
        return getSegmentDataReady() && getLocationDataReady();
    }

    private boolean getMapReady(){
        return mMapReady;
    }

    private void displayMap(){

        LatLngBounds bounds = computeBoundingBoxForSegments();
        Timber.d("Bounds: maxLat= %s", Double.toString(bounds.northeast.latitude));
        Timber.d("Bounds: maxLon= %s", Double.toString(bounds.northeast.longitude));
        Timber.d("Bounds: minLat= %s", Double.toString(bounds.southwest.latitude));
        Timber.d("Bounds: minLon= %s", Double.toString(bounds.southwest.longitude));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 16));

        int plotLineCounter =0;
        for(Segment segment : mSegmentList){

            Uri segmentUri = SegmentSchema.SegmentTable.buildItemUri(segment.getRowId());
            LocationCursor locationCursor = mSegmentToLocationsMap.get(segmentUri);


            PolylineOptions options = new PolylineOptions()
                    .color(getResources().getColor(plotLineColorResources[plotLineCounter++]))
                    .clickable(true);

            Polyline plotLine = mMap.addPolyline(options);
            plotLine.setTag(segment);
            mPolyLines.add(plotLine);

            locationCursor.moveToPosition(-1);
            mSegmentPlotter.queueSegment(plotLine, locationCursor);

        }
    }

    private LatLngBounds computeBoundingBoxForSegments(){
        LatLonBounds boundingBox = new LatLonBounds();

        for(Segment segment : mSegmentList){
            boundingBox.update(segment.getMinLatitude(), segment.getMinLongitude());
            boundingBox.update(segment.getMaxLatitude(), segment.getMaxLongitude());
        }

        LatLngBounds bounds = new LatLngBounds(new LatLng(boundingBox.getMinLat(),
                boundingBox.getMinLon()), new LatLng(boundingBox.getMaxLat(),
                boundingBox.getMaxLon()));

        return bounds;
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
}
