package com.teleostnacl.szu.record.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class GrowthRecordRetrofit extends BaseRetrofit {

    private static GrowthRecordRetrofit growthRecordRetrofit;

    private GrowthRecordRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(),
                RxJava3CallAdapterFactory.create(), GsonConverterFactory.create());
    }

    public static GrowthRecordRetrofit getInstance() {
        if (growthRecordRetrofit == null) {
            growthRecordRetrofit = new GrowthRecordRetrofit();
        }

        return growthRecordRetrofit;
    }

    @Override
    public String getBaseUrl() {
        return "http://ehall.szu.edu.cn/jwapp/sys/czjl/modules/czjl/";
    }

    public GrowthRecordApi growthRecordApi() {
        return create(GrowthRecordApi.class);
    }
}
