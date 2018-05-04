package com.keyeswest.trackme;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keyeswest.trackme.adapters.TrackLogAdapter;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public class TripListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Unbinder mUnbinder;

    @BindView(R.id.track_log_recycler_view)
    RecyclerView mTrackLogListView;

    public TripListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragmemt_trip_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mTrackLogListView.setLayoutManager(new LinearLayoutManager(getContext()));


        getActivity().getSupportLoaderManager().initLoader(0, null, this);

        return view;
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
        return SegmentLoader.newAllSegmentsInstance(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Timber.d("onCreateLoader invoked");
        if (cursor != null) {
            Timber.d("NUmber of records = " + cursor.getCount());
            TrackLogAdapter adapter = new TrackLogAdapter(new SegmentCursor(cursor), new TrackLogAdapter.SegmentClickListener() {
                @Override
                public void onSegmentClick(Uri segmentUri) {
                    Timber.d("Segment Selected");
                    Timber.d("Segment Uri: " + segmentUri);

                    Intent intent = MapsActivity.newIntent(getContext(), segmentUri);
                    startActivity(intent);
                }
            });
            adapter.setHasStableIds(true);
            mTrackLogListView.setAdapter(adapter);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mTrackLogListView.setAdapter(null);
    }
}