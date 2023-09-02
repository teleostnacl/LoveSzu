package com.teleostnacl.common.android.retrofit.cookies;

import androidx.room.Dao;
import androidx.room.Query;

import com.teleostnacl.common.android.database.CRUDao;

import java.util.List;

@Dao
public interface CookieDao extends CRUDao<CookieEntry, Cookie> {
    @Query("SELECT * FROM cookie")
    List<CookieEntry> getCookiesFromDatabase();

    default List<Cookie> getCookies() {
        return entriesToModels(getCookiesFromDatabase());
    }
}
