package com.teleostnacl.common.android.retrofit.cookies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Ignore;

import com.teleostnacl.common.android.database.BaseModel;
import com.teleostnacl.common.android.utils.EncryptUtils;

public class Cookie implements BaseModel<CookieEntry> {

    //region cookie信息
    //域名
    public String domain;

    //路径
    public String path;

    //cookie名
    public String name;

    //cookie值
    public String value;

    //过期时间
    public long expiresAt;

    //secure
    public boolean secure;

    //httpOnly
    public boolean httpOnly;

    //persistent
    public boolean persistent;

    //hostOnly
    public boolean hostOnly;
    //endregion

    public Cookie() {
    }

    @Ignore
    public Cookie(@NonNull okhttp3.Cookie cookie) {
        this.domain = cookie.domain();
        this.path = cookie.path();
        this.name = cookie.name();
        //对cookie加密后保存进数据库
        this.value = EncryptUtils.encrypt(cookie.value());
        this.expiresAt = cookie.expiresAt();
        this.secure = cookie.secure();
        this.httpOnly = cookie.httpOnly();
        this.persistent = cookie.persistent();
        this.hostOnly = cookie.hostOnly();
    }

    public okhttp3.Cookie getCookie() {
        okhttp3.Cookie.Builder builder = new okhttp3.Cookie.Builder()
                .domain(domain).path(path).name(name)
                //对cookie进行解密
                .value(EncryptUtils.decrypt(value));
        if (secure) builder.secure();
        if (httpOnly) builder.httpOnly();
        if (persistent) builder.expiresAt(expiresAt);
        if (hostOnly) builder.hostOnlyDomain(domain);
        return builder.build();
    }

    //若域名路径和名字都相同,则表示为同一个cookie
    @Override
    public boolean equals(@Nullable Object other) {
        if (!(other instanceof Cookie)) return false;
        Cookie that = (Cookie) other;
        return that.domain.equals(domain) &&
                that.path.equals(path) &&
                that.name.equals(name);
    }

    @Override
    public CookieEntry toEntry() {
        return new CookieEntry(this);
    }
}
