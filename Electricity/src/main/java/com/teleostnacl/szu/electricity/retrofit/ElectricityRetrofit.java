package com.teleostnacl.szu.electricity.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class ElectricityRetrofit extends BaseRetrofit {
    private static ElectricityRetrofit electricityRetrofit;

    private ElectricityRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(), 30,
                RxJava3CallAdapterFactory.create(), null);
    }

    public static synchronized ElectricityRetrofit getInstance() {
        if (electricityRetrofit == null) {
            electricityRetrofit = new ElectricityRetrofit();
        }
        return electricityRetrofit;
    }

    @Override
    public String getBaseUrl() {
        return "http://192.168.84.3:9090/cgcSims/";
    }

    public ElectricityApi getApi() {
        return create(ElectricityApi.class);
    }
}
