package com.keyeswest.trackme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
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
import android.widget.TextView;

import com.keyeswest.trackme.adapters.TrackLogAdapter;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;
import com.keyeswest.trackme.models.DurationRecord;
import com.keyeswest.trackme.models.Segment;

import com.keyeswest.trackme.tasks.DeleteTripTask;
import com.keyeswest.trackme.tasks.UpdateFavoriteStatusTask;
import com.keyeswest.trackme.utilities.BatteryStatePreferences;
import com.keyeswest.trackme.utilities.FilterSharedPreferences;
import com.keyeswest.trackme.utilities.PluralHelpers;
import com.keyeswest.trackme.utilities.SortSharedPreferences;
import com.keyeswest.trackme.utilities.SortResult;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.utilities.BatteryStatePreferences.BATTERY_PREFERENCES;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.BATTERY_STATE_EXTRA;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.LOW_BATTERY_THRESHOLD;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.getLowBatteryState;



public class TripListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        TrackLogAdapter.SegmentClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {


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

    @BindView(R.id.low_battery_tv)
    TextView mLowBatteryMessage;

    // List of currently selected/checked trips
    private List<Segment> mSelectedSegments;

    private TrackLogAdapter mTrackLogAdapter;

    private boolean mListFiltered= false;
    private Menu mMainMenu;

    private SegmentCursor mSegmentCursor;

    private SharedPreferences mBatteryPreferences;

    private boolean mNewTripMenuItemEnabled;



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

        Bundle arguments = getArguments();
        if (arguments != null) {
            mHideDisplayButton = arguments.getBoolean(TWO_PANE_EXTRA, false);
        }

        setHasOptionsMenu(true);

