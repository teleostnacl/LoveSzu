package com.teleostnacl.common.android.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import com.teleostnacl.common.android.log.Logger;

public abstract class BaseLogService extends Service {
    private final String TAG = getClass().getSimpleName() + "[" + hashCode() + "]";

    @Override
    @CallSuper
    public void onCreate() {
        super.onCreate();
        log();
    }

    @Override
    @CallSuper
    public int onStartCommand(Intent intent, int flags, int startId) {
        log();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        log();
    }

    @Nullable
    @Override
    @CallSuper
    public IBinder onBind(Intent intent) {
        log();
        return null;
    }

    @Override
    @CallSuper
    public boolean onUnbind(Intent intent) {
        log();
        return super.onUnbind(intent);
    }

    @Override
    @CallSuper
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        log();
    }

    @Override
    @CallSuper
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        log();
    }

    @Override
    @CallSuper
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        log();
    }

    protected boolean showLog() {
        return false;
    }

    private void log() {
        if (showLog()) {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            Logger.v(TAG, stackTraceElements[3].getMethodName());
        }
    }
}
