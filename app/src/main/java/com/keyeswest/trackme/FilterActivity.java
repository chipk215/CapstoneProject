package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.keyeswest.trackme.utilities.FilterSharedPreferences;


import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;


import static com.keyeswest.trackme.utilities.FilterSharedPreferences.getEndDate;
import static com.keyeswest.trackme.utilities.FilterSharedPreferences.getFavoriteFilterSetting;
import static com.keyeswest.trackme.utilities.FilterSharedPreferences.getStartDate;
import static com.keyeswest.trackme.utilities.FilterSharedPreferences.isDateRangeSet;
import static com.keyeswest.trackme.utilities.FilterSharedPreferences.saveDateRangeFilter;
import static com.keyeswest.trackme.utilities.FilterSharedPreferences.saveFavoriteFilter;

public class FilterActivity extends AppCompatActivity {

    public static Intent newIntent(Context packageContext, boolean clearFilter){
        Intent intent = new Intent(packageContext, FilterActivity.class);
        intent.putExtra(EXTRA_CLEAR_FILTERS, clearFilter);
        return intent;
    }

    public static boolean getFilterChangedResult(Intent data){
        boolean filterChanged = data.getBooleanExtra(EXTRA_CHANGE_FILTER_RESULT,
                true);
        return filterChanged;
    }

    public static boolean getFiltersClearedResult(Intent data){
        boolean filtersCleared = data.getBooleanExtra(EXTRA_CLEAR_FILTERS,false);
        return filtersCleared;
    }

    private static final String EXTRA_CHANGE_FILTER_RESULT = "extraChangeFilterResult";
    private static final String EXTRA_CLEAR_FILTERS = "extraClearFilters";

    private Unbinder mUnbinder;

    @BindView(R.id.submit_btn)
    Button mSubmitButton;

    // This switch position is read when the submit buttton is pressed. There is no listener for
    //the toggle switch itself.
    @BindView(R.id.favorite_sw)
    Switch mFavoriteSwitch;

    @BindView(R.id.cancel_btn)
    Button mCancelButton;

    // used to set the date range
    @BindView(R.id.date_btn)
    Button mDateButton;

    private boolean mDateRangeUpdatedByUser;

    // filter date range
    private Long mStartDate= null;
    private long mEndDate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        mUnbinder = ButterKnife.bind( this);

        mDateRangeUpdatedByUser = false;

        setCurrentFavoriteFilterSelection();

        mSubmitButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                // save the favorite filter selection to shared preferences
                boolean isSelected = mFavoriteSwitch.isChecked();
                saveFavoriteFilter(FilterActivity.this, isSelected);

                // save the date range to shared preferences if the user set a date range
                if (mDateRangeUpdatedByUser){

                    saveDateRangeFilter(FilterActivity.this, mStartDate, mEndDate);
                }

                setFilterResult(true, false);
                finish();

            }
        });

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //read shared preferences to determine is user has an active date range set from
                // previous invocation
                Calendar calendar = GregorianCalendar.getInstance();

                if (isDateRangeSet(FilterActivity.this)){

                    //read the date range from shared preferences
                    mStartDate = getStartDate(FilterActivity.this);
                    //TODO figure out the
                    long endDate = getEndDate(FilterActivity.this);

                }else if (mStartDate == null){
                    // use today's date to initialize picker
                    mStartDate = Calendar.getInstance().getTime().getTime();

                }

                // note if mStartDate not null and not read from Shared preferences we are using the
                // value set by the user in this session using the date picker
                calendar.setTimeInMillis(mStartDate);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);


                // https://github.com/borax12/MaterialDateRangePicker
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view,
                                                  int year, int monthOfYear, int dayOfMonth,
                                                  int yearEnd, int monthOfYearEnd,
                                                  int dayOfMonthEnd) {


                                mStartDate = new GregorianCalendar(year, monthOfYear,
                                        dayOfMonth).getTime().getTime() / 1000;

                                mEndDate =  new GregorianCalendar(yearEnd, monthOfYearEnd,
                                        dayOfMonthEnd).getTime().getTime() / 1000;

                                mDateRangeUpdatedByUser = true;
                            }
                        },
                        year,month, day);

                dpd.setAutoHighlight(true);
                dpd.show(getFragmentManager(), "Datepickerdialog");


            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilterResult(false, false);
                finish();
            }
        });



        Intent intent = getIntent();
        boolean clearFilters = intent.getBooleanExtra(EXTRA_CLEAR_FILTERS, false);
        if (clearFilters){
            // clear all filters
            FilterSharedPreferences.clearFilters(this, true);
            // set return result to include cleared result
            setFilterResult(true, true);
            finish();
        }
    }



    // set the filter results in the return intent
    private void setFilterResult(boolean filtersChanged, boolean cleared){
        Intent data = new Intent();
        data.putExtra(EXTRA_CHANGE_FILTER_RESULT, filtersChanged);
        data.putExtra(EXTRA_CLEAR_FILTERS, cleared);
        setResult(RESULT_OK, data);
    }

    @Override
    public void onDestroy(){
        mUnbinder.unbind();
        super.onDestroy();

    }


    private void setCurrentFavoriteFilterSelection(){

        mFavoriteSwitch.setChecked(getFavoriteFilterSetting(FilterActivity.this));

    }
}
