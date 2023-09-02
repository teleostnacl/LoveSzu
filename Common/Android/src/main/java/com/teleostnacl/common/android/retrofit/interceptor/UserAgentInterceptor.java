package com.teleostnacl.common.android.retrofit.interceptor;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.network.NetworkUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 设置UA的Interceptor
 */
public class UserAgentInterceptor implements Interceptor {

    @NonNull @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        okhttp3.Request.Builder builder = chain.request().newBuilder();
        //设置user-agent
        builder.removeHeader("User-Agent");
        builder.addHeader("User-Agent", NetworkUtils.getUserAgent());
        return chain.proceed(builder.build());
    }
}
