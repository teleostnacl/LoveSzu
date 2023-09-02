package com.teleostnacl.szu.libs.utils.activity;

import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.context.ContextUtils;

public final class ActivityUtil {

    public static final String ACTIVITY_ACTION = "ACTIVITY";

    public static final String KEY_ACTIVITY = "KEY_ACTIVITY";

    /**
     * @return 生成ActivityBroadcast的广播过滤器
     */
    @NonNull
    public static IntentFilter getActivityIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTIVITY_ACTION);
        return intentFilter;
    }

    public static void sendBroadcast(@NonNull Intent intent) {
        ContextUtils.sendBroadcast(configIntent(intent));
    }

    public static void sendBroadcast(String activity) {
        sendBroadcast(getIntent(activity));
    }

    /**
     * 获取使用广播启动指定Activity的Intent
     */
    @NonNull
    public static Intent getIntent(String activity) {
        Intent intent = new Intent();
        intent.putExtra(KEY_ACTIVITY, activity);

        return intent;
    }

    /**
     * 配置Intent 增加启动activity 的广播action
     */
    public static Intent configIntent(@NonNull Intent intent) {
        intent.setPackage(ContextUtils.getPackageName());
        intent.setAction(ACTIVITY_ACTION);

        return intent;
    }
}
