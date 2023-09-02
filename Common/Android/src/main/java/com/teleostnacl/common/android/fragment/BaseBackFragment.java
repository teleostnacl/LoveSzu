package com.teleostnacl.common.android.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.teleostnacl.common.android.activity.IBaseBackFragmentActivity;

public abstract class BaseBackFragment extends BaseLogFragment {

    protected IBaseBackFragmentActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (IBaseBackFragmentActivity) requireActivity();
    }

    /**
     * 返回代码的实现
     * @return 是否消费返回事件
     */
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        //绑定
        activity.setCurrentFragment(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //取消绑定
        if (activity.getCurrentFragment() == this) {
            activity.setCurrentFragment(null);
        }
    }
}
