package com.teleostnacl.szu.libs.utils.activity;

public final class GradeUtil {
    // 广播的ACTION值
    public static final String ACTION_GRADE = "com.teleostnacl.szu.grade.GradeActivity";

    public static void startGrade() {
        ActivityUtil.sendBroadcast(ACTION_GRADE);
    }
}
