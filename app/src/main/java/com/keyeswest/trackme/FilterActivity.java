package com.keyeswest.trackme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.keyeswest.trackme.utilities.FilterSharedPreferences;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
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

/**
 * Handles filter selection for the trip list.
 */
public class FilterActivity extends AppCompatActivity {

    // The user clears all filters by selecting the clear filter icon. Otherwise
    // the activity is started in order to set a filter.
    public static Intent newIntent(Context packageContext, boolean clearFilter){
        Intent intent = new Intent(packageContext, FilterActivity.class);
        intent.putExtra(EXTRA_CLEAR_FILTERS, clearFilter);
        return intent;
    }

    // Helper for invoking client to determine if result of filter request changed the filter
    //settings.
    public static boolean getFilterChangedResult(Intent data){
        boolean filterChanged = data.getBooleanExtra(EXTRA_CHANGE_FILTER_RESULT,
                true);
        return filterChanged;
    }

    //Helper for invoking client to determine if user cleared filters during filter request.
    public static boolean getFiltersClearedResult(Intent data){
        boolean filtersCleared = data.getBooleanExtra(EXTRA_CLEAR_FILTERS,false);
        return filtersCleared;
    }


    //Helper for invoking client to determine if user set a date range.
    public static boolean isDateFilterSet(Intent data){
        boolean isSet = data.getBooleanExtra(EXTRA_DATE_FILTER, false);
        return isSet;
    }

    //Helper for invoking client to determine if user set the favorites only filter..
    public static boolean isFavoriteFilterSet(Intent data){
        boolean isSet = data.getBooleanExtra(EXTRA_FAVORITE_FILTER, false);
        return isSet;
    }

    private static final String EXTRA_CHANGE_FILTER_RESULT = "extraChangeFilterResult";
    private static final String EXTRA_CLEAR_FILTERS = "extraClearFilters";
    private static final String EXTRA_SHOW_DATE_RANGE =  "extraShowDateRange";
    private static final String EXTRA_START_DATE = "extraStartDate";
    private static final String EXTRA_END_DATE = "extraEndDate";
    private static final String EXTRA_DATE_FILTER = "extraDateFilter";
    private static final String EXTRA_FAVORITE_FILTER = "extraFavoriteFilter";

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

    @BindView(R.id.start_lbl_tv)
    TextView mStartDateLabelTextView;

    @BindView(R.id.start_date_tv)
    TextView mStartDateTextView;

    @BindView(R.id.end_lbl_tv)
    TextView mEndDateLabelTextView;

    @BindView(R.id.end_date_tv)
    TextView mEndDateTextView;

    // determines whether dates should be saved to shared preferences
    private boolean mDateRangeUpdatedByUser;

    //specifies  whether date filter is set when user exits filter activity
    private boolean mIsDateFilterOn;

    // filter date range
    private Long mStartDate= null;
    private long mEndDate;

    private boolean mShowingDateRange = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        mUnbinder = ButterKnife.bind( this);

        mDateRangeUpdatedByUser = false;

        if (savedInstanceState != null){
            mShowingDateRange = savedInstanceState.getBoolean(EXTRA_SHOW_DATE_RANGE);
            if (mShowingDateRange){
                mStartDate = savedInstanceState.getLong(EXTRA_START_DATE);
                mEndDate = savedInstanceState.getLong(EXTRA_END_DATE);
                mStartDateTextView.setText(getDateString(mStartDate));
                mEndDateTextView.setText(getDateString(mEndDate));

                showDateRangeDates(true);
            }else{
                showDateRangeDates(false);
            }
        }else{
            showDateRangeDates(false);
        }

        // configure the filter favorite switch to either on or off
        setCurrentFavoriteFilterSelection();

        mIsDateFilterOn = isDateRangeSet(FilterActivity.this);

        mSubmitButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                // save the favorite filter selection to shared preferences
                boolean isFavoriteFilter = mFavoriteSwitch.isChecked();
                saveFavoriteFilter(FilterActivity.this, isFavoriteFilter);

                // save the date range to shared preferences if the user set a date range
                if (mDateRangeUpdatedByUser){

                    saveDateRangeFilter(FilterActivity.this, mStartDate, mEndDate);
                }

