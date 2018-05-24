package com.keyeswest.trackme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
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

import com.keyeswest.trackme.adapters.TrackLogAdapter;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.tasks.DeleteTripTask;
import com.keyeswest.trackme.tasks.UpdateFavoriteStatusTask;
import com.keyeswest.trackme.utilities.FilterSharedPreferences;
import com.keyeswest.trackme.utilities.SortSharedPreferences;
import com.keyeswest.trackme.utilities.SortResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;


public class TripListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, TrackLogAdapter.SegmentClickListener{


    public interface TripListListener{
        void onTripSelected(Segment segment);
        void onTripUnselected(Segment segment);
        void plotSelectedTrips();
    }

    public static TripListFragment newInstance(Boolean isTwoPane){
        TripListFragment fragment = new TripListFragment();
        Bundle args = new Bundle();
        args.putBoolean(TWO_PANE_EXTRA, isTwoPane);
        fragment.setArguments(args);
        return fragment;
    }

    public TripListFragment() {}

    public static final int MAX_TRIP_SELECTIONS = 4;
    public static final String ARG_SELECTED_SEGMENTS = "argSelectedSegments";
    private static final String TWO_PANE_EXTRA = "twoPaneExtra";

    private static final String FILTER_STATE_EXTRA = "filterStateExtra";
    private static final String DIALOG_DELETE_CONFIRM = "dialogDeleteConfirm";
    private static final int REQUEST_TRIP_DELETE_CONFIRM = 0;
    private static final int REQUEST_SORT_PREFERENCES = 10;
    private static final int REQUEST_FILTER_PREFERENCES = 20;


    private TripListListener mCallback;

    private Unbinder mUnbinder;

    private View mFragmentView;

    private boolean mHideDisplayButton;

    @BindView(R.id.track_log_recycler_view)
    RecyclerView mTrackLogListView;

    @BindView(R.id.display_btn)
    Button mDisplayButton;

    // List of currently selected/checked trips
    private List<Segment> mSelectedSegments;

    private TrackLogAdapter mTrackLogAdapter;

