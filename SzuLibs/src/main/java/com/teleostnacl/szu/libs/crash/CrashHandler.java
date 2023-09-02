package com.teleostnacl.szu.libs.crash;

import static com.teleostnacl.szu.libs.crash.CrashActivity.ARG_CRASH_MESSAGE;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.PmAmUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Thread crashThread;
    private Throwable crashThrowable;

    // 单例模式
    private static final CrashHandler instance = new CrashHandler();

    private CrashHandler() {
    }

    /**
     * 初始化
     */
    public static void init() {
        Thread.setDefaultUncaughtExceptionHandler(instance);
    }

    /**
     * @return 获取崩溃日志
     */
    @NonNull
    public static String getCrashString() {

        StringBuilder stringBuilder = new StringBuilder();

        // 获取版本号
        stringBuilder.append(PmAmUtils.getVersionName())
                .append("(").append(PmAmUtils.getVersionCode()).append(")")
                .append("\n");

        // 获取崩溃的线程信息
        stringBuilder.append(instance.crashThread.toString()).append("\n");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        instance.crashThrowable.printStackTrace(pw);

        // 记录错误堆栈信息
        stringBuilder.append(sw);

        return stringBuilder.toString();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        this.crashThread = t;
        this.crashThrowable = e;

        Logger.e(e);

        Intent intent = new Intent(ContextUtils.getContext(), CrashActivity.class);
        intent.putExtra(ARG_CRASH_MESSAGE, getCrashString());
        ContextUtils.startActivity(intent);

        PmAmUtils.killSelf();
    }
}
