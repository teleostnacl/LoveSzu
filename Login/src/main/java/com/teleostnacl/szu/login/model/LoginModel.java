package com.teleostnacl.szu.login.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.teleostnacl.common.android.database.BaseModel;
import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.retrofit.QueryFieldMap;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.login.BR;
import com.teleostnacl.szu.login.R;
import com.teleostnacl.szu.login.database.UserEntry;

import java.util.Map;
import java.util.Objects;

public class LoginModel extends BaseObservable implements Cloneable, BaseModel<UserEntry> {

    // 登录表单
    public String username;
    public String password;
    public String captcha;
    public String lt;
    public String dllt;
    public String execution;
    public String _eventId;
    public String rmShown;

    // 记录登录表单是否已经获取
    public boolean hasGotten = false;

    // 加密的salt
    public String pwdDefaultEncryptSalt;
    // 是否需要验证码
    public boolean needCaptcha = false;
    // 输入的密码
    public String passwordInput;
    // 自动登录是否可见
    public int autoLoginVisibility = View.GONE;

    // 保存验证码的位图
    private Bitmap captchaBitmap;

    // 保存验证码错误的位图
    private static Bitmap captchaBitmapError;

    // 记住密码
    public boolean remember = false;
    //自动登录
    public boolean autoLogin = false;
    //记录上次登录得时间
    public long lastLogin;

    public LoginModel() {
        //设置默认值
        username = "";
        captcha = "";
        passwordInput = "";
    }

    //region DataBinding
    @NonNull
    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    @Bindable
    public String getPasswordInput() {
        return passwordInput;
    }

    public void setPasswordInput(String password) {
        this.passwordInput = password;
        notifyPropertyChanged(BR.passwordInput);
    }

    @Bindable
    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
        notifyPropertyChanged(BR.captcha);
    }

    @Bindable
    public boolean getRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
        if (remember) {
            setAutoLoginVisibility(View.VISIBLE);
        } else {
            setAutoLoginVisibility(View.GONE);
            setAutoLogin(false);
        }
        notifyPropertyChanged(BR.remember);
    }

    @Bindable
    public Boolean getAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(Boolean autoLogin) {
        this.autoLogin = autoLogin;
        notifyPropertyChanged(BR.autoLogin);
    }

    @Bindable
    public int getAutoLoginVisibility() {
        return autoLoginVisibility;
    }

    public void setAutoLoginVisibility(int autoLoginVisibility) {
        this.autoLoginVisibility = autoLoginVisibility;
        notifyPropertyChanged(BR.autoLoginVisibility);
    }

    @Bindable
    public boolean isNeedCaptcha() {
        return needCaptcha;
    }

    public void setNeedCaptcha(boolean needCaptcha) {
        this.needCaptcha = needCaptcha;
        notifyPropertyChanged(BR.needCaptcha);
    }

    @Bindable
    public Bitmap getCaptchaBitmap() {
        return captchaBitmap;
    }

    public void setCaptchaBitmap(Bitmap captchaBitmap) {
        this.captchaBitmap = captchaBitmap;
        notifyPropertyChanged(BR.captchaBitmap);
    }
    //endregion

    public Bitmap getCaptchaBitmapError() {
        if (captchaBitmapError == null) {
            Drawable drawable = ResourcesUtils.getDrawable(R.drawable.ic_login_captcha_error);
            assert drawable != null;
            captchaBitmapError = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicWidth(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(captchaBitmapError);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return captchaBitmapError;
    }

    //获取登录表单的Map
    public Map<String, String> getLoginModelMap() {
        Map<String, String> map = new QueryFieldMap();
        map.put("username", username);
        map.put("password", password);
        map.put("captchaResponse", captcha);
        map.put("lt", lt);
        map.put("dllt", dllt);
        map.put("execution", execution);
        map.put("_eventId", _eventId);
        map.put("rmShown", rmShown);
        map.put("rememberMe", "on");
        return map;
    }

    @NonNull
    @Override
    public LoginModel clone() {
        LoginModel loginModel;
        try {
            loginModel = (LoginModel) super.clone();
        } catch (CloneNotSupportedException e) {
            Logger.e(e);
            loginModel = new LoginModel();
        }
        return loginModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginModel that = (LoginModel) o;
        return remember == that.remember && autoLogin == that.autoLogin && username.equals(that.username) && Objects.equals(passwordInput, that.passwordInput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, passwordInput, remember, autoLogin);
    }

    @Override
    public UserEntry toEntry() {
        return new UserEntry(this);
    }
}