package com.liskovsoft.smartyoutubetv2.common.prefs;

import com.liskovsoft.smartyoutubetv2.common.utils.WeakHashSet;

public class DataChangeBase {
    public interface OnDataChange {
        void onDataChange();
    }

    private final WeakHashSet<OnDataChange> mOnChangeList = new WeakHashSet<>();

    public void setOnChange(OnDataChange callback) {
        mOnChangeList.add(callback);
    }

    public void removeOnChange(OnDataChange callback) {
        mOnChangeList.remove(callback);
    }

    protected void persistState() {
        mOnChangeList.forEach(OnDataChange::onDataChange);
    }
}