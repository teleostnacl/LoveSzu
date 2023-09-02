package com.teleostnacl.common.android.retrofit.cookies;

import androidx.room.RoomDatabase;

public abstract class CookiesBaseDatabase extends RoomDatabase {

    private CookieJar cookieJar;

    public abstract CookieDao cookieDao();

    public synchronized final CookieJar getCookieJar() {
        if(cookieJar == null) {
            cookieJar = new CookieJar(cookieDao());
        }

        return cookieJar;
    }
}
