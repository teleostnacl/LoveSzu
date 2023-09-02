package com.teleostnacl.common.android.retrofit.cookies;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.teleostnacl.common.android.database.BaseEntry;
import com.teleostnacl.common.android.utils.EncryptUtils;

@Entity(tableName = "cookie", primaryKeys = {"domain", "path", "name"})
public class CookieEntry implements BaseEntry<Cookie> {
    //region cookie信息
    //域名
    @NonNull
    @ColumnInfo(name = "domain")
    public final String domain;

    //路径
    @NonNull
    @ColumnInfo(name = "path")
    public final String path;

    //cookie名
    @NonNull
    @ColumnInfo(name = "name")
    public final String name;

    //cookie值
    @ColumnInfo(name = "value")
    public final String value;

    //过期时间
    @ColumnInfo(name = "expiresAt")
    public final long expiresAt;

    //secure
    @ColumnInfo(name = "secure")
    public final boolean secure;

    //httpOnly
    @ColumnInfo(name = "httpOnly")
    public final boolean httpOnly;

    //persistent
    @ColumnInfo(name = "persistent")
    public final boolean persistent;

    //hostOnly
    @ColumnInfo(name = "hostOnly")
    public final boolean hostOnly;

    public CookieEntry(@NonNull String domain, @NonNull String path, @NonNull String name, String value, long expiresAt, boolean secure, boolean httpOnly, boolean persistent, boolean hostOnly) {
        this.domain = domain;
        this.path = path;
        this.name = name;
        this.value = value;
        this.expiresAt = expiresAt;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.persistent = persistent;
        this.hostOnly = hostOnly;
    }

    @Ignore
    public CookieEntry(@NonNull Cookie cookie) {
        this.domain = EncryptUtils.encrypt(cookie.domain);
        this.path = EncryptUtils.encrypt(cookie.path);
        this.name = EncryptUtils.encrypt(cookie.name);
        this.value = EncryptUtils.encrypt(cookie.value);
        this.expiresAt = cookie.expiresAt;
        this.secure = cookie.secure;
        this.httpOnly = cookie.httpOnly;
        this.persistent = cookie.persistent;
        this.hostOnly = cookie.hostOnly;
    }

    @Override
    public Cookie toModel() {
        Cookie cookie = new Cookie();
        cookie.domain = EncryptUtils.decrypt(domain);
        cookie.path = EncryptUtils.decrypt(path);
        cookie.name = EncryptUtils.decrypt(name);
        cookie.value = EncryptUtils.decrypt(value);
        cookie.expiresAt = expiresAt;
        cookie.secure = secure;
        cookie.httpOnly = httpOnly;
        cookie.persistent = persistent;
        cookie.hostOnly = hostOnly;
        return cookie;
    }
}
