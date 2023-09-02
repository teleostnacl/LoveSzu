package com.teleostnacl.szu.libs.utils.activity;

public final class DocumentationUtil {
    // 广播的ACTION值
    public static final String ACTION_DOCUMENTATION = "com.teleostnacl.szu.documentation.DocumentationActivity";

    public static void startDocument() {
        ActivityUtil.sendBroadcast(ACTION_DOCUMENTATION);
    }
}
