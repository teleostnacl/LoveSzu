package com.teleostnacl.szu.libs.utils.activity;

public final class SchemeUtil {
    // 广播的ACTION值
    public static final String ACTION_SCHEME = "com.teleostnacl.szu.scheme.SchemeActivity";
    public static final String ACTION_ACADEMIC = "com.teleostnacl.szu.academic.AcademicActivity";

    public static void startScheme() {
        ActivityUtil.sendBroadcast(ACTION_SCHEME);
    }

    public static void startAcademic() {
        ActivityUtil.sendBroadcast(ACTION_ACADEMIC);
    }
}
