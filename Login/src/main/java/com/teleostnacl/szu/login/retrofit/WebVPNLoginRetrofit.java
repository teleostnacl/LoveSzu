package com.teleostnacl.szu.login.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class WebVPNLoginRetrofit extends BaseRetrofit {

    private static WebVPNLoginRetrofit loginRetrofit;

    private WebVPNLoginRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(), 10,
                RxJava3CallAdapterFactory.create(), null);
    }

    public static synchronized WebVPNLoginRetrofit getInstance() {
        if(loginRetrofit == null) {
            loginRetrofit = new WebVPNLoginRetrofit();
        }
        return loginRetrofit;
    }

    @Override
    public String getBaseUrl() {return "https://authserver-443.webvpn.szu.edu.cn/authserver/login/";}

    public LoginApi getLoginService() {return create(LoginApi.class);}
}