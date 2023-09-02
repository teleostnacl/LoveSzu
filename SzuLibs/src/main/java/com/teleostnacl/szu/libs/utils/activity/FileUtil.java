package com.teleostnacl.szu.libs.utils.activity;

public final class FileUtil {
    // 广播的ACTION值
    public static final String ACTION_FILE = "com.teleostnacl.szu.file.FileActivity";

    public static void startFile() {
        ActivityUtil.sendBroadcast(ACTION_FILE);
    }
}
