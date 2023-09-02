package com.teleostnacl.szu.bulletin;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.activity.IBaseBackFragmentActivity;
import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.szu.bulletin.viewmodel.BulletinViewModel;
import com.teleostnacl.szu.libs.activity.BaseLoadingActivity;

import java.lang.ref.WeakReference;

public class BulletinActivity extends BaseLoadingActivity implements IBaseBackFragmentActivity {

    private BulletinViewModel bulletinViewModel;

    public WeakReference<BaseBackFragment> mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color, R.string.bulletin_loading);

        bulletinViewModel = new ViewModelProvider(this).get(BulletinViewModel.class);

        disposable.add(bulletinViewModel.checkLogin().subscribe(aBoolean -> {
            if (aBoolean) {
                setContentView(R.layout.activity_bulletin);
                getWindow().getDecorView().post(this::hideLoadingView);
            } else {
                finish();
            }
        }));
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
        if (mCurrentFragment == null) {
            return null;
        }

        return mCurrentFragment.get();
    }
}