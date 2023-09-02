package com.teleostnacl.loveszu;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.szu.libs.utils.activity.ActivityUtil;

/**
 * 全局使用广播启动Activity 减少模块间的耦合
 */
public class ActivityBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        Class<? extends Activity> clz = null;
        try {
            // noinspection all
            clz = (Class<? extends Activity>) Class.forName(intent.getStringExtra(ActivityUtil.KEY_ACTIVITY));
        } catch (Exception ignored) {
        }

        if (clz != null) {
            ContextUtils.startActivity(new Intent(context, clz).putExtras(intent));
        }
    }
}