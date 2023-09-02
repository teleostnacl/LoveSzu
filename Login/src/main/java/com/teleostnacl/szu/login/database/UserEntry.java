package com.teleostnacl.szu.login.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.teleostnacl.common.android.database.BaseEntry;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.szu.login.model.LoginModel;

@Entity(tableName = "user")
public class UserEntry implements BaseEntry<LoginModel> {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "username")
    public final String username;
    //记录base加密后的密码
    @ColumnInfo(name = "password")
    public final String password;

    //记住密码
    @ColumnInfo(name = "remember")
    public final boolean remember;
    //自动登录
    @ColumnInfo(name = "auto_login")
    public final boolean autoLogin;
    //记录上次登录得时间
    @ColumnInfo(name = "last_login")
    public final long lastLogin;

    public UserEntry(@NonNull String username, String password, boolean remember, boolean autoLogin, long lastLogin) {
        this.username = username;
        this.password = password;
        this.remember = remember;
        this.autoLogin = autoLogin;
        this.lastLogin = lastLogin;
    }

    @Ignore
    public UserEntry(@NonNull LoginModel loginModel) {
        this.username = EncryptUtils.encrypt(loginModel.username);
        this.password = loginModel.remember ? EncryptUtils.encrypt(loginModel.passwordInput) : "";
        this.remember = loginModel.remember;
        this.autoLogin = loginModel.autoLogin;
        this.lastLogin = loginModel.lastLogin;
    }

    @Override
    public LoginModel toModel() {
        LoginModel loginModel = new LoginModel();
        loginModel.username = EncryptUtils.decrypt(username);
        loginModel.passwordInput = EncryptUtils.decrypt(password);
        loginModel.setRemember(remember);
        loginModel.autoLogin = autoLogin;
        loginModel.lastLogin = lastLogin;
        return loginModel;
    }
}
