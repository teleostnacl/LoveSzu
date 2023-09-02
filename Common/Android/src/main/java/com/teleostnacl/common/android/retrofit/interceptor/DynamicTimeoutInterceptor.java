package com.teleostnacl.common.android.retrofit.interceptor;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.retrofit.DynamicTimeout;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * 动态配置超时时间的拦截器
 */
public class DynamicTimeoutInterceptor implements Interceptor {
    @NonNull @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        final Invocation tag = request.tag(Invocation.class);
        final Method method = tag != null ? tag.method() : null;
        final DynamicTimeout timeout = method != null ? method.getAnnotation(DynamicTimeout.class) : null;

        if(timeout != null && timeout.timeout() > 0){

            return chain.withConnectTimeout(timeout.timeout(), TimeUnit.SECONDS)
                    .withReadTimeout(timeout.timeout(), TimeUnit.SECONDS)
                    .withWriteTimeout(timeout.timeout(), TimeUnit.SECONDS)
                    .proceed(request);
        }

        return chain.proceed(request);
    }
}
