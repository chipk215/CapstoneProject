package com.keyeswest.trackme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public class TripListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, TrackLogAdapter.SegmentClickListener{

    private static final int MAX_TRIP_SELECTIONS = 4;
    private static final String DIALOG_DELETE_CONFIRM = "dialogDeleteConfirm";
    private static final int REQUEST_TRIP_DELETE_CONFIRM = 0;
    public static final String ARG_SELECTED_SEGMENTS = "argSelectedSegments";

    public static final String FILTER_PREFERENCES = "filterPreferences";
    public static final String SORT_PREFERENCES_KEY = "sortPreferencesKey";
    public static final String FAVORITE_PREFERENCES_KEY = "favoritePreferencesKey";
    public static final SortPreference DEFAULT_FILTER = SortPreference.NEWEST;
    private static final boolean DEFAULT_FAVORITES_ONLY_FILTER = false;

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

        saveDefaultPreferences(getContext(),false);


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
            case R.id.filter:

                intent = FilterActivity.newIntent(getContext());
                startActivity(intent);

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
        return SegmentLoader.newAllSegmentsSortedFilteredByPreferences(getContext());
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
       // Toast.makeText(getContext(), segment.getId().toString(), Toast.LENGTH_SHORT).show();
        //DeleteTripTask task = new DeleteTripTask(getContext());
       // task.execute(segment.getId());
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

        }
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


    /**
     * Save default sorting and filtering preferences.
     * @param force - if true overwrite existing preferences, otherwise only save preferences if
     *              they have not previously been saved.
     */
    public static void saveDefaultPreferences(Context context, boolean force){

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (force) {
            // save the default values regardless of what has previously been saved

            editor.putString(SORT_PREFERENCES_KEY, DEFAULT_FILTER.getCode());
            editor.putBoolean(FAVORITE_PREFERENCES_KEY, DEFAULT_FAVORITES_ONLY_FILTER);
            editor.commit();
        }else{

            boolean updated = false;
            // don't save defaults if preferences have previously been saved
            if (! sharedPreferences.contains(SORT_PREFERENCES_KEY)){
                editor.putString(SORT_PREFERENCES_KEY, DEFAULT_FILTER.getCode());
                updated = true;
            }

            if (! sharedPreferences.contains(FAVORITE_PREFERENCES_KEY)){
                editor.putBoolean(FAVORITE_PREFERENCES_KEY, DEFAULT_FAVORITES_ONLY_FILTER);
                updated = true;
            }

            if (updated){
                editor.commit();
            }
        }

    }
}