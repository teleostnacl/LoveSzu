package com.teleostnacl.szu.scheme.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SchemeRetrofit extends BaseRetrofit {

    private static SchemeRetrofit schemeRetrofit;

    private SchemeRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(),
                RxJava3CallAdapterFactory.create(), GsonConverterFactory.create());
    }

    public static SchemeRetrofit getInstance() {
        if (schemeRetrofit == null) {
            schemeRetrofit = new SchemeRetrofit();
        }

        return schemeRetrofit;
    }


    @Override
    public String getBaseUrl() {
        return "http://ehall.szu.edu.cn/jwapp/sys/qxfacx/";
    }

    public SchemeApi schemeApi() {
        return create(SchemeApi.class);
    }
}
