package com.teleostnacl.szu.login.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;
import com.teleostnacl.szu.login.model.LoginModel;
import com.teleostnacl.szu.login.repository.LoginRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

public class LoginViewModel extends AndroidViewModel {

    public static boolean isWebVpn = false;

    private final LoginRepository loginRepository;

    //记录所有登录过的LoginModel
    public final List<LoginModel> loginModelList;

    // 登录所使用的LoginModel
    public LoginModel loginModel;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        WebView webView = new WebView(application);
        //noinspection SetJavaScriptEnabled
        webView.getSettings().setJavaScriptEnabled(true);
        loginRepository = new LoginRepository(webView, isWebVpn);

        loginModelList = loginRepository.getAllUser();

        // 设置第一个为登录的Model
        if (loginModelList.size() > 0) {
            loginModel = loginModelList.get(0).clone();
        }
    }

    /**
     * 获取验证码
     */
    public void getCaptcha() {
        loginRepository.getCaptcha(loginModel);
    }

    /**
     * 获取登录表单信息
     *
     * @return 过程是否成功
     */
    public Observable<Boolean> getInformation() {
        loginRepository.clearLogState();
        return loginRepository.getLoginInformation(loginModel)
                .map(loginModel1 -> true)
                .onErrorReturn(throwable -> false)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 使用输入的帐号密码进行登录
     *
     * @return 登录是否成功的观察者
     */
    public Observable<Boolean> loginByPassword() {
        return Observable.just(loginModel)
                .flatMap(loginModel1 -> {
                    // 已经获取了登录表单信息 则不需要再获取
                    if (loginModel1.hasGotten) {
                        return Observable.just(loginModel1);
                    }
                    // 获取登录表单信息
                    else {
                        return loginRepository.getLoginInformation(loginModel1);
                    }
                })
                .flatMap(this::login)
                // 出错之后应重新获取登录信息
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return getInformation().map(aBoolean -> false).blockingFirst();
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 使用上次登录的帐号密码进行登录
     *
     * @param autoLogin 是否自动登录
     * @return 是否登录成功
     */
    public Observable<Boolean> loginByLast(boolean autoLogin) {
        // 历史登录为空 则放回自动登录失败
        if (loginModel == null) {
            return Observable.just(false);
        }

        // 非自动登录 则清除上次登录状态
        if (!autoLogin || TextUtils.isEmpty(loginModel.username) ||
                TextUtils.isEmpty(loginModel.passwordInput) || !loginModel.autoLogin) {
            loginRepository.clearLogState();
            // 清除已登录的Model
            LoginUtil.setUserModel(null);
            return Observable.just(false);
        }

        return loginRepository.checkLogin(loginModel)
                .flatMap(aBoolean -> {
                    if (aBoolean) {
                        return Observable.just(true);
                    }

                    if (LoginUtil.isLoggedIn()) {
                        // 如果LoginUtil已经标示为登录状态, 但是Cookies登录失败了
                        // 则标示当前处于特殊情况, 不再继续登录
                        return Observable.just(false);
                    }
                    return loginRepository.getLoginInformation(loginModel).flatMap(this::login);
                })
                //发生错误则直接返回自动登录失败
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 登录共用的流程
     *
     * @param loginModel 登录表单
     * @return 登录是否成功
     */
    private Observable<Boolean> login(LoginModel loginModel) {
        // 获取加密后的密码
        return loginRepository.setPasswordEncrypt(loginModel)
                // 登录
                .flatMap(loginRepository::login);
    }

    /**
     * 从数据库中移除已登录过的用户
     *
     * @param loginModel 已登录过的用户
     */
    public void removeLoginModel(LoginModel loginModel) {
        loginModelList.remove(loginModel);
        loginRepository.removeLoginModel(loginModel);
    }

    /**
     * @return 返回此次登录是否成功
     */
    public boolean getLoginForCurrent() {
        return loginRepository.getLoginForCurrent();
    }
}