        SortSharedPreferences.saveDefaultSortPreferences(Objects.requireNonNull(getContext()),false);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.d("onCreateView invoked");
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_trip_list, container, false);
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
        DividerItemDecoration itemDecorator = new DividerItemDecoration(Objects
                .requireNonNull(getActivity()), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getActivity(),
                R.drawable.custom_list_divider)));
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

        //Determine if battery is in a low battery state and set menus accordingly
        configureMenusWithBatteryStateInformation();

        // register for changes in low battery state messages
        mBatteryPreferences = Objects.requireNonNull(getContext())
                .getSharedPreferences(BATTERY_PREFERENCES, Context.MODE_PRIVATE);

        if (mBatteryPreferences != null) {
            Timber.d("registering for shared prefs notification");
            mBatteryPreferences.registerOnSharedPreferenceChangeListener(this);
        }


    }




    @Override
    public void onPause(){
        super.onPause();
        Timber.d("onPause invoked");
        mBatteryPreferences = Objects.requireNonNull(getContext()).
                getSharedPreferences(BatteryStatePreferences.BATTERY_PREFERENCES, Context.MODE_PRIVATE);

        if (mBatteryPreferences != null) {
            mBatteryPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.d("onSharedPreferenceChanged");
        if (key.equals(BATTERY_STATE_EXTRA)){
            // read the battery state
            boolean isLow = getLowBatteryState(Objects.requireNonNull(getContext()));
            if (isLow){
                Timber.d("Low Battery State Occurred");
                Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
                mNewTripMenuItemEnabled = false;
                mLowBatteryMessage.setVisibility(View.VISIBLE);

            }else{
                Timber.d("Low Battery State Ended");
                Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
                mNewTripMenuItemEnabled = true;
                mLowBatteryMessage.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void onPrepareOptionsMenu (Menu menu) {
        Timber.d("onPrepareOptionsMenu");
        MenuItem newTripItem = menu.findItem(R.id.new_trip);
        newTripItem.setEnabled(mNewTripMenuItemEnabled);
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
                intent = FilterActivity.newIntent(getContext(), mListFiltered);
                startActivityForResult(intent, REQUEST_FILTER_PREFERENCES);
                return true;

            case R.id.share:
                shareTripList();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.fragment_trip_list, menu);
        mMainMenu = menu;
        MenuItem newTripItem = menu.findItem(R.id.new_trip);
        newTripItem.setEnabled(mNewTripMenuItemEnabled);

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
            mSegmentCursor = new SegmentCursor(cursor);
            Timber.d("Number of records = %s", mSegmentCursor.getCount());
            mTrackLogAdapter = new TrackLogAdapter(mSegmentCursor, getContext(),
                    mSelectedSegments, this);

            mTrackLogAdapter.setHasStableIds(true);
            mTrackLogListView.setAdapter(mTrackLogAdapter);

            // Determine whether Display button should be disabled
            updateDisplayButtonState(mSegmentCursor);

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
        Timber.d("Adding segment to selected trips: %s", segment.getId().toString());

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
        Timber.d("Removing segment to selected trips: %s", segment.getId().toString());
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
        if (manager != null) {
            dialog.show(manager, DIALOG_DELETE_CONFIRM);
        }


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
                    showFilterStatus(data);
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


    /**
     * Invoked by parent activity on a configuration change going from two pane to one pane, as
     * when on a tablet and going from landscape to portrait.
     *
     */
    public void hideDisplayButton(Boolean hide){
        mHideDisplayButton = hide;
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
        String result = getString(R.string.most_recent_sort);
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

    private void updateDisplayButtonState(SegmentCursor segmentCursor){
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
    }

    private void showFilterStatus(Intent data) {
        boolean dateFilter = FilterActivity.isDateFilterSet(data);
        boolean favoriteFilter = FilterActivity.isFavoriteFilterSet(data);
        String message = null;
        if (dateFilter && favoriteFilter){
            message = getString(R.string.favorite_date_filter);
        }else if (dateFilter){
            message = getString(R.string.date_filter);
        }else if(favoriteFilter){
            message = getString(R.string.favorite_filter);
        }
        if (message != null){
            showSnackbar(mFragmentView,message,
                    Snackbar.LENGTH_LONG);
        }
    }


    private void shareTripList(){
        if (mSegmentCursor != null) {
            mSegmentCursor.moveToPosition(-1);
            String message="";
            int count = 1;
            String newLine = System.getProperty("line.separator");
            while(mSegmentCursor.moveToNext()){
                Segment segment = mSegmentCursor.getSegment();
                message += getString(R.string.trip) + " " + Integer.toString(count) +  newLine;
                message += getString(R.string.date) + "  " + segment.getDate() +  newLine;
                message += getString(R.string.start_time) + "  " + segment.getTime() +  newLine;
                DurationRecord durationRecord = segment.getSegmentDuration(getContext());

                message+= getString(R.string.trip_elapsed) + " " +
                        durationRecord.getValue()+ " " +
                        durationRecord.getDimension() + newLine;

                message += getString(R.string.distance) + "  " +
                        segment.getDistanceMiles() + " " +
                        Objects.requireNonNull(getContext()).getResources()
                                .getQuantityString(R.plurals.miles_plural,
                                        PluralHelpers.getPluralQuantity(segment.getDistance()));
                message += newLine + newLine;
                count++;

            }

            startActivity(Intent.createChooser(ShareCompat.IntentBuilder
                    .from(Objects.requireNonNull(getActivity()))
                    .setType("text/plain")
                    .setSubject(Objects.requireNonNull(getContext())
                    .getString(R.string.trip_list_subject))
                    .setText(message)
                    .getIntent(), getString(R.string.action_share)));
        }
    }


    /**
     * If the battery is in a low battery state, disable teh new trip menu item and post a low
     * battery state message. Otherwise, enable tracking functionality.
     */
    private void configureMenusWithBatteryStateInformation(){
        float batteryPercentage = BatteryStatePreferences.getCurrentBatteryPercentLevel(Objects
                .requireNonNull(getActivity()));

        if (batteryPercentage <= LOW_BATTERY_THRESHOLD ){
            mNewTripMenuItemEnabled = false;
            mLowBatteryMessage.setVisibility(View.VISIBLE);
        }else{
            mNewTripMenuItemEnabled = true;
            mLowBatteryMessage.setVisibility(View.GONE);
        }
        getActivity().invalidateOptionsMenu();
    }

}