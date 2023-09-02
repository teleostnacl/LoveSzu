package com.teleostnacl.common.android.retrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import retrofit2.Response;

/**
 * 与Retrofit相关的工具类
 */
public class RetrofitUtils {

    /**
     * 从Response中获取url
     *
     * @return url
     */
    @NonNull
    public static String getUrlFromResponse(Response<?> response) {
        if (response == null) {
            return "";
        }

        return response.raw().request().url().toString();
    }

    /**
     * @return 获取Response的body
     */
    @Nullable
    public static <T> T getBodyFromResponse(Response<T> response) {
        return response.body();
    }
}
