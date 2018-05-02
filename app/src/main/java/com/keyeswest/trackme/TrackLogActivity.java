package com.keyeswest.trackme;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.keyeswest.trackme.adapters.TrackLogAdapter;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentLoader;

import timber.log.Timber;

public class TrackLogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private RecyclerView mTrackLogListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track_log);

        mTrackLogListView = findViewById(R.id.track_log_recycler_view);
        mTrackLogListView.setLayoutManager(new LinearLayoutManager(this));

        getSupportLoaderManager().initLoader(0, null, this);


    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Timber.d("onCreateLoader invoked");
        return SegmentLoader.newAllSegmentsInstance(this);
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

                    Intent intent = MapsActivity.newIntent(TrackLogActivity.this, segmentUri);
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