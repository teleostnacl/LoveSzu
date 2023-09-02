package com.teleostnacl.szu.library;

import android.os.Bundle;

import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.teleostnacl.common.android.activity.IBaseBackFragmentActivity;
import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.libs.activity.BaseActivity;

import java.lang.ref.WeakReference;

public class LibraryActivity extends BaseActivity implements IBaseBackFragmentActivity {

    public WeakReference<BaseBackFragment> mCurrentFragment;

    public FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        FragmentContainerView fragmentContainerView = findViewById(R.id.activity_library_nav);

        floatingActionButton = findViewById(R.id.activity_library_float_action_bar);
        floatingActionButton.setOnClickListener(v -> NavigationUtils.navigate(
                fragmentContainerView, R.id.action_global_libraryListFragment));
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