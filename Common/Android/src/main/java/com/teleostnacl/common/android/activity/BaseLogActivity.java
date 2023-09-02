package com.teleostnacl.common.android.activity;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.teleostnacl.common.android.log.Logger;

public abstract class BaseLogActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName() + "[" + hashCode() + "]";

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log();
    }

    @Override
    @CallSuper
    protected void onRestart() {
        super.onRestart();
        log();
    }

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        log();
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        log();
    }

    @Override
    @CallSuper
    protected void onPause() {
        super.onPause();
        log();
    }

    @Override
    @CallSuper
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        log();
    }

    @Override
    @CallSuper
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        log();
    }

    @Override
    @CallSuper
    protected void onStop() {
        super.onStop();
        log();
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        super.onDestroy();
        log();
    }

    @Override
    @CallSuper
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        log();
    }

    @Override
    @CallSuper
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        log();
    }

    @Override
    @CallSuper
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        log();
    }

    @Override
    @CallSuper
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        log();
    }

    @Override
    @CallSuper
    public void finish() {
        super.finish();
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
