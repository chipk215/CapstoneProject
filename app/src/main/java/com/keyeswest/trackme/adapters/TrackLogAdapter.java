package com.keyeswest.trackme.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.keyeswest.trackme.R;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.data.SegmentSchema;
import com.keyeswest.trackme.models.Segment;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class TrackLogAdapter extends RecyclerView.Adapter<TrackLogAdapter.LogHolder>   {

    private boolean mSelectionsFrozen;
    private SegmentCursor mCursor;
    private SegmentClickListener mSegmentClickListener;
    private List<Segment> mInitialSelectedSegments;



    public interface SegmentClickListener{
        void onItemChecked(Segment segment );
        void onItemUnchecked(Segment segment);
        void onDeleteClick(Segment segment);
     //   void onFavoriteClick(Segment segment);
     //   void onUnFavoriteClicked(Segment segment);

    }



    public TrackLogAdapter(SegmentCursor cursor, List<Segment> selectedSegments,SegmentClickListener listener){
        mCursor = cursor;
        mSegmentClickListener = listener;
        mInitialSelectedSegments = selectedSegments;
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
        holder.mSegment = mCursor.getSegment();

        // check the box if we are reloading the cursor data
        if (mInitialSelectedSegments.contains(holder.mSegment)){
            Timber.d("Setting checkbox for Segment ID: %s",
                    holder.mSegment.getId().toString());
            holder.checkSelection();
        }


        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectionsFrozen &&  (! holder.mCheckBox.isChecked()) ){

                    Toast.makeText(v.getContext(), R.string.max_trips_selected, Toast.LENGTH_SHORT).show();

                }else{
                    holder.mCheckBox.setChecked( ! holder.mCheckBox.isChecked());
                    if (holder.mCheckBox.isChecked()){
                        mSegmentClickListener.onItemChecked(holder.mSegment);

                    }else{
                        mSegmentClickListener.onItemUnchecked(holder.mSegment);

                        // once the segment is unselected we don't have to bind against it
                        if (mInitialSelectedSegments.contains(holder.mSegment)){
                            mInitialSelectedSegments.remove(holder.mSegment);
                        }
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


     public class LogHolder extends RecyclerView.ViewHolder {

        private TextView mDateView;
        private TextView mTimeView;

        private TextView mDistanceView;
        private ImageButton mTrashButton;
        private ImageButton mFavoriteButton;
        private CheckBox mCheckBox;
        private View mItemView;
        private Segment mSegment;

        public Segment getSegment(){
            return mSegment;
        }

        public void checkSelection(){
            mCheckBox.setChecked(true);
        }


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
                    mSegmentClickListener.onDeleteClick(mSegment);
                }
            });

            mFavoriteButton = view.findViewById(R.id.fav_btn);

            // need to handle unfavorite as well
            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  //  mSegmentClickListener.onFavoriteClick(mCursor);
                }
            });

        }

        public void setOnClickListener(View.OnClickListener onClickListener){
            mItemView.setOnClickListener(onClickListener);
        }


    }

}
