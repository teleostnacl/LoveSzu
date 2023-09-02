package com.teleostnacl.szu.scheme;

import android.os.Bundle;
import android.view.ViewStub;

import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.activity.IBaseBackFragmentActivity;
import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.szu.libs.activity.BaseLoadingActivity;
import com.teleostnacl.szu.scheme.viewmodel.SchemeViewModel;

import java.lang.ref.WeakReference;

public class SchemeActivity extends BaseLoadingActivity implements IBaseBackFragmentActivity {

    private SchemeViewModel schemeViewModel;

    public WeakReference<BaseBackFragment> mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme);

        schemeViewModel = new ViewModelProvider(this).get(SchemeViewModel.class);

        showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color, R.string.scheme_loading);
        disposable.add(schemeViewModel.getAllYears().subscribe(aBoolean -> {
            if (aBoolean) {
                initView();
            } else {
                hideLoadingView();
                finish();
            }
        }));
    }

    private void initView() {
        ((ViewStub) findViewById(R.id.layout_scheme_nav_view_stub)).inflate();
        getWindow().getDecorView().post(this::hideLoadingView);
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