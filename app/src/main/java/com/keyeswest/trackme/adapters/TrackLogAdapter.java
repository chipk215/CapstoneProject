package com.keyeswest.trackme.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.keyeswest.trackme.R;
import com.keyeswest.trackme.data.SegmentCursor;
import com.keyeswest.trackme.models.Segment;
import com.keyeswest.trackme.utilities.PluralHelpers;

import java.util.List;

import timber.log.Timber;


public class TrackLogAdapter extends RecyclerView.Adapter<TrackLogAdapter.LogHolder>   {

    private boolean mSelectionsFrozen;
    private SegmentCursor mCursor;
    private SegmentClickListener mSegmentClickListener;
    private List<Segment> mInitialSelectedSegments;
    private Context mContext;

    public interface SegmentClickListener{
        void onItemChecked(Segment segment );
        void onItemUnchecked(Segment segment);
        void onDeleteClick(Segment segment);
        void onFavoriteClick(Segment segment, boolean selected);
    }


    public TrackLogAdapter(SegmentCursor cursor, Context context, List<Segment> selectedSegments, SegmentClickListener listener){
        mCursor = cursor;
        mSegmentClickListener = listener;
        mInitialSelectedSegments = selectedSegments;
        mSelectionsFrozen = false;
        mContext = context;
    }



    @NonNull
    @Override
    public LogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_log, parent, false);

        return new LogHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull LogHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mDateView.setText(mCursor.getSegment().getDate());
        holder.mTimeView.setText(mCursor.getSegment().getTime());
        holder.mDistanceView.setText(mCursor.getSegment().getDistanceMiles());

        String mileOrMiles = mContext.getResources().getQuantityString(R.plurals.miles_plural,
                        PluralHelpers.getPluralQuantity(mCursor.getSegment().getDistance()));
        holder.mMilesLabel.setText(mileOrMiles);
        holder.mSegment = mCursor.getSegment();

        boolean favoriteState = holder.mSegment.isFavorite();
        if (favoriteState){
            holder.mFavoriteButton.setImageResource(R.drawable.fav_star_filled);
            holder.mFavoriteButton.setTag(true);
        }else{
            holder.mFavoriteButton.setImageResource(R.drawable.fav_star_border);
            holder.mFavoriteButton.setTag(false);
        }

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
        private TextView mMilesLabel;

        public Segment getSegment(){
            return mSegment;
        }

        void checkSelection(){
            mCheckBox.setChecked(true);
        }


        LogHolder(View view){
            super(view);
            mItemView = view;
            mDateView = view.findViewById(R.id.date_lbl);
            mDistanceView = view.findViewById(R.id.distance_lb);
            mTimeView = view.findViewById(R.id.time_tv);
            mCheckBox = view.findViewById(R.id.checkBox);
            mCheckBox.setClickable(false);
            mMilesLabel = view.findViewById(R.id.miles_label_tv);


            mTrashButton = view.findViewById(R.id.delete_btn);
            mTrashButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSegmentClickListener.onDeleteClick(mSegment);
                }
            });

            mFavoriteButton = view.findViewById(R.id.fav_btn);

            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isFavorite = (Boolean)mFavoriteButton.getTag();
                    if (isFavorite){
                        // change to not favorite
                        mFavoriteButton.setTag(false);
                        mFavoriteButton.setImageResource(R.drawable.fav_star_border);
                        mSegmentClickListener.onFavoriteClick(mSegment, false);

                    }else{
                        mFavoriteButton.setTag(true);
                        mFavoriteButton.setImageResource(R.drawable.fav_star_filled);
                        mSegmentClickListener.onFavoriteClick(mSegment, true);
                    }

                }
            });

        }

        void setOnClickListener(View.OnClickListener onClickListener){
            mItemView.setOnClickListener(onClickListener);
        }

    }

}
