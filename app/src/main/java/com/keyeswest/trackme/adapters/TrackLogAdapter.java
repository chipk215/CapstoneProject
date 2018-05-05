package com.keyeswest.trackme.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
        holder.mDateView.setText(mCursor.getSegment().getDate());
        holder.mTimeView.setText(mCursor.getSegment().getTime());
        holder.mDistanceView.setText(Double.toString(mCursor.getSegment().getDistanceMiles()));

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    static class LogHolder extends RecyclerView.ViewHolder{

        private TextView mDateView;
        private TextView mTimeView;

        private TextView mDistanceView;
        private ImageButton mTrashButton;
        private ImageButton mFavoriteButton;

        public LogHolder(View view){
            super(view);
            mDateView = view.findViewById(R.id.date_tv);
            mDistanceView = view.findViewById(R.id.distance_tv);
            mTimeView = view.findViewById(R.id.time_tv);

            mTrashButton = view.findViewById(R.id.delete_btn);
            mTrashButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Trash Clicked", Toast.LENGTH_SHORT).show();
                }
            });

            mFavoriteButton = view.findViewById(R.id.fav_btn);

            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Favorite clicked", Toast.LENGTH_SHORT).show();
                }
            });


        }



    }
}
