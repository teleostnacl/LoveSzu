package com.teleostnacl.common.android.retrofit;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.retrofit.cookies.CookieJar;
import com.teleostnacl.common.android.retrofit.interceptor.DynamicTimeoutInterceptor;
import com.teleostnacl.common.android.retrofit.interceptor.UserAgentInterceptor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Retrofit基类
 */
public abstract class BaseRetrofit {

    private final Retrofit retrofit;

    /**
     * 公共构造方法
     *
     * @param okHttpClient         OkHttpClient
     * @param callAdapterFactories CallAdapterFactory集合
     * @param converterFactories   ConverterFactory集合
     */
    public BaseRetrofit(OkHttpClient okHttpClient,
                        List<CallAdapter.Factory> callAdapterFactories,
                        List<Converter.Factory> converterFactories) {
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(getBaseUrl()).client(okHttpClient);
        if (callAdapterFactories != null) {
            for (CallAdapter.Factory factory : callAdapterFactories) {
                builder.addCallAdapterFactory(factory);
            }
        }

        if (converterFactories != null) {
            for (Converter.Factory factory : converterFactories) {
                builder.addConverterFactory(factory);
            }
        }

        retrofit = builder.build();
    }

    /**
     * 公共构造方法
     *
     * @param okHttpClient       OkHttpClient
     * @param callAdapterFactory CallAdapter.Factory
     * @param converterFactory   Converter.Factory
     */
    public BaseRetrofit(OkHttpClient okHttpClient,
                        CallAdapter.Factory callAdapterFactory,
                        Converter.Factory converterFactory) {
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(getBaseUrl()).client(okHttpClient);

        if (callAdapterFactory != null) builder.addCallAdapterFactory(callAdapterFactory);
        if (converterFactory != null) builder.addConverterFactory(converterFactory);

        retrofit = builder.build();
    }

    /**
     * 快速构建
     *
     * @param cookieJar            用以返回指定CookieJar含有本机UA的OkHttpClient
     * @param callAdapterFactories CallAdapter.Factory集合
     * @param converterFactories   Converter.Factory集合
     */
    public BaseRetrofit(CookieJar cookieJar,
                        List<CallAdapter.Factory> callAdapterFactories,
                        List<Converter.Factory> converterFactories) {
        this(cookieJar, 10, callAdapterFactories, converterFactories);
    }

    /**
     * 快速构建
     *
     * @param cookieJar            用以返回指定CookieJar含有本机UA的OkHttpClient
     * @param timeout              超时时间 单位s
     * @param callAdapterFactories CallAdapter.Factory集合
     * @param converterFactories   Converter.Factory集合
     */
    public BaseRetrofit(CookieJar cookieJar,
                        int timeout,
                        List<CallAdapter.Factory> callAdapterFactories,
                        List<Converter.Factory> converterFactories) {
        this(getOkHttpClient(cookieJar, timeout), callAdapterFactories, converterFactories);
    }

    /**
     * 快速构建
     *
     * @param cookieJar            用以返回指定CookieJar含有本机UA的OkHttpClient
     * @param callAdapterFactories 单个CallAdapterFactory
     */
    public BaseRetrofit(CookieJar cookieJar,
                        CallAdapter.Factory callAdapterFactories,
                        Converter.Factory converterFactory) {
        this(cookieJar, 10, callAdapterFactories, converterFactory);
    }


    /**
     * 快速构建
     *
     * @param cookieJar            用以返回指定CookieJar含有本机UA的OkHttpClient
     * @param timeout              指定超时时间 单位s
     * @param callAdapterFactories 单个CallAdapterFactory
     */
    public BaseRetrofit(CookieJar cookieJar, int timeout,
                        CallAdapter.Factory callAdapterFactories,
                        Converter.Factory converterFactory) {
        this(getOkHttpClient(cookieJar, timeout), callAdapterFactories, converterFactory);
    }

    /**
     * 快速构建
     *
     * @param retrofit 已构建的retrofit
     */
    public BaseRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    /**
     * 返回指定CookieJar含有本机UA的OkHttpClient
     *
     * @param cookieJar 指定CookieJar
     * @return OkHttpClient
     */
    @NonNull
    public static OkHttpClient getOkHttpClient(CookieJar cookieJar) {
        return getOkHttpClient(cookieJar, 10);
    }

    /**
     * 返回指定CookieJar含有本机UA的OkHttpClient, 并指定超时时间
     *
     * @param cookieJar 指定CookieJar
     * @param timeout   指定超时时间, 单位s
     * @return OkHttpClient
     */
    @NonNull
    public static OkHttpClient getOkHttpClient(CookieJar cookieJar, long timeout) {
        return new OkHttpClient.Builder()
                //添加Head信息的拦截器
                .addInterceptor(new UserAgentInterceptor())
                .addInterceptor(new DynamicTimeoutInterceptor())
                //cookies管理
                .cookieJar(cookieJar)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }

    public abstract String getBaseUrl();
}