                setFilterResult(true, false, mIsDateFilterOn,
                        isFavoriteFilter);


                finish();

            }
        });

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //read shared preferences to determine is user has an active date range set from
                // previous invocation
                Calendar calendar = GregorianCalendar.getInstance();

                if (mIsDateFilterOn){

                    //read the date range from shared preferences
                    mStartDate = getStartDate(FilterActivity.this);

                    long endDate = getEndDate(FilterActivity.this);

                }else if (mStartDate == null){
                    // use today's date to initialize picker
                    mStartDate = Calendar.getInstance().getTime().getTime();

                }else{
                    mStartDate*=1000;  // convert back to milliseconds
                }

                // note if mStartDate not null and not read from Shared preferences we are using the
                // value set by the user in this session using the date picker
                calendar.setTimeInMillis(mStartDate);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                //TODO reconstruct the end date for the range
                // use DatePickerDialog setHighlightedDays method to show the duration of the range


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

                                mStartDateTextView.setText(getDateString(mStartDate));

                                mEndDateTextView.setText(getDateString(mEndDate));

                                showDateRangeDates(true);
                                mDateRangeUpdatedByUser = true;

                                mIsDateFilterOn = true;

                            }
                        },
                        year,month, day);

                dpd.setAutoHighlight(true);
                dpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        showDateRangeDates(false);
                        mDateRangeUpdatedByUser = false;
                        mIsDateFilterOn = false;
                    }
                });

                dpd.show(getFragmentManager(), "Datepickerdialog");

            }
        });


        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilterResult(false, false, mIsDateFilterOn ,
                        mFavoriteSwitch.isChecked() );

                finish();
            }
        });



        Intent intent = getIntent();
        boolean clearFilters = intent.getBooleanExtra(EXTRA_CLEAR_FILTERS, false);
        if (clearFilters){
            // clear all filters
            FilterSharedPreferences.clearFilters(this, true);
            // set return result to include cleared result
            setFilterResult(true, true, false, false);
            finish();
        }
    }



    private void showDateRangeDates(boolean show){
        mShowingDateRange = show;
        if (show){
            mStartDateLabelTextView.setVisibility(View.VISIBLE);
            mStartDateTextView.setVisibility(View.VISIBLE);
            mEndDateLabelTextView.setVisibility(View.VISIBLE);
            mEndDateTextView.setVisibility(View.VISIBLE);

        }else{
            mStartDateLabelTextView.setVisibility(View.INVISIBLE);
            mStartDateTextView.setVisibility(View.INVISIBLE);
            mEndDateLabelTextView.setVisibility(View.INVISIBLE);
            mEndDateTextView.setVisibility(View.INVISIBLE);
        }
    }

    // set the filter results in the return intent
    private void setFilterResult(boolean filtersChanged, boolean cleared, boolean dateFilterOn,
                                 boolean favoriteFilterOn){
        Intent data = new Intent();
        data.putExtra(EXTRA_CHANGE_FILTER_RESULT, filtersChanged);
        data.putExtra(EXTRA_CLEAR_FILTERS, cleared);
        if (!cleared){
            data.putExtra(EXTRA_DATE_FILTER, dateFilterOn);
            data.putExtra(EXTRA_FAVORITE_FILTER, favoriteFilterOn);
        }
        setResult(RESULT_OK, data);
    }

    @Override
    public void onDestroy(){
        mUnbinder.unbind();
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
        // save the set of selected trips
        savedInstanceState.putBoolean(EXTRA_SHOW_DATE_RANGE, mShowingDateRange);
        if (mShowingDateRange){
            savedInstanceState.putLong(EXTRA_START_DATE, mStartDate);
            savedInstanceState.putLong(EXTRA_END_DATE, mEndDate);
        }
        super.onSaveInstanceState(savedInstanceState);
    }


    private void setCurrentFavoriteFilterSelection(){
        mFavoriteSwitch.setChecked(getFavoriteFilterSetting(FilterActivity.this));
    }


    private static String getDateString(long timeStamp){
        Date date = new Date(timeStamp * 1000);
        String dateString = DateFormat.getDateInstance(DateFormat.SHORT).format(date);
        return dateString;
    }
}
