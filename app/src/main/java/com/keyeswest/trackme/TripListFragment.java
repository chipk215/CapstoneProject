package com.keyeswest.trackme;

import android.app.Activity;
import android.content.Intent;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import com.facebook.stetho.Stetho;
import com.keyeswest.trackme.adapters.TrackLogAdapter;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.tasks.UpdateFavoriteStatusTask;
import com.keyeswest.trackme.utilities.FilterSharedPreferences;
import com.keyeswest.trackme.utilities.SortSharedPreferences;
import com.keyeswest.trackme.utilities.SortResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;


public class TripListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, TrackLogAdapter.SegmentClickListener{

    public static final int MAX_TRIP_SELECTIONS = 4;
    private static final String DIALOG_DELETE_CONFIRM = "dialogDeleteConfirm";

    private static final int REQUEST_TRIP_DELETE_CONFIRM = 0;
    private static final int REQUEST_SORT_PREFERENCES = 10;
    private static final int REQUEST_FILTER_PREFERENCES = 20;

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

        Timber.d("onCreate invoked");

        Stetho.initializeWithDefaults(getContext());

        setHasOptionsMenu(true);

        SortSharedPreferences.saveDefaultSortPreferences(getContext(),false);
        FilterSharedPreferences.saveDefaultFilterPreferences(getContext(), false);

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

        Intent intent;
        switch(item.getItemId()){

            case R.id.new_trip:
                intent = NewTripActivity.newIntent(getContext());
                startActivity(intent);
                return true;
            case R.id.sort:

                intent = SortActivity.newIntent(getContext());
                startActivityForResult(intent, REQUEST_SORT_PREFERENCES);
                return true;
            case R.id.filter:
                intent = FilterActivity.newIntent(getContext());
                startActivityForResult(intent, REQUEST_FILTER_PREFERENCES);
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
        return SegmentLoader.newAllSegmentsSortedByPreferences(getContext());
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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
        Timber.d("onSaveInstanceState invoked");

        savedInstanceState.putParcelableArrayList(ARG_SELECTED_SEGMENTS,
                (ArrayList<Segment>)mSelectedSegments);

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

        ConfirmDeleteDialogFragment dialog =
                ConfirmDeleteDialogFragment.newInstance(segment.getId());

        dialog.setTargetFragment(TripListFragment.this, REQUEST_TRIP_DELETE_CONFIRM);

        FragmentManager manager = getFragmentManager();
        dialog.show(manager, DIALOG_DELETE_CONFIRM);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK){
            return;
        }

        if (requestCode == REQUEST_TRIP_DELETE_CONFIRM){
            boolean deleted = data.getBooleanExtra(ConfirmDeleteDialogFragment.EXTRA_CONFIRM,
                    false);

            if (deleted){
                showSnackbar(mFragmentView, getString(R.string.trip_deleted), Snackbar.LENGTH_SHORT);
            }else{
                showSnackbar(mFragmentView, getString(R.string.trip_delete_cancel), Snackbar.LENGTH_SHORT);
            }

        } else if (requestCode == REQUEST_SORT_PREFERENCES){
            SortResult sortResult = SortActivity.getSortChangedResult(data);

            if (sortResult.isSortChanged()){

                Timber.d("Handling sort change, restarting segment loader");
                getActivity().getSupportLoaderManager().restartLoader(0, null, this);

                showSnackbar(mFragmentView,getSortMessage(sortResult.getSelectedSort()),
                        Snackbar.LENGTH_SHORT);

            }
        } else if (requestCode == REQUEST_FILTER_PREFERENCES){
            //TODO handle result
            boolean filterChanged = FilterActivity.getFilterChangedResult(data);
            if (filterChanged){
                Timber.d("Handling filter change, restarting segment loader");
                getActivity().getSupportLoaderManager().restartLoader(0, null, this);
            }
        }
    }



    @Override
    public void onFavoriteClick(Segment segment, boolean favorite) {
        new UpdateFavoriteStatusTask(getContext(), segment.getId(), favorite).execute();
    }



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

    private String getSortMessage(SortPreferenceEnum selectedSort){
        String result = getString(R.string.most_recent_sort);;
        switch(selectedSort){
            case NEWEST:
                result =  getString(R.string.most_recent_sort);
                break;
            case OLDEST:
                result =  getString(R.string.trips_ordered_oldest);
                break;
            case LONGEST:
                result =  getString(R.string.trips_ordered_longest);
                break;
            case SHORTEST:
                result =  getString(R.string.trips_ordered_shortest);
                break;
        }

        return result;
    }


}