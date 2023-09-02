package com.teleostnacl.szu.library.retrofit;

import com.teleostnacl.common.android.retrofit.BaseRetrofit;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class LibraryRetrofit extends BaseRetrofit {

    private static LibraryRetrofit libraryRetrofit;

    private LibraryRetrofit() {
        super(SzuCookiesDatabase.getInstance().getCookieJar(),
                RxJava3CallAdapterFactory.create(), null);
    }

    public static synchronized LibraryRetrofit getInstance() {
        if (libraryRetrofit == null) {
            libraryRetrofit = new LibraryRetrofit();
        }
        return libraryRetrofit;
    }

    @Override
    public String getBaseUrl() {
        return "http://www.lib.szu.edu.cn/opac/";
    }

    public MainApi getLibraryApi() {
        return create(MainApi.class);
    }

    public SearchApi getSearchApi() {
        return create(SearchApi.class);
    }

    public BookDetailApi getBookDetailApi() {
        return create(BookDetailApi.class);
    }

    public MyLibraryApi getMyLibraryApi() {
        return create(MyLibraryApi.class);
    }
}
