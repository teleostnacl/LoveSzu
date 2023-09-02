package com.teleostnacl.szu.timetable;

import android.os.Bundle;

import com.teleostnacl.common.android.activity.IBaseBackFragmentActivity;
import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.szu.libs.activity.BaseActivity;

import java.lang.ref.WeakReference;

public class TimetableActivity extends BaseActivity implements IBaseBackFragmentActivity {

    private WeakReference<BaseBackFragment> mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
    }

    @Override
    public void setCurrentFragment(BaseBackFragment fragment) {
        this.mCurrentFragment = new WeakReference<>(fragment);
    }

    @Override
    public BaseBackFragment getCurrentFragment() {
        if (mCurrentFragment == null) {
            return null;
        }
        return mCurrentFragment.get();
    }
}