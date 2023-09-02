package com.teleostnacl.szu.libs.utils.activity;

public final class BulletinUtil {
    // 广播的ACTION值
    public static final String ACTION_BULLETIN = "com.teleostnacl.szu.bulletin.BulletinActivity";

    public static void startBulletin() {
        ActivityUtil.sendBroadcast(ACTION_BULLETIN);
    }
}
