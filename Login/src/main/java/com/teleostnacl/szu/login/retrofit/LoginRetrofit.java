package com.teleostnacl.szu.login.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class LoginRetrofit extends BaseRetrofit {

    private static LoginRetrofit loginRetrofit;

    private LoginRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(), 10,
                RxJava3CallAdapterFactory.create(), null);
    }

    public static synchronized LoginRetrofit getInstance() {
        if(loginRetrofit == null) {
            loginRetrofit = new LoginRetrofit();
        }
        return loginRetrofit;
    }

    @Override
    public String getBaseUrl() {return "https://authserver.szu.edu.cn/authserver/";}

    public LoginApi getLoginService() {return create(LoginApi.class);}
}