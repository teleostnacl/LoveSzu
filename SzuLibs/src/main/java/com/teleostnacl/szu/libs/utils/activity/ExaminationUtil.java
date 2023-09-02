package com.teleostnacl.szu.libs.utils.activity;

public final class ExaminationUtil {
    // 广播的ACTION值
    public static final String ACTION_EXAMINATION = "com.teleostnacl.szu.examination.ExaminationActivity";

    public static void startExamination() {
        ActivityUtil.sendBroadcast(ACTION_EXAMINATION);
    }
}
