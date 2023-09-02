package com.teleostnacl.common.android.activity;

import com.teleostnacl.common.android.fragment.BaseBackFragment;

/**
 * 为Activity添加Fragment返回键的回调
 */
public interface IBaseBackFragmentActivity {
    void setCurrentFragment(BaseBackFragment fragment);

    BaseBackFragment getCurrentFragment();
}
