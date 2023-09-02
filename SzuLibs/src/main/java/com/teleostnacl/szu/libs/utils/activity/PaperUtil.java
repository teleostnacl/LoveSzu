package com.teleostnacl.szu.libs.utils.activity;

public final class PaperUtil {
    // 广播的ACTION值
    public static final String ACTION_PAPER = "com.teleostnacl.szu.paper.PaperActivity";

    public static void startPaper() {
        ActivityUtil.sendBroadcast(ACTION_PAPER);
    }
}
