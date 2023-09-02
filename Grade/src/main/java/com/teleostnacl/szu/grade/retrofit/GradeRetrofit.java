package com.teleostnacl.szu.grade.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class GradeRetrofit extends BaseRetrofit {

    private static GradeRetrofit gradeRetrofit;

    private GradeRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(), 60,
                RxJava3CallAdapterFactory.create(), GsonConverterFactory.create());
    }

    public static GradeRetrofit getInstance() {
        if (gradeRetrofit == null) {
            gradeRetrofit = new GradeRetrofit();
        }

        return gradeRetrofit;
    }

    @Override
    public String getBaseUrl() {
        return "http://ehall.szu.edu.cn/jwapp/sys/cjcx/modules/cjcx/";
    }

    public GradeApi gradeApi() {
        return create(GradeApi.class);
    }
}