    private boolean mListFiltered= false;
    private Menu mMainMenu;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not throw an exception
        try {
            mCallback = (TripListListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement TripListListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("onCreate invoked");

        mHideDisplayButton = getArguments().getBoolean(TWO_PANE_EXTRA);

        setHasOptionsMenu(true);

        SortSharedPreferences.saveDefaultSortPreferences(getContext(),false);
        FilterSharedPreferences.clearFilters(getContext(), false);

        if (savedInstanceState != null){
            Timber.d("Restoring mSelectedSegments and filter state after config change");
            mSelectedSegments = savedInstanceState.getParcelableArrayList(ARG_SELECTED_SEGMENTS);
            mListFiltered = savedInstanceState.getByte(FILTER_STATE_EXTRA) != 0;


        }else {
            mSelectedSegments = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.d("onCreateView invoked");
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragmemt_trip_list, container, false);
        mFragmentView = view;
        mUnbinder = ButterKnife.bind(this, view);


        if (mHideDisplayButton){
            mDisplayButton.setVisibility(View.GONE);
        }
        if (mSelectedSegments.size() < 1){
            // disable the display button until a segment is checked
            mDisplayButton.setEnabled(false);
        }else{
            mDisplayButton.setEnabled(true);
        }

        mDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.plotSelectedTrips();
            }

        });

        mTrackLogListView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.custom_list_divider));
        mTrackLogListView.addItemDecoration(itemDecorator);



        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Timber.d("onActivityCreated invoked");
        getLoaderManager().initLoader(0, null, this);

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
                Timber.d("Filter request. mListFilter= " + Boolean.toString(mListFiltered));
                intent = FilterActivity.newIntent(getContext(), mListFiltered);
                startActivityForResult(intent, REQUEST_FILTER_PREFERENCES);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.fragment_trip_list, menu);
        mMainMenu = menu;

        // Restore filter icon on configuration change
        if (mListFiltered){
            Timber.d("Restoring filter icon on config change");
            MenuItem filterItem = mMainMenu.findItem(R.id.filter);
            filterItem.setIcon(R.drawable.filter_remove_outline);
            getLoaderManager().restartLoader(0, null, this);
        }
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
            SegmentCursor segmentCursor = new SegmentCursor(cursor);
            Timber.d("Number of records = %s", segmentCursor.getCount());
            mTrackLogAdapter = new TrackLogAdapter(segmentCursor,
                    mSelectedSegments, this);

            mTrackLogAdapter.setHasStableIds(true);
            mTrackLogListView.setAdapter(mTrackLogAdapter);

            // Determine whether Display button should be disabled
            segmentCursor.moveToPosition(-1);
            Segment selectedSegment = null;
            while(segmentCursor.moveToNext()){
                Segment listSegment = segmentCursor.getSegment();
                if (mSelectedSegments.contains(listSegment)){
                    selectedSegment = listSegment;
                    break;
                }
            }
            mDisplayButton.setEnabled(selectedSegment != null);
           // segmentCursor.close();
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

        // save the set of selected trips
        savedInstanceState.putParcelableArrayList(ARG_SELECTED_SEGMENTS,
                (ArrayList<Segment>)mSelectedSegments);

        // save the filter state of the list
        savedInstanceState.putByte(FILTER_STATE_EXTRA, (byte)(mListFiltered ? 1 : 0));

        super.onSaveInstanceState(savedInstanceState);

    }


    @Override
    public void onItemChecked(Segment segment) {

        mSelectedSegments.add(segment);
        Timber.d("Adding segment to selected trips: " + segment.getId().toString());

        mCallback.onTripSelected(segment);
        mDisplayButton.setEnabled(true);
        if (mSelectedSegments.size() >= MAX_TRIP_SELECTIONS){
            mTrackLogAdapter.setSelectionsFrozen(true);
            showSnackbar(mFragmentView, getString(R.string.max_select_snack), Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void onItemUnchecked(Segment segment) {

        mSelectedSegments.remove(segment);
        Timber.d("Removing segment to selected trips: " + segment.getId().toString());
        mCallback.onTripUnselected(segment);

        if (mSelectedSegments.size() < 1){
            mDisplayButton.setEnabled(false);
        }

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
            boolean confirmDelete = ConfirmDeleteDialogFragment.getConfirmation(data);

            if (confirmDelete){
                UUID segmentId = ConfirmDeleteDialogFragment.getSegmentId(data);
                showSnackbar(mFragmentView, getString(R.string.trip_deleted), Snackbar.LENGTH_SHORT);
                Segment segmentMatch = null;
                for (Segment segment : mSelectedSegments){
                    if (segment.getId().equals(segmentId)){
                        segmentMatch = segment;
                        break;
                    }
                }

                if (segmentMatch != null){
                    mSelectedSegments.remove(segmentMatch);
                    if (mSelectedSegments.size() < 1){
                        mDisplayButton.setEnabled(false);
                    }
                }
                DeleteTripTask task = new DeleteTripTask(getContext());

                task.execute(segmentId);

            }else{
                showSnackbar(mFragmentView, getString(R.string.trip_delete_cancel), Snackbar.LENGTH_SHORT);
            }

        } else if (requestCode == REQUEST_SORT_PREFERENCES){
            SortResult sortResult = SortActivity.getSortChangedResult(data);

            if (sortResult.isSortChanged()){

                Timber.d("Handling sort change, restarting segment loader");
                getLoaderManager().restartLoader(0, null, this);

                showSnackbar(mFragmentView,getSortMessage(sortResult.getSelectedSort()),
                        Snackbar.LENGTH_SHORT);

            }
        } else if (requestCode == REQUEST_FILTER_PREFERENCES){
            boolean filterChanged = FilterActivity.getFilterChangedResult(data);
            if (filterChanged){
                MenuItem filterItem = mMainMenu.findItem(R.id.filter);
                boolean filtersCleared = FilterActivity.getFiltersClearedResult(data);
                if (filtersCleared){
                    Timber.d("Filter result:  filters cleared");
                    mListFiltered = false;
                    filterItem.setIcon(R.drawable.filter_outline);

                }else{
                    Timber.d("Filter result:  filters set");
                    filterItem.setIcon(R.drawable.filter_remove_outline);
                    mListFiltered = true;
                }

               getLoaderManager().restartLoader(0, null, this);
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