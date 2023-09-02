package com.teleostnacl.szu.login;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.activity.IBaseBackFragmentActivity;
import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.szu.libs.activity.BaseActivity;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;
import com.teleostnacl.szu.login.viewmodel.LoginViewModel;

import java.lang.ref.WeakReference;

public class LoginActivity extends BaseActivity implements IBaseBackFragmentActivity {

    private WeakReference<BaseBackFragment> mCurrentFragment;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Override
    public void finish() {
        super.finish();
        if (loginViewModel.getLoginForCurrent()) {
            // 登录成功的回调
            LoginUtil.runSuccessRunnable();
        } else {
            // 未登录则调用登录失败的Runnable
            LoginUtil.runFailRunnable();
        }
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