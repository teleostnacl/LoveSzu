package com.teleostnacl.szu.libs.utils.activity;

public final class LibraryUtil {
    // 广播的ACTION值
    public static final String ACTION_LIBRARY = "com.teleostnacl.szu.library.LibraryActivity";

    public static void startLibrary() {
        ActivityUtil.sendBroadcast(ACTION_LIBRARY);
    }
}
