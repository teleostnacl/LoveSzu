package com.teleostnacl.szu.libs.utils.activity;

public final class TimetableUtil {
    // 广播的ACTION值
    public static final String ACTION_TIMETABLE = "com.teleostnacl.szu.timetable.TimetableActivity";

    public static void startTimetable() {
        ActivityUtil.sendBroadcast(ACTION_TIMETABLE);
    }
}
