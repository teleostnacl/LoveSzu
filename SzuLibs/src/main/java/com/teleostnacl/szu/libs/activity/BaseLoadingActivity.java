package com.teleostnacl.szu.libs.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.teleostnacl.common.android.view.loading.LoadingPopupWindow;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public abstract class BaseLoadingActivity extends BaseActivity {
    //加载动画
    protected LoadingPopupWindow loadingPopupWindow;

    protected CompositeDisposable disposable;

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disposable = new CompositeDisposable();
    }

    /**
     * 显示加载动画
     *
     * @param backgroundColor 背景色
     * @param stringId        标题的字符串id
     */
    protected void showLoadingView(@ColorRes int backgroundColor, @StringRes int stringId) {
        showLoadingView(backgroundColor, getString(stringId));
    }

    /**
     * 显示加载动画
     *
     * @param backgroundColor 背景色
     * @param stringId        标题的字符串id
     * @param formatArgs      参数
     */
    protected void showLoadingView(@ColorRes int backgroundColor, @StringRes int stringId, @Nullable Object... formatArgs) {
        showLoadingView(backgroundColor, getString(stringId, formatArgs));
    }

    /**
     * 显示加载动画
     *
     * @param backgroundColor 背景色
     * @param string          显示的字符串
     */
    protected void showLoadingView(@ColorRes int backgroundColor, String string) {
        if (loadingPopupWindow == null) {
            loadingPopupWindow = new LoadingPopupWindow(getWindow().getDecorView(),
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            loadingPopupWindow.getTextView().setTextColor(Color.BLACK);
            loadingPopupWindow.setClippingEnabled(false);
        }

        loadingPopupWindow.setBackgroundColor(getColor(backgroundColor)).setTitle(string).postShow();
    }

    /**
     * 隐藏加载动画
     */
    protected void hideLoadingView() {
        if (loadingPopupWindow != null) {
            loadingPopupWindow.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposable.dispose();
        hideLoadingView();
    }
}
