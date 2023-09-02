package com.teleostnacl.szu.libs.utils.activity;

public final class ElectricityUtil {
    // 广播的ACTION值
    public static final String ACTION_ELECTRICITY = "com.teleostnacl.szu.electricity.ElectricityActivity";

    public static void startElectricity() {
        ActivityUtil.sendBroadcast(ACTION_ELECTRICITY);
    }
}
