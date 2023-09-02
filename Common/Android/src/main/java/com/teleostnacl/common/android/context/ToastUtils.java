package com.teleostnacl.common.android.context;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.StringRes;

/**
 * 使用Application的Context创建Toast
 */
public class ToastUtils extends ContextUtils {
    public static void makeToast(CharSequence text) {
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show());
    }

    public static void makeToast(@StringRes int resId) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show());
    }

    public static void makeToast(@StringRes int resId, Object... formatArgs) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(),
                        ResourcesUtils.getString(resId, formatArgs),
                        Toast.LENGTH_SHORT).show());
    }

    public static void makeLongToast(CharSequence text) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show());
    }

    public static void makeLongToast(@StringRes int resId) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), resId, Toast.LENGTH_LONG).show());
    }

    public static void makeLongToast(@StringRes int resId, Object... formatArgs) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(),
                        ResourcesUtils.getString(resId, formatArgs),
                        Toast.LENGTH_LONG).show());
    }
}
