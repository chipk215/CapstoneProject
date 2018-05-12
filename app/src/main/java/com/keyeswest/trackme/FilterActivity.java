package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.keyeswest.trackme.TripListFragment.FILTER_PREFERENCES;
import static com.keyeswest.trackme.TripListFragment.SORT_PREFERENCES_KEY;
import static com.keyeswest.trackme.TripListFragment.saveDefaultPreferences;

public class FilterActivity extends AppCompatActivity {

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, FilterActivity.class);
        return intent;
    }

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

        mUnbinder = ButterKnife.bind( this);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedId = mRadioFilterGroup.getCheckedRadioButtonId();
                switch (selectedId){
                    case R.id.date_newest_rb:

                        setSortOrder(FilterActivity.this, SortPreference.NEWEST );

                        break;
                    case R.id.date_oldest_rb:

                        setSortOrder(FilterActivity.this, SortPreference.OLDEST );
                        break;

                    case R.id.dist_longest_rb:
                        setSortOrder(FilterActivity.this, SortPreference.LONGEST );
                        break;

                    case R.id.dist_shortest_rb:
                        setSortOrder(FilterActivity.this, SortPreference.SHORTEST );
                        break;

                    default:
                        setSortOrder(FilterActivity.this, SortPreference.NEWEST );
                }

                // send back a result for a snackbar message

                finish();
            }
        });

        mDefaultSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDefaultPreferences(FilterActivity.this,true);

                finish();
            }
        });

    }



    private static void setSortOrder(Context context, SortPreference preference){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(FILTER_PREFERENCES, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SORT_PREFERENCES_KEY, preference.getCode());
        editor.commit();
    }
}
