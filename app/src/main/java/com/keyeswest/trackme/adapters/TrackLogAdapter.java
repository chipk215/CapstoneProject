package com.keyeswest.trackme.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keyeswest.trackme.R;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentSchema;


public class TrackLogAdapter extends RecyclerView.Adapter<TrackLogAdapter.LogHolder> {

    private SegmentCursor mCursor;
    private SegmentClickListener mSegmentClickListener;

    public interface SegmentClickListener{
        void onSegmentClick(Uri segmentUri);
    }

    public TrackLogAdapter(SegmentCursor cursor, SegmentClickListener listener){
        mCursor = cursor;
        mSegmentClickListener = listener;
    }



    @NonNull
    @Override
    public LogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_log, parent, false);
        final TrackLogAdapter.LogHolder vh = new TrackLogAdapter.LogHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCursor.moveToPosition(vh.getAdapterPosition());
                Uri itemUri = SegmentSchema.SegmentTable.buildItemUri(mCursor.getSegment().getRowId());
                mSegmentClickListener.onSegmentClick(itemUri);

            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull LogHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mDateView.setText(mCursor.getSegment().getDateTime());
        holder.mDistanceView.setText(Double.toString(mCursor.getSegment().getDistance()));

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    static class LogHolder extends RecyclerView.ViewHolder{

        private TextView mDateView;
        private TextView mDistanceView;

        public LogHolder(View view){
            super(view);
            mDateView = view.findViewById(R.id.date_tv);
            mDistanceView = view.findViewById(R.id.distance_tv);
        }



    }
}
