package com.teleostnacl.common.android.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.teleostnacl.common.android.view.loading.LoadingPopupWindow;
import com.teleostnacl.common.android.context.ColorResourcesUtils;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * 带有loading popup window的Fragment
 */
public abstract class BaseLoadingFragment extends BaseBackFragment {
    protected CompositeDisposable disposable;

    //加载动画
    protected LoadingPopupWindow loadingPopupWindow;

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (disposable != null) {
            disposable.dispose();
        }
        disposable = new CompositeDisposable();
    }

    @Override
    @CallSuper
    public void onDestroyView() {
        super.onDestroyView();

        if (disposable != null) {
            disposable.dispose();
        }

        hideLoadingView();
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
            loadingPopupWindow = new LoadingPopupWindow(requireActivity().getWindow().getDecorView(),
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            loadingPopupWindow.getTextView().setTextColor(Color.BLACK);
            loadingPopupWindow.setClippingEnabled(false);
        }

        loadingPopupWindow.setBackgroundColor(ColorResourcesUtils.getColor(backgroundColor))
                .setTitle(string).postShow();
    }

    /**
     * 隐藏加载动画
     */
    protected void hideLoadingView() {
        if (loadingPopupWindow != null) {
            loadingPopupWindow.dismiss();
        }
    }

    /**
     * @return 加载动画是否正在显示
     */
    protected boolean isLoadingViewShowing() {
        return loadingPopupWindow != null && loadingPopupWindow.isShowing();
    }
}
