package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.FilterPreferences.FILTER_PREFERENCES;
import static com.keyeswest.trackme.FilterPreferences.SORT_PREFERENCES_KEY;

public class FilterActivity extends AppCompatActivity {

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, FilterActivity.class);
        return intent;
    }

    public static boolean getFilterChangedResult(Intent data){
        boolean filtersChanged = data.getBooleanExtra(FilterActivity.EXTRA_CHANGE_RESULT,
                true);
        return filtersChanged;
    }

    public static final String EXTRA_CHANGE_RESULT = "extraChangeResult";

    private Unbinder mUnbinder;

    @BindView(R.id.submit_btn)
    Button mSubmitButton;

    @BindView(R.id.default_btn)
    Button mDefaultSettings;

    @BindView(R.id.radioFilterGroup)
    RadioGroup mRadioFilterGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        setFilterResult(false);

        mUnbinder = ButterKnife.bind( this);



        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedId = mRadioFilterGroup.getCheckedRadioButtonId();
                switch (selectedId){
                    case R.id.date_newest_rb:
                        Timber.d("Setting sort order to newest");
                        setSortOrder(FilterActivity.this, SortPreference.NEWEST );

                        break;
                    case R.id.date_oldest_rb:
                        Timber.d("Setting sort order to oldest");
                        setSortOrder(FilterActivity.this, SortPreference.OLDEST );
                        break;

                    case R.id.dist_longest_rb:
                        Timber.d("Setting sort order to longest");
                        setSortOrder(FilterActivity.this, SortPreference.LONGEST );
                        break;

                    case R.id.dist_shortest_rb:
                        Timber.d("Setting sort order to shortest");
                        setSortOrder(FilterActivity.this, SortPreference.SHORTEST );
                        break;

                    default:
                        setSortOrder(FilterActivity.this, SortPreference.NEWEST );
                }

                // send back a result for a snackbar message
                setFilterResult(true);
                finish();


            }
        });

        mDefaultSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterPreferences.saveDefaultPreferences(FilterActivity.this,true);

                setFilterResult(true);
                finish();
            }
        });

    }

    @Override
    public void onDestroy(){
        mUnbinder.unbind();
        super.onDestroy();

    }


    private void setFilterResult(boolean filtersChanged){
        Intent data = new Intent();
        data.putExtra(EXTRA_CHANGE_RESULT, filtersChanged);
        setResult(RESULT_OK, data);

    }

    private static void setSortOrder(Context context, SortPreference preference){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SORT_PREFERENCES_KEY, preference.getCode());
        editor.commit();
    }
}
