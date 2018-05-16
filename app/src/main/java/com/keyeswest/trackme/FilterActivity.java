package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.keyeswest.trackme.utilities.FilterSharedPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.keyeswest.trackme.utilities.FilterSharedPreferences.FAVORITE_PREFERENCES_KEY;
import static com.keyeswest.trackme.utilities.FilterSharedPreferences.FILTER_PREFERENCES;

public class FilterActivity extends AppCompatActivity {

    public static Intent newIntent(Context packageContext, boolean clearFilter){
        Intent intent = new Intent(packageContext, FilterActivity.class);
        intent.putExtra(EXTRA_CLEAR_FILTERS, clearFilter);
        return intent;
    }

    public static boolean getFilterChangedResult(Intent data){
        boolean sortChanged = data.getBooleanExtra(EXTRA_CHANGE_FILTER_RESULT,
                true);
        return sortChanged;
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

    @BindView(R.id.favorite_sw)
    Switch mFavoriteSwitch;

    @BindView(R.id.cancel_btn)
    Button mCancelButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        mUnbinder = ButterKnife.bind( this);

        setCurrentFilterSelection();

        mSubmitButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                // process the favorite filter
                boolean isSelected = mFavoriteSwitch.isChecked();
                setFavoriteFilter(isSelected);

                setFilterResult(true, false);
                finish();

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


    private void setFavoriteFilter(boolean isSelected){
        SharedPreferences sharedPreferences =
                getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FAVORITE_PREFERENCES_KEY, isSelected);
        editor.commit();

    }


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

    private void setCurrentFilterSelection(){
        SharedPreferences sharedPreferences =
                getSharedPreferences(FILTER_PREFERENCES, MODE_PRIVATE);

        boolean filterFavorites = sharedPreferences.getBoolean(FAVORITE_PREFERENCES_KEY, false);

        mFavoriteSwitch.setChecked(filterFavorites);

    }
}
