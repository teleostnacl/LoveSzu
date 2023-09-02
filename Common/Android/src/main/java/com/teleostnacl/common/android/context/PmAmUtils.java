package com.teleostnacl.common.android.context;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.teleostnacl.common.android.activity.ActivityUtil;
import com.teleostnacl.common.android.log.Logger;

public class PmAmUtils extends ContextUtils {
    private static final PackageManager pm = getContext().getPackageManager();

    private static final ActivityManager am =
            (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);

    public static PackageManager getPm() {
        return pm;
    }

    public static ActivityManager getAm() {
        return am;
    }

    /**
     * @return 当前应用的包名
     */
    public static String getPackageName() {
        return getContext().getPackageName();
    }

    /**
     * 杀死应用自身
     */
    public static void killSelf() {
        ActivityUtil.clear();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * @return 当前应用的版本
     */
    public static String getVersionName() {
        try {
            PackageInfo pi = getPm().getPackageInfo(getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(e);
        }

        return "";
    }

    /**
     * @return 当前应用的版本号
     */
    public static int getVersionCode() {
        try {
            PackageInfo pi = getPm().getPackageInfo(getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(e);
        }

        return 0;
    }
}
