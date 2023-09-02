package com.teleostnacl.common.android.log;

import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;
import static android.util.Log.getStackTraceString;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public final class Logger {

    /**
     * 用于只打印方法名
     *
     * @param object 调用的对象
     */
    public static void v(@Nullable Object object) {
        // 获取调用的方法名
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        log(VERBOSE, getObject(object, stackTraceElements), getMethod(stackTraceElements), null);
    }

    /**
     * 用于打印指定tag的日志输出
     *
     * @param tag tag
     * @param msg 日志输出
     */
    public static void v(String tag, String msg) {
        log(VERBOSE, tag, msg, null);
    }

    /**
     * 用于打印异常
     *
     * @param tr Throwable
     */
    public static void e(Throwable tr) {
        log(ERROR, "", "", tr);
    }

    /**
     * 从堆栈中获取打印日志时的对象
     *
     * @param object             调用的对象
     * @param stackTraceElements 堆栈
     * @return 全限定名@[hash] (只有当object不为空时, 才打印hash)
     */
    @NonNull
    private static String getObject(@Nullable Object object, @NonNull StackTraceElement[] stackTraceElements) {
        StringBuilder stringBuilder = new StringBuilder();

        // 全限定类名
        stringBuilder.append(stackTraceElements[3].getClassName());
        if (object != null) {
            // hashcode
            stringBuilder.append("@[").append(String.format(Locale.CHINA, "%x", object.hashCode())).append("]");
        }

        return stringBuilder.toString();
    }

    /**
     * 从堆栈中获取打印日志时的方法名及行号
     *
     * @param stackTraceElements 堆栈
     * @return 方法名() 行号
     */
    @NonNull
    private static String getMethod(@NonNull StackTraceElement[] stackTraceElements) {
        // 方法名
        return stackTraceElements[3].getMethodName() + "()" + " " +
                // 行号
                stackTraceElements[3].getLineNumber();
    }

    /**
     * 从堆栈中获取打印日志时的上一个Object
     *
     * @return { from 全限定类名.方法名() 行号 }
     */
    @NonNull
    private static String getLastObject(@NonNull StackTraceElement[] stackTraceElements) {
        StringBuilder stringBuilder = new StringBuilder();
        // 可进行溯源, 打印调用其的上一个函数
        if (stackTraceElements.length > 4) {
            stringBuilder.append("{ from ")
                    // 类名
                    .append(stackTraceElements[4].getClassName()).append(".")
                    // 方法名
                    .append(stackTraceElements[4].getMethodName()).append("()").append(" ")
                    // 行号
                    .append(stackTraceElements[4].getLineNumber()).append(" }");
        }

        return stringBuilder.toString();
    }

    private static void log(int priority, String tag, String msg, Throwable tr) {
        logToTerminal(priority, tag, msg, tr);
    }

    /**
     * 将日志输出的文件中
     */
    private static void logToFile() {

    }

    /**
     * 将日志输出到终端中
     */
    private static void logToTerminal(int priority, String tag, String msg, Throwable tr) {
        switch (priority) {
            case VERBOSE:
                Log.v(tag, msg);
                break;
            case INFO:
            case WARN:
                break;
            case ERROR:
                Log.e("", " \n" + getStackTraceString(tr));
                break;
        }
    }
}
