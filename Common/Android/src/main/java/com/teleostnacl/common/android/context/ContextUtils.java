package com.teleostnacl.common.android.context;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.java.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ContextUtils {
    // 记录已经注册了的BroadcastReceiver
    private final static Map<Class<? extends BroadcastReceiver>, BroadcastReceiver> receivers = new HashMap<>();

    public static Context getContext() {
        return BaseApplication.getInstance();
    }

    public static String getPackageName() {
        return getContext().getPackageName();
    }

    public static void startActivity(Class<? extends Activity> clz) {
        startActivity(new Intent(ContextUtils.getContext(), clz));
    }

    public static void startActivity(@NonNull Intent intent) {
        getContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 启动浏览器的Activity进行浏览网页
     *
     * @param url 需要浏览的网页
     */
    public static void startBrowserActivity(String url) {
        startActivity(getStartBrowserActivityIntent(url));
    }

    /**
     * 获取启动浏览器的Activity进行浏览网页的Intent
     */
    @NonNull
    public static Intent getStartBrowserActivityIntent(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        return intent;
    }

    /**
     * 获取由Shortcut启动的Intent
     */
    public static Intent getShortcutIntent(Class<?> cls) {
        return new Intent(getContext(), cls).setAction(Intent.ACTION_VIEW);
    }

    public static void sendBroadcast(Intent intent) {
        getContext().sendBroadcast(intent);
    }

    /**
     * 注册指定的广播接收器
     *
     * @param receiver 广播接收器的类
     * @param filter   过滤器
     */
    public static synchronized void registerReceiver(@NonNull Class<? extends BroadcastReceiver> receiver, IntentFilter filter) {
        if (receivers.containsKey(receiver)) {
            return;
        }

        BroadcastReceiver broadcastReceiver = null;

        try {
            broadcastReceiver = receiver.newInstance();
        } catch (Exception e) {
            Logger.e(e);
        }

        receivers.put(receiver, broadcastReceiver);

        getContext().registerReceiver(broadcastReceiver, filter);
    }

    /**
     * 解注册指定的广播接收器
     *
     * @param receiver 广播接收器的类
     */
    public static synchronized void unregisterReceiver(@NonNull Class<? extends BroadcastReceiver> receiver) {
        // 已注册的广播中含有该class 则进行解注册
        if (receivers.containsKey(receiver)) {
            getContext().unregisterReceiver(receivers.remove(receiver));
        }
    }

    /**
     * @param dictionary app缓存路径下指定的次文件夹名
     * @return 返回app缓存路径下指定的次文件夹名 的缓存路径
     */
    @NonNull
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getDiskCacheDir(String dictionary) {
        File file = new File(getCacheDir(), dictionary);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    /**
     * @return app的缓存路径
     */
    public static File getCacheDir() {
        return getContext().getCacheDir();
    }

    /**
     * 隐藏输入法
     */
    public static void hideSoftInputFromWindow(@NonNull View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
