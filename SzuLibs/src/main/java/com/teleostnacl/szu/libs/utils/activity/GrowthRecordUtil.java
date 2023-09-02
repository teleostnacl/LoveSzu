package com.teleostnacl.szu.libs.utils.activity;

public final class GrowthRecordUtil {
    public static final String ACTION_GROWTH_RECORD = "com.teleostnacl.szu.record.GrowthRecordActivity";

    public static void startGrowthRecord() {
        ActivityUtil.sendBroadcast(ACTION_GROWTH_RECORD);
    }
}
