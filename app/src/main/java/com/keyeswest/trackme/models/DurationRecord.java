package com.keyeswest.trackme.models;

import android.content.Context;

import com.keyeswest.trackme.R;
import com.keyeswest.trackme.utilities.PluralHelpers;

import java.text.DecimalFormat;

public class DurationRecord {

    private final static int SEC_PER_MINUTE = 60;
    private final static int SEC_PER_HOUR = 3600;
    private final static int MILLISECONDS_PER_SECOND = 1000;

    private double mValue;
    private String mDimension;



    DurationRecord(Context context, long duration) {
        mValue = duration/ MILLISECONDS_PER_SECOND;

        if (mValue < SEC_PER_MINUTE){
            mDimension = context.getResources().getQuantityString(R.plurals.seconds_plural,
                    PluralHelpers.getPluralQuantity(mValue));
        }else if (mValue < SEC_PER_HOUR){
            mValue = mValue / SEC_PER_MINUTE;
            mDimension = context.getResources().getQuantityString(R.plurals.minutes_plural,
                    PluralHelpers.getPluralQuantity(mValue));
        }else{
            mValue = mValue/SEC_PER_HOUR;
            mDimension = context.getResources().getQuantityString(R.plurals.hours_plural,
                    PluralHelpers.getPluralQuantity(mValue));
        }
    }

    public String getDimension() {

        return mDimension;
    }


    public String getValue(){
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(mValue);
    }



}
