package com.teleostnacl.common.android.retrofit;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.network.NetworkUtils;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;

public class ErrorHandleUtils {
    /**
     * 检查是否为网络错误, 并重试
     * @return retryWhen()所需参数
     */
    @NonNull
    public static Function<Observable<Throwable>, ObservableSource<Object>> retryAnyway() {
        return retryAnyway(null);
    }

    /**
     * 检查是否为网络错误, 并重试
     * @param runnable 重试前执行的操作
     * @return retryWhen()所需参数
     */
    @NonNull
    public static Function<Observable<Throwable>, ObservableSource<Object>> retryAnyway(Runnable runnable) {
        return throwableObservable -> throwableObservable.map(throwable -> {
            if(runnable != null) runnable.run();
            NetworkUtils.errorHandle(throwable);
            return new Object();
        });
    }
}
