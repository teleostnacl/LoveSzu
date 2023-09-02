package com.teleostnacl.common.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.teleostnacl.common.android.log.Logger;

public abstract class BaseLogFragment extends Fragment {
    private final String TAG = getClass().getSimpleName() + "[" + hashCode() + "]";

    @Override
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log();
    }

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        log();
    }

    @Nullable
    @Override
    @CallSuper
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        log();
        return null;
    }

    @Override
    @CallSuper
    public void onStart() {
        super.onStart();
        log();
    }

    @Override
    @CallSuper
    public void onResume() {
        super.onResume();
        log();
    }

    @Override
    @CallSuper
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        log();
    }

    @Override
    @CallSuper
    public void onDetach() {
        super.onDetach();
        log();
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        log();
    }

    @Override
    @CallSuper
    public void onStop() {
        super.onStop();
        log();
    }

    @Override
    @CallSuper
    public void onDestroyView() {
        super.onDestroyView();
        log();
    }

    @Override
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        log();
    }

    @Override
    @CallSuper
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        log();
    }

    @Override
    @CallSuper
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
