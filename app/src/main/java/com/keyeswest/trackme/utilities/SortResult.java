package com.keyeswest.trackme.utilities;

import com.keyeswest.trackme.SortPreferenceEnum;

public class SortResult {

    private boolean mSortChanged;
    private SortPreferenceEnum mSelectedSort;

    public SortResult(boolean changed, SortPreferenceEnum selected){
        mSortChanged = changed;
        mSelectedSort = selected;
    }

    public boolean isSortChanged() {
        return mSortChanged;
    }

    public SortPreferenceEnum getSelectedSort() {
        return mSelectedSort;
    }
}
