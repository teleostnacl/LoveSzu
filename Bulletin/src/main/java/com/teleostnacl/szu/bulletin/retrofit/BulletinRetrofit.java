package com.teleostnacl.szu.bulletin.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class BulletinRetrofit extends BaseRetrofit {
    private static BulletinRetrofit bulletinRetrofit;

    private BulletinRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(), 30,
                RxJava3CallAdapterFactory.create(), null);
    }

    public static synchronized BulletinRetrofit getInstance() {
        if (bulletinRetrofit == null) {
            bulletinRetrofit = new BulletinRetrofit();
        }
        return bulletinRetrofit;
    }

    @Override
    public String getBaseUrl() {
        return "https://www1.szu.edu.cn/board/";
    }

    public BulletinApi bulletinApi() {
        return create(BulletinApi.class);
    }

}
