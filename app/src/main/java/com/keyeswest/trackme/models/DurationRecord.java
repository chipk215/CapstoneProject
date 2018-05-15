package com.keyeswest.trackme.models;

import android.content.Context;

import com.keyeswest.trackme.R;

import java.text.DecimalFormat;

import static java.lang.Math.abs;

public class DurationRecord {

    private final static int SEC_PER_MINUTE = 60;
    private final static int SEC_PER_HOUR = 3600;
    private final static int MILLISECONDS_PER_SECOND = 1000;

    private final static double EPSILON = 1E-6;

    private Context mContext;
    private double mValue;
    private String mDimension;



    public  DurationRecord(Context context, long duration) {
        mValue = duration/ MILLISECONDS_PER_SECOND;
        mContext = context;

        if (mValue < SEC_PER_MINUTE){
            mDimension = mContext.getResources().getQuantityString(R.plurals.seconds_plural,
                    getPluralQuantity(mValue));
        }else if (mValue < SEC_PER_HOUR){
            mValue = mValue / SEC_PER_MINUTE;
            mDimension = mContext.getResources().getQuantityString(R.plurals.minutes_plural,
                    getPluralQuantity(mValue));
        }else{
            mValue = mValue/SEC_PER_HOUR;
            mDimension = mContext.getResources().getQuantityString(R.plurals.hours_plural,
                    getPluralQuantity(mValue));
        }
    }

    public String getDimension() {

        return mDimension;
    }


    public String getValue(){
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(mValue);
    }

    private boolean isSingular(double value){
        return (abs(value - 1.0d) < EPSILON) ? true : false;
    }

    private int getPluralQuantity(double value){
        return isSingular(value) ? 1 : 2;
    }

}
