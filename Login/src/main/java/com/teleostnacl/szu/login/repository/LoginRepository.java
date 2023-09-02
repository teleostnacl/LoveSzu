package com.teleostnacl.szu.login.repository;

import android.graphics.BitmapFactory;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.retrofit.RetrofitUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;
import com.teleostnacl.szu.login.R;
import com.teleostnacl.szu.login.database.UserDao;
import com.teleostnacl.szu.login.database.UserDatabase;
import com.teleostnacl.szu.login.model.LoginModel;
import com.teleostnacl.szu.login.retrofit.LoginApi;
import com.teleostnacl.szu.login.retrofit.LoginRetrofit;
import com.teleostnacl.szu.login.retrofit.WebVPNLoginRetrofit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRepository {

    private final WebView webView;

    private final UserDao userDao = UserDatabase.getInstance().userDao();
    private final LoginApi loginApi;

    // 记录此次登录是否成功
    private boolean loginForCurrent = false;

    /**
     * @return 返回此次登录是否成功
     */
    public boolean getLoginForCurrent() {
        return loginForCurrent;
    }

    public LoginRepository(WebView webView, boolean isWebVpn) {
        this.webView = webView;

        loginApi = isWebVpn ? WebVPNLoginRetrofit.getInstance().getLoginService() :
                LoginRetrofit.getInstance().getLoginService();
    }

    /**
     * @return 所有已登录过的账户信息
     */
    public List<LoginModel> getAllUser() {
        return userDao.getAll();
    }

    /**
     * 从数据库中移除已登录过的用户
     *
     * @param loginModel 已登录过的用户
     */
    public void removeLoginModel(LoginModel loginModel) {
        userDao.delete(loginModel);
    }

    /**
     * 清除登录状态
     */
    public void clearLogState() {
        // 清除cookies
        SzuCookiesDatabase.getInstance().getCookieJar().clearCookies();
    }

    /**
     * 检查是否已经登录成功, 并执行登录成功的逻辑
     *
     * @param title url的标题
     * @return 是否已经登录成功
     */
    private boolean checkLoginSuccessfully(LoginModel loginModel, String title) {
        if (!LoginApi.LOGIN_SUCCESSFUL_TITLE.equals(title)) {
            return false;
        }

        ToastUtils.makeToast(R.string.login_success);

        loginForCurrent = true;

        // userModel非空时 属于特殊情况 不进行更新
        if (!LoginUtil.isLoggedIn()) {
            // 标记已登录的Model
            LoginUtil.setUserModel(new LoginUtil.UserModel(loginModel.username));
            // 设置是否自动登录
            LoginUtil.setHasAutoLoginUser(loginModel.autoLogin);

            // 更新数据库
            loginModel.lastLogin = new Date().getTime();
            userDao.insertOrUpdate(loginModel);
        }

        return true;
    }

    /**
     * 登录成功后需要进行的操作
     */
    public Observable<Boolean> runAfterLoginSuccess() {
        return Observable.zip(getStatus(), getDate(), (aBoolean, aBoolean2) -> true)
                .onErrorReturnItem(true);
    }

    /**
     * @return 获取出入校审核状态
     */
    private Observable<Boolean> getStatus() {
        return loginApi.getStatus().map(responseBody -> {
            Element body = Jsoup.parse(new String(responseBody.bytes(), "gb2312")).body();

            Elements elements = body.getElementsByAttributeValue("name", "sendbV");

            String status = elements.select("img").attr("src");

            if (status.equals("yes.png")) {
                Objects.requireNonNull(LoginUtil.getUserModel()).status = "✔";
            } else if (status.equals("no.png")) {
                Objects.requireNonNull(LoginUtil.getUserModel()).status = "❌";
            } else {
                Objects.requireNonNull(LoginUtil.getUserModel()).status = null;
            }

            return true;
        });
    }

    /**
     * 获取当前日期
     */
    private Observable<Boolean> getDate() {
        return loginApi.getDate().map(responseBody -> {
            Element body = Jsoup.parse(new String(responseBody.bytes(), "gb2312")).body();

            Elements elements = body.getElementsByAttributeValue("title", "查看校历");

            if (!elements.isEmpty()) {
                Objects.requireNonNull(LoginUtil.getUserModel()).date = elements.text().replace(" (查看校历)", "");
            }

            return true;
        });
    }

    //region 登录流程

    /**
     * 验证是否已经登录(即尝试使用Cookies登录)
     *
     * @return 是否已经登录
     */
    public Observable<Boolean> checkLogin(LoginModel loginModel) {
        return loginApi.getLogin().map(responseBody ->
                        checkLoginSuccessfully(loginModel, Jsoup.parse(responseBody.string()).title()))
                .flatMap(aBoolean -> {
                    if (!aBoolean) {
                        return Observable.just(false);
                    }

                    // 运行登录成功后的操作
                    return runAfterLoginSuccess();
                });
    }

    /**
     * 获取登录表单信息
     *
     * @param loginModel 用于登录的表单信息
     * @return 登录的表单信息
     */
    public Observable<LoginModel> getLoginInformation(@NonNull LoginModel loginModel) {
        loginModel.hasGotten = false;
        return loginApi.getLogin().map(responseBody -> {
            String temp = responseBody.string();

            //获取表单
            Document document = Jsoup.parse(temp);
            String title = document.title();

            //预期结果标题为"统一身份认证"
            if (!title.equals(LoginApi.LOGIN_TITLE)) throw new Exception();

            //获取pwdDefaultEncryptSalt
            loginModel.pwdDefaultEncryptSalt =
                    temp.split("pwdDefaultEncryptSalt = \"")[1].split("\"")[0];

            Element bodyElement = document.body();
            loginModel.lt = Objects.requireNonNull(bodyElement.getElementsByAttributeValue("name", "lt").first()).val();
            loginModel.dllt = Objects.requireNonNull(bodyElement.getElementsByAttributeValue("name", "dllt").first()).val();
            loginModel.execution = Objects.requireNonNull(bodyElement.getElementsByAttributeValue("name", "execution").first()).val();
            loginModel._eventId = Objects.requireNonNull(bodyElement.getElementsByAttributeValue("name", "_eventId").first()).val();
            loginModel.rmShown = Objects.requireNonNull(bodyElement.getElementsByAttributeValue("name", "rmShown").first()).val();

            loginModel.hasGotten = true;

            return loginModel;
        });
    }

    /**
     * 获取验证码图片
     *
     * @param loginModel 用于保存所获取验证码的登录表单
     */
    public void getCaptcha(@NonNull LoginModel loginModel) {
        loginModel.setCaptcha("");
        loginModel.setNeedCaptcha(true);
        loginModel.setCaptchaBitmap(null);

        loginApi.fetchCaptcha().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    //保存验证码图片
                    byte[] bys = Objects.requireNonNull(RetrofitUtils.getBodyFromResponse(response)).bytes();
                    loginModel.setCaptchaBitmap(BitmapFactory.decodeByteArray(bys, 0, bys.length));
                } catch (IOException e) {
                    onFailure(call, e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Logger.e(t);
                loginModel.setCaptchaBitmap(loginModel.getCaptchaBitmapError());
                ToastUtils.makeToast(R.string.login_captcha_fail);
            }
        });
    }

    /**
     * 设置加密后的密码
     *
     * @param loginModel 用于登录的表单信息
     * @return 登录的表单信息
     */
    public Observable<LoginModel> setPasswordEncrypt(@NonNull LoginModel loginModel) {
        return loginApi.getEncrypt().flatMap(responseBody -> {
            String temp = responseBody.string();
            //拼接获得加密密码的脚本
            String script = "javascript: (function() { " + temp + "\r\n" +
                    "return encryptAES('" + loginModel.passwordInput + "', '"
                    + loginModel.pwdDefaultEncryptSalt + "');}) ();";

            return Observable.create((ObservableOnSubscribe<LoginModel>) emitter -> {
                //运行脚本,获得加密密码
                webView.evaluateJavascript(script, s -> {
                    //将获取到的加密密码去除双引号
                    loginModel.password = s.replace("\"", "");
                    emitter.onNext(loginModel);
                });
            }).subscribeOn(AndroidSchedulers.mainThread());
        });
    }

    /**
     * 登录
     *
     * @param loginModel 用于登录的表单信息
     * @return 登录的表单信息
     */
    public Observable<Boolean> login(@NonNull LoginModel loginModel) {
        return loginApi.login(loginModel.getLoginModelMap())
                .map(responseBody -> {
                    Document document = Jsoup.parse(responseBody.string());
                    String title = document.title();

                    // 检查登录是否成功
                    if (checkLoginSuccessfully(loginModel, title)) {
                        return "successful";
                    } else {
                        if (LoginApi.LOGIN_TITLE.equals(title)) {
                            Element errorMsgElement = document.body().getElementById("errorMsg");
                            if (errorMsgElement != null) {
                                return errorMsgElement.text();
                            }
                        }
                    }
                    // 未知错误
                    return "";
                })
                .flatMap((Function<String, ObservableSource<Boolean>>) errorMsg -> {
                    // 登录成功
                    if (errorMsg.equals("successful")) {
                        // 运行登录成功的结果
                        return runAfterLoginSuccess();
                    }
                    // 登录失败 重新获取表单
                    return getLoginInformation(loginModel).map(loginModel1 -> {
                        switch (errorMsg) {
                            //用户名和密码错误
                            case "您提供的用户名或者密码有误": {
                                //清除密码
                                loginModel.setPasswordInput("");
                                //需要验证码,则更新验证码
                                if (loginModel.needCaptcha) {
                                    getCaptcha(loginModel);
                                }
                                ToastUtils.makeToast(R.string.login_password_error);
                                return false;
                            }
                            //验证码错误
                            case "请输入验证码":
                            case "无效的验证码": {
                                // 清除验证码
                                getCaptcha(loginModel);
                                ToastUtils.makeToast(R.string.login_captcha_error);
                                return false;
                            }
                        }

                        // 未知错误 抛出异常
                        throw new Exception();
                    });
                });
    }
//endregion
}
