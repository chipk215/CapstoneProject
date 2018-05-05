package com.keyeswest.trackme;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.keyeswest.trackme.adapters.TrackLogAdapter;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public class TripListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, TrackLogAdapter.SegmentClickListener{

    private Unbinder mUnbinder;

    @BindView(R.id.track_log_recycler_view)
    RecyclerView mTrackLogListView;

    public TripListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Stetho.initializeWithDefaults(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragmemt_trip_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mTrackLogListView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.custom_list_divider));
        mTrackLogListView.addItemDecoration(itemDecorator);


        getActivity().getSupportLoaderManager().initLoader(0, null, this);

        return view;
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
        Timber.d("onCreateLoader invoked");
        if (cursor != null) {
            Timber.d("Number of records = %s", cursor.getCount());
            TrackLogAdapter adapter = new TrackLogAdapter(new SegmentCursor(cursor), this);

            mTrackLogListView.setAdapter(adapter);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mTrackLogListView.setAdapter(null);
    }

    @Override
    public void onSegmentClick(Uri segmentUri) {
        Timber.d("Segment Selected");
        Timber.d("Segment Uri: " + segmentUri);

        Intent intent = MapsActivity.newIntent(getContext(), segmentUri);
        startActivity(intent);

    }

    @Override
    public void onDeleteClick(Uri segmentUri) {
        Toast.makeText(getContext(), "URI Trash Clicked: " + segmentUri.toString(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(Uri segmentUri, boolean makeFavorite) {
        Toast.makeText(getContext(), "Favorite Clicked: " + segmentUri.toString(),
                Toast.LENGTH_SHORT).show();

    }
}