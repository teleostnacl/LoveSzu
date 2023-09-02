package com.teleostnacl.common.android.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.annotation.StringRes;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.R;
import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.ToastUtils;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class NetworkUtils {
    /**
     * 判断当前是否为Wifi环境
     * @return 是否为Wifi环境
     */
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean isWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) ContextUtils.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * @return 手机的UA
     */
    public static String getUserAgent() {
        return WebSettings.getDefaultUserAgent(ContextUtils.getContext());
    }

    /**
     * 检查是否为网络错误
     * @param throwable 错误
     * @return 是否为网络错误
     */
    public static boolean isNetworkError(@NonNull Throwable throwable) {
        //网络错误,进行处理
        return throwable instanceof UnknownHostException ||
                throwable instanceof SocketException;
    }

    /**
     * 检查是否为网络超时错误
     * @param throwable – 错误
     * @return 是否为网络超时错误
     */
    public static boolean isTimeout(@NonNull Throwable throwable) {
        return throwable instanceof SocketTimeoutException;
    }

    /**
     * 统一错误处理
     * @param throwable 错误
     */
    public static void errorHandle(@NonNull Throwable throwable) {
        // 网络连接错误
        if(isNetworkError(throwable)) {
            ToastUtils.makeToast(R.string.networklibs_network_error);
        }
        // 网络超时错误
        else if(isTimeout(throwable)) {
            ToastUtils.makeToast(R.string.networklibs_network_timeout);
        }
        // 自定义提示的错误
        else if(throwable instanceof CustomTipException) {
            ToastUtils.makeToast(throwable.getMessage());
        }
        else {
            ToastUtils.makeToast(R.string.unknown_error);
            Logger.e(throwable);
        }
    }

    /**
     * 自定义提示的Exception
     */
    public static class CustomTipException extends Exception{
        public CustomTipException(String message) {
            super(message);
        }

        public CustomTipException(@StringRes int message) {
            super(ResourcesUtils.getString(message));
        }
    }
}
