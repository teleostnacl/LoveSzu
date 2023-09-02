package com.teleostnacl.szu.paper;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.teleostnacl.common.android.activity.IBaseBackFragmentActivity;
import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.szu.libs.activity.BaseActivity;

import java.lang.ref.WeakReference;

public class PaperActivity extends BaseActivity implements IBaseBackFragmentActivity {

    private WeakReference<BaseBackFragment> mCurrentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_paper);
    }

    @Override
    public void onBackPressed() {
        // 响应Fragment的回退事件
        if (getCurrentFragment() != null) {
            if (getCurrentFragment().onBackPressed()) {
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public void setCurrentFragment(BaseBackFragment fragment) {
        this.mCurrentFragment = new WeakReference<>(fragment);
    }

    @Override
    public BaseBackFragment getCurrentFragment() {
        if (mCurrentFragment == null) return null;

        return mCurrentFragment.get();
    }
}
