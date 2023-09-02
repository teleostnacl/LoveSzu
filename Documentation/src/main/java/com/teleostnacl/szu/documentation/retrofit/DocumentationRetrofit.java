package com.teleostnacl.szu.documentation.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DocumentationRetrofit extends BaseRetrofit {
    private static DocumentationRetrofit documentationRetrofit;

    private DocumentationRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(), 60,
                RxJava3CallAdapterFactory.create(), GsonConverterFactory.create());
    }

    public static synchronized DocumentationRetrofit getInstance() {
        if (documentationRetrofit == null) {
            documentationRetrofit = new DocumentationRetrofit();
        }
        return documentationRetrofit;
    }

    @Override
    public String getBaseUrl() {
        return "https://jwzzfw.szu.edu.cn/wec-self-print-app-console/";
    }

    public DocumentationApi getApi() {
        return create(DocumentationApi.class);
    }
}
