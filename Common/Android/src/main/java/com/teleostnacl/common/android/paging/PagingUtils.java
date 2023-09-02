package com.teleostnacl.common.android.paging;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingSource;
import androidx.paging.rxjava3.PagingRx;

import io.reactivex.rxjava3.core.Flowable;
import kotlin.jvm.functions.Function0;
import kotlinx.coroutines.CoroutineScope;

public class PagingUtils {
    @NonNull
    public static  <T> Flowable<PagingData<T>> getFlowable(ViewModel viewModel, Function0<PagingSource<Integer, T>> function0) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(viewModel);
        Pager<Integer, T> pager = new Pager<>(
                new PagingConfig(10),
                function0);

        Flowable<PagingData<T>> flowable = PagingRx.getFlowable(pager);
        return PagingRx.cachedIn(flowable, viewModelScope);
    }
}
