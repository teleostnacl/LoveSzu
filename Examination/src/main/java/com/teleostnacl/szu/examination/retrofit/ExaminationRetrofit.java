package com.teleostnacl.szu.examination.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExaminationRetrofit extends BaseRetrofit {

    private static ExaminationRetrofit examinationRetrofit;

    private ExaminationRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(),
                RxJava3CallAdapterFactory.create(), GsonConverterFactory.create());
    }

    public static synchronized ExaminationRetrofit getInstance() {
        if (examinationRetrofit == null) {
            examinationRetrofit = new ExaminationRetrofit();
        }
        return examinationRetrofit;
    }

    @Override
    public String getBaseUrl() {
        return "http://ehall.szu.edu.cn/jwapp/sys/studentWdksapApp/";
    }

    public ExaminationApi examinationApi() {
        return create(ExaminationApi.class);
    }
}
