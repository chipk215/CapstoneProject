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

    private static final int MAX_CHECKED_ITEMS = 4;


    private SegmentCursor mCursor;
    private SegmentClickListener mSegmentClickListener;

    private int mCheckedItems;

    public interface SegmentClickListener{
        void onItemChecked(SegmentCursor segmentCursor );
        void onItemUnchecked(SegmentCursor segmentCursor);
        void onDeleteClick(Uri segmentUri);
        void onFavoriteClick(Uri segmentUri, boolean makeFavorite);

    }



    public TrackLogAdapter(SegmentCursor cursor, SegmentClickListener listener){
        mCursor = cursor;
        mSegmentClickListener = listener;
        mCheckedItems = 0;
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

                holder.mCheckBox.setChecked( ! holder.mCheckBox.isChecked());
                if (holder.mCheckBox.isChecked()){
                    mSegmentClickListener.onItemChecked(holder.mSegmentCursor);
                    mCheckedItems+=1;
                }else{
                    mSegmentClickListener.onItemUnchecked(holder.mSegmentCursor);
                    mCheckedItems-=1;
                }



                if (mCheckedItems == MAX_CHECKED_ITEMS){
                    // disable all unchecked checkboxes
                    //TODO
                }


            }
        });


    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
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
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        mCursor.moveToPosition(position);
                        Uri itemUri = SegmentSchema.SegmentTable.buildItemUri(mCursor.getSegment().getRowId());
                        mSegmentClickListener.onDeleteClick(itemUri);

                    }

                }
            });

            mFavoriteButton = view.findViewById(R.id.fav_btn);

            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mCursor.moveToPosition(position);
                        Uri itemUri = SegmentSchema.SegmentTable.buildItemUri(mCursor.getSegment().getRowId());
                        mSegmentClickListener.onFavoriteClick(itemUri, true);
                    }
                }
            });

        }

        public void setOnClickListener(View.OnClickListener onClickListener){
            mItemView.setOnClickListener(onClickListener);
        }





    }
}
