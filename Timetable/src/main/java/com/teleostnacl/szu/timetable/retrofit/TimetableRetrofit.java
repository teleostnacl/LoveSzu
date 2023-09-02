package com.teleostnacl.szu.timetable.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class TimetableRetrofit extends BaseRetrofit {

    private static TimetableRetrofit timetableRetrofit;

    private TimetableRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(),
                RxJava3CallAdapterFactory.create(), GsonConverterFactory.create());
    }

    public static TimetableRetrofit getInstance() {
        if (timetableRetrofit == null) {
            timetableRetrofit = new TimetableRetrofit();
        }

        return timetableRetrofit;
    }

    @Override
    public String getBaseUrl() {
        return "http://ehall.szu.edu.cn/jwapp/sys/wdkb/modules/";
    }

    public TimetableApi timetableApi() {
        return create(TimetableApi.class);
    }

}
