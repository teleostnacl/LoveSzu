package com.teleostnacl.common.android.utils;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;

import com.teleostnacl.common.android.log.Logger;

public final class NavigationUtils {
    public static void navigate(View view, @IdRes int resId) {
        try {
            Navigation.findNavController(view).navigate(resId);
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    public static void navigate(View view, @IdRes int resId, Bundle bundle) {
        try {
            Navigation.findNavController(view).navigate(resId, bundle);
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    public static boolean popBackStack(View view) {
        return Navigation.findNavController(view).popBackStack();
    }

    /**
     * 为toolbar配置setNavigationIcon, 并响应返回操作
     *
     * @param toolbar Toolbar
     */
    public static void navPopBackForToolbar(@NonNull Toolbar toolbar) {
        navigationForToolbar(toolbar, NavigationUtils::popBackStack);
    }

    /**
     * 为为toolbar配置setNavigationIcon, 并自定义响应单击事件
     *
     * @param toolbar  Toolbar
     * @param listener 自定义响应单击事件
     */
    public static void navigationForToolbar(@NonNull Toolbar toolbar, View.OnClickListener listener) {
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(listener);
    }
}
