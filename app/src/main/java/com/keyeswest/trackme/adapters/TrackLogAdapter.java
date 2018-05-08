package com.keyeswest.trackme.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.keyeswest.trackme.R;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentSchema;

import java.util.ArrayList;
import java.util.List;


public class TrackLogAdapter extends RecyclerView.Adapter<TrackLogAdapter.LogHolder>   {

    private boolean mSelectionsFrozen;
    private SegmentCursor mCursor;
    private SegmentClickListener mSegmentClickListener;

    private int mCheckedItems;

    public interface SegmentClickListener{
        void onItemChecked(SegmentCursor segmentCursor );
        void onItemUnchecked(SegmentCursor segmentCursor);
        void onDeleteClick(SegmentCursor segmentCursor);
        void onFavoriteClick(SegmentCursor segmentCursor);
        void onUnFavoriteClicked(SegmentCursor segmentCursor);

    }



    public TrackLogAdapter(SegmentCursor cursor, SegmentClickListener listener){
        mCursor = cursor;
        mSegmentClickListener = listener;
        mCheckedItems = 0;
        mSelectionsFrozen = false;
    }



    @NonNull
    @Override
    public LogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_log, parent, false);
        final TrackLogAdapter.LogHolder holder = new TrackLogAdapter.LogHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final @NonNull LogHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mDateView.setText(mCursor.getSegment().getDate());
        holder.mTimeView.setText(mCursor.getSegment().getTime());
        holder.mDistanceView.setText(mCursor.getSegment().getDistanceMiles());
        holder.mSegmentCursor = mCursor;
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectionsFrozen &&  (! holder.mCheckBox.isChecked()) ){

                    Toast.makeText(v.getContext(), R.string.max_trips_selected, Toast.LENGTH_SHORT).show();

                }else{
                    holder.mCheckBox.setChecked( ! holder.mCheckBox.isChecked());
                    if (holder.mCheckBox.isChecked()){
                        mSegmentClickListener.onItemChecked(holder.mSegmentCursor);
                        mCheckedItems+=1;
                    }else{
                        mSegmentClickListener.onItemUnchecked(holder.mSegmentCursor);
                        mCheckedItems-=1;
                    }
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }



    //TODO Figure out why the next two methods are needed so that the trip list items are stable
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public void setSelectionsFrozen(boolean freeze){
        mSelectionsFrozen = freeze;
    }

     class LogHolder extends RecyclerView.ViewHolder {

        private TextView mDateView;
        private TextView mTimeView;

        private TextView mDistanceView;
        private ImageButton mTrashButton;
        private ImageButton mFavoriteButton;
        private CheckBox mCheckBox;
        private View mItemView;
        private SegmentCursor mSegmentCursor;

        public LogHolder(View view){
            super(view);
            mItemView = view;
            mDateView = view.findViewById(R.id.date_tv);
            mDistanceView = view.findViewById(R.id.distance_tv);
            mTimeView = view.findViewById(R.id.time_tv);
            mCheckBox = view.findViewById(R.id.checkBox);
            mCheckBox.setClickable(false);


            mTrashButton = view.findViewById(R.id.delete_btn);
            mTrashButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSegmentClickListener.onDeleteClick(mCursor);
                }
            });

            mFavoriteButton = view.findViewById(R.id.fav_btn);

            // need to handle unfavorite as well
            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSegmentClickListener.onFavoriteClick(mCursor);
                }
            });

        }

        public void setOnClickListener(View.OnClickListener onClickListener){
            mItemView.setOnClickListener(onClickListener);
        }


    }

}
