package com.keyeswest.trackme;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.keyeswest.trackme.adapters.TrackLogAdapter;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.tasks.DeleteTripTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public class TripListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, TrackLogAdapter.SegmentClickListener{

    private static final int MAX_TRIP_SELECTIONS = 4;
    public static final String ARG_SELECTED_SEGMENTS = "argSelectedSegments";

    private Unbinder mUnbinder;

    private View mFragmentView;

    @BindView(R.id.track_log_recycler_view)
    RecyclerView mTrackLogListView;

    @BindView(R.id.display_btn)
    Button mDisplayButton;

    private List<Segment> mSelectedSegments;

    private TrackLogAdapter mTrackLogAdapter;

    public TripListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        Timber.d("onCreate invoked");

        Stetho.initializeWithDefaults(getContext());

        if (savedInstanceState != null){
            mSelectedSegments = savedInstanceState.getParcelableArrayList(ARG_SELECTED_SEGMENTS);

        }else {
            mSelectedSegments = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragmemt_trip_list, container, false);
        mFragmentView = view;
        mUnbinder = ButterKnife.bind(this, view);

        Timber.d("onCreateView invoked");


        mDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Uri> selectedTrips = new ArrayList<>();
                for (Segment segment : mSelectedSegments){
                    Uri itemUri = SegmentSchema.SegmentTable.buildItemUri(segment.getRowId());
                    selectedTrips.add(itemUri);
                }

                if (selectedTrips.size() > 0){
                    // plot them
                    Intent intent = MapsActivity.newIntent(getContext(), selectedTrips);
                    startActivity(intent);
                }
            }

        });

        mTrackLogListView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.custom_list_divider));
        mTrackLogListView.addItemDecoration(itemDecorator);


        getActivity().getSupportLoaderManager().initLoader(0, null, this);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Timber.d("onActivityCreated invoked");

    }

    @Override
    public void onResume(){
        super.onResume();
        Timber.d("onResume invoked");

    }

    @Override
    public void onPause(){
        super.onPause();
        Timber.d("onPause invoked");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){

            case R.id.new_trip:
                Intent intent = NewTripActivity.newIntent(getContext());
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_trip_list, menu);
    }

    @Override
    public void onDestroyView(){
        Timber.d("onDestroyView invoked");
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Timber.d("onCreateLoader invoked");
        return SegmentLoader.newAllSegmentsInstanceOrderByDate(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Timber.d("onLoadFinished invoked");
        if (cursor != null) {
            Timber.d("Number of records = %s", cursor.getCount());
            mTrackLogAdapter = new TrackLogAdapter(new SegmentCursor(cursor),
                    mSelectedSegments, this);

            mTrackLogAdapter.setHasStableIds(true);
            mTrackLogListView.setAdapter(mTrackLogAdapter);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Timber.d("onLoaderReset invoked");
        mTrackLogAdapter = null;
        mTrackLogListView.setAdapter(null);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        Timber.d("onSaveInstanceState invoked");

        savedInstanceState.putParcelableArrayList(ARG_SELECTED_SEGMENTS,
                new ArrayList<>(mSelectedSegments) );

        super.onSaveInstanceState(savedInstanceState);

    }




    @Override
    public void onItemChecked(Segment segment) {

        mSelectedSegments.add(segment);

        if (mSelectedSegments.size() >= MAX_TRIP_SELECTIONS){
            mTrackLogAdapter.setSelectionsFrozen(true);
            showSnackbar(mFragmentView, getString(R.string.max_select_snack), Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void onItemUnchecked(Segment segment) {
        mSelectedSegments.remove(segment);

        if (mSelectedSegments.size() == (MAX_TRIP_SELECTIONS-1)){
            mTrackLogAdapter.setSelectionsFrozen(false);
        }
    }


    @Override
    public void onDeleteClick(Segment segment) {
        Toast.makeText(getContext(), segment.getId().toString(), Toast.LENGTH_SHORT).show();
        DeleteTripTask task = new DeleteTripTask(getContext());
        task.execute(segment.getId());




    }

/*
    @Override
    public void onFavoriteClick(SegmentCursor segmentCursor) {


    }

    @Override
    public void onUnFavoriteClicked(SegmentCursor segmentCursor) {

    }

 */

    private void showSnackbar(View view, String message, int duration){
        // Create snackbar
        final Snackbar snackbar = Snackbar.make(view, message, duration);

        // Set an action on it, and a handler
        snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}