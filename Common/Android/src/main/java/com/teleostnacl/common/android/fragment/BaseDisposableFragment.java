package com.teleostnacl.common.android.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * 含有Disposable的Fragment
 */
public abstract class BaseDisposableFragment extends BaseLogFragment {
    protected CompositeDisposable disposable;

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (disposable != null) {
            disposable.dispose();
        }
        disposable = new CompositeDisposable();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (disposable != null) {
            disposable.dispose();
        }
    }
}
