package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;


import com.keyeswest.trackme.utilities.SortPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.utilities.SortPreferences.SORT_PREFERENCES;
import static com.keyeswest.trackme.utilities.SortPreferences.SORT_PREFERENCES_KEY;

public class SortActivity extends AppCompatActivity {

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, SortActivity.class);
        return intent;
    }

    public static boolean getSortChangedResult(Intent data){
        boolean sortChanged = data.getBooleanExtra(EXTRA_CHANGE_SORT_RESULT,
                true);
        return sortChanged;
    }

    private static final String EXTRA_CHANGE_SORT_RESULT = "extraChangeSortResult";

    private Unbinder mUnbinder;

    @BindView(R.id.submit_btn)
    Button mSubmitButton;


    @BindView(R.id.radioFilterGroup)
    RadioGroup mRadioFilterGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort);
        mUnbinder = ButterKnife.bind( this);

        setCurrentSortPreference();

        setSortResult(false);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedId = mRadioFilterGroup.getCheckedRadioButtonId();
                switch (selectedId){
                    case R.id.date_newest_rb:
                        Timber.d("Setting sort order to newest");
                        setSortOrder(SortPreferenceEnum.NEWEST );

                        break;
                    case R.id.date_oldest_rb:
                        Timber.d("Setting sort order to oldest");
                        setSortOrder(SortPreferenceEnum.OLDEST );
                        break;

                    case R.id.dist_longest_rb:
                        Timber.d("Setting sort order to longest");
                        setSortOrder(SortPreferenceEnum.LONGEST );
                        break;

                    case R.id.dist_shortest_rb:
                        Timber.d("Setting sort order to shortest");
                        setSortOrder( SortPreferenceEnum.SHORTEST );
                        break;

                    default:
                        setSortOrder(SortPreferenceEnum.NEWEST );
                }

                // send back a result for a snackbar message
                setSortResult(true);
                finish();


            }
        });


    }

    private void setCurrentSortPreference() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(SORT_PREFERENCES, MODE_PRIVATE);

        String sortByCode = sharedPreferences.getString(SORT_PREFERENCES_KEY,
                SortPreferences.DEFAULT_SORT.getCode());


        SortPreferenceEnum sortPreference = SortPreferenceEnum.lookupByCode(sortByCode);
        switch (sortPreference){
            case NEWEST:
                mRadioFilterGroup.check(R.id.date_newest_rb);
                break;
            case OLDEST:
                mRadioFilterGroup.check(R.id.date_oldest_rb);
                break;
            case LONGEST:
                mRadioFilterGroup.check(R.id.dist_longest_rb);
                break;
            case SHORTEST:
                mRadioFilterGroup.check(R.id.dist_shortest_rb);
                break;
            default:


        }
    }

    @Override
    public void onDestroy(){
        mUnbinder.unbind();
        super.onDestroy();

    }


    private void setSortResult(boolean sortChanged){
        Intent data = new Intent();
        data.putExtra(EXTRA_CHANGE_SORT_RESULT, sortChanged);
        setResult(RESULT_OK, data);

    }

    private void setSortOrder( SortPreferenceEnum preference){
        SharedPreferences sharedPreferences =
                getSharedPreferences(SORT_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SORT_PREFERENCES_KEY, preference.getCode());
        editor.commit();
    }
}
