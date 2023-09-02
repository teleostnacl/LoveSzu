package com.teleostnacl.szu.libs.retrofit;

import androidx.room.Database;
import androidx.room.Room;

import com.teleostnacl.common.android.retrofit.cookies.CookieEntry;
import com.teleostnacl.common.android.retrofit.cookies.CookiesBaseDatabase;
import com.teleostnacl.common.android.context.ContextUtils;

@Database(entities = {CookieEntry.class}, version = 1, exportSchema = false)
public abstract class SzuCookiesDatabase extends CookiesBaseDatabase {
    private static final String DATABASE_NAME = "SzuCookies.db";

    private static SzuCookiesDatabase szuCookiesDatabase;

    public synchronized static SzuCookiesDatabase getInstance() {
        if (szuCookiesDatabase == null) {
            szuCookiesDatabase = Room.databaseBuilder(ContextUtils.getContext(),
                            SzuCookiesDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }

        return szuCookiesDatabase;
    }
}
