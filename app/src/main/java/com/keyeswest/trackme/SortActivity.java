package com.keyeswest.trackme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;


import com.keyeswest.trackme.utilities.SortSharedPreferences;
import com.keyeswest.trackme.utilities.SortResult;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.keyeswest.trackme.utilities.SortSharedPreferences.SORT_PREFERENCES;
import static com.keyeswest.trackme.utilities.SortSharedPreferences.SORT_PREFERENCES_KEY;

public class SortActivity extends AppCompatActivity {

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, SortActivity.class);
        return intent;
    }

    public static SortResult getSortChangedResult(Intent data){
        boolean sortChanged = data.getBooleanExtra(EXTRA_CHANGE_SORT_RESULT,
                true);

        SortPreferenceEnum selected = SortPreferenceEnum.lookupByCode(data.getStringExtra(EXTRA_SELECTED_SORT ));
        return new SortResult(sortChanged, selected);
    }

    private static final String EXTRA_CHANGE_SORT_RESULT = "extraChangeSortResult";
    private static final String EXTRA_SELECTED_SORT = "extraSelectedSort";

    private Unbinder mUnbinder;

    private SortPreferenceEnum mSelectedSort;

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
                        mSelectedSort = SortPreferenceEnum.NEWEST;
                        break;
                    case R.id.date_oldest_rb:
                        Timber.d("Setting sort order to oldest");
                        mSelectedSort = SortPreferenceEnum.OLDEST;
                        break;

                    case R.id.dist_longest_rb:
                        Timber.d("Setting sort order to longest");
                        mSelectedSort = SortPreferenceEnum.LONGEST;
                        break;

                    case R.id.dist_shortest_rb:
                        Timber.d("Setting sort order to shortest");
                        mSelectedSort = SortPreferenceEnum.SHORTEST;
                        break;

                    default:
                        mSelectedSort = SortPreferenceEnum.NEWEST;
                }

                setSortOrder(mSelectedSort);

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
                SortSharedPreferences.DEFAULT_SORT.getCode());


        mSelectedSort= SortPreferenceEnum.lookupByCode(sortByCode);
        switch (mSelectedSort){
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
        data.putExtra(EXTRA_SELECTED_SORT, mSelectedSort.getCode());
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
