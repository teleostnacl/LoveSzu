package com.teleostnacl.szu.libs.utils.activity;

import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.retrofit.RetrofitUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.SPUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.szu.libs.R;

import org.jetbrains.annotations.Contract;

import java.util.Objects;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subjects.SingleSubject;
import retrofit2.Response;

public final class LoginUtil {

    // 储存的SharedPreferences文件
    private static final String SP_ARG_LOGIN = "login";
    // 记录是否有自动登录账户的键值
    private static final String HAS_AUTO_LOGIN_USER = "has_auto_login_user";

    // 广播的ACTION值
    public static final String ACTION_LOGIN = "com.teleostnacl.szu.login.LoginActivity";
    // 传递是否自动登录的参数
    public static final String ARG_AUTO_FLAG = "1";
    // 登录成功后的回调
    private static Runnable successRunnable;
    // 不登录就退出的回调
    private static Runnable failRunnable;

    private final static MutableLiveData<UserModel> userModelLiveData = new MutableLiveData<>(null);

    /**
     * 登录的URL
     */
    public final static String LOGIN_URL = "https://authserver.szu.edu.cn/authserver/login";

    /**
     * 登录WebVpn的Url
     */
    public final static String WEB_VPN_LOGIN_URL = "https://authserver-443.webvpn.szu.edu.cn/authserver/login";

    public static void login(@Nullable Runnable successRunnable, @Nullable Runnable failRunnable, boolean autoLogin) {
        Intent intent = new Intent();
        intent.putExtra(ActivityUtil.KEY_ACTIVITY, ACTION_LOGIN);

        intent.putExtra(ARG_AUTO_FLAG, autoLogin);

        if (successRunnable != null) {
            if (LoginUtil.successRunnable != null) {
                Runnable oldRunnable = LoginUtil.successRunnable;
                LoginUtil.successRunnable = () -> {
                    oldRunnable.run();
                    successRunnable.run();
                };
            } else {
                LoginUtil.successRunnable = successRunnable;
            }
        }

        if (failRunnable != null) {
            if (LoginUtil.failRunnable != null) {
                Runnable oldRunnable = LoginUtil.failRunnable;
                LoginUtil.failRunnable = () -> {
                    oldRunnable.run();
                    failRunnable.run();
                };
            } else {
                LoginUtil.failRunnable = failRunnable;
            }
        }


        ActivityUtil.sendBroadcast(intent);
    }

    public static void login(@Nullable Runnable successRunnable) {
        login(successRunnable, null);
    }

    public static void login(@Nullable Runnable successRunnable, @Nullable Runnable failRunnable) {
        login(successRunnable, failRunnable, true);
    }

    public static void login(boolean autoLogin) {
        login(null, null, autoLogin);
    }

    /**
     * @return 是否已经登录
     */
    public static boolean isLoggedIn() {
        return getUserModel() != null;
    }

    /**
     * @param url 待检查的url
     * @return url是否为登录的url
     */
    public static boolean isLogUrl(@NonNull String url) {
        boolean needLogin = url.contains(LOGIN_URL) || url.contains(WEB_VPN_LOGIN_URL);
        if (needLogin) {
            ToastUtils.makeToast(R.string.need_login);
        }
        return needLogin;
    }

    /**
     * @return 检查是否有自动登录的账号
     */
    public static boolean hasAutoLoginUser() {
        return SPUtils.getBoolean(SPUtils.getSP(SP_ARG_LOGIN), HAS_AUTO_LOGIN_USER, false);
    }

    /**
     * 记录是否有自动登录的账号
     *
     * @param has 是否有自动登录的账号
     */
    public static void setHasAutoLoginUser(boolean has) {
        SPUtils.putBoolean(SPUtils.getSP(SP_ARG_LOGIN).edit(), HAS_AUTO_LOGIN_USER, has).apply();
    }

    /**
     * 当登录成功时 运行回调方法
     */
    public static void runSuccessRunnable() {
        if (successRunnable != null) {
            try {
                successRunnable.run();
                successRunnable = null;
            } catch (Exception e) {
                Logger.e(e);
            }
        }
    }

    /**
     * 当登录失败时, 运行该Runnable
     */
    public static void runFailRunnable() {
        if (failRunnable != null) {
            try {
                failRunnable.run();
                failRunnable = null;
            } catch (Exception e) {
                Logger.e(e);
            }
        }
    }

    /**
     * 检查并尝试登录
     *
     * @return 是否登录成功
     */
    @NonNull
    public static Single<Boolean> checkLogin() {
        SingleSubject<Boolean> singleSubject = SingleSubject.create();

        if (isLoggedIn()) {
            // 已经登录 则返回登录成功
            singleSubject.onSuccess(true);
        } else {
            // 尝试登录
            login(() -> singleSubject.onSuccess(true), () -> singleSubject.onSuccess(false));
        }

        return singleSubject;
    }

    /**
     * 检查是否为登录的处理
     *
     * @param supplier 登录成功后的处理
     * @param <T>      Response中的数据类型
     * @return Response中的数据
     */
    @NonNull
    public static <T> Function<Response<T>, ObservableSource<T>> checkLoginHandleObservable(Supplier<Observable<T>> supplier) {
        return responseBodyResponse -> {
            // 获取referer
            String url = RetrofitUtils.getUrlFromResponse(responseBodyResponse);

            // 检查是否为登录的url
            if (LoginUtil.isLogUrl(url)) {
                // 为登录的url则进行登录 并重新请求
                return Observable.create(emitter -> login(
                                () -> emitter.onNext(new Object()),
                                () -> emitter.onError(new NetworkUtils.CustomTipException(R.string.need_login))))
                        .flatMap(o -> supplier.get());
            }

            T body = RetrofitUtils.getBodyFromResponse(responseBodyResponse);
            assert body != null;
            return Observable.just(body);
        };
    }

    /**
     * 检查是否为登录的处理
     *
     * @param supplier 登录成功后的处理
     * @param <T>      Response中的数据类型
     * @return Response
     */
    @NonNull
    public static <T> Function<Response<T>, ObservableSource<Response<T>>> checkLoginHandleObservableResponse(Supplier<Observable<Response<T>>> supplier) {
        return responseBodyResponse -> {
            // 获取referer
            String url = RetrofitUtils.getUrlFromResponse(responseBodyResponse);

            // 检查是否为登录的url
            if (LoginUtil.isLogUrl(url)) {
                // 为登录的url则进行登录 并重新请求
                return Observable.create(emitter -> login(
                                () -> emitter.onNext(new Object()),
                                () -> emitter.onError(new NetworkUtils.CustomTipException(R.string.need_login))))
                        .flatMap(o -> supplier.get());
            }

            return Observable.just(responseBodyResponse);
        };
    }

    /**
     * 检查是否为登录的处理
     *
     * @param supplier 登录成功后的处理
     * @param <T>      Response中的数据类型
     * @return Response中的数据
     */
    @NonNull
    public static <T> Function<Response<T>, SingleSource<T>> checkLoginHandleSingle(Supplier<Single<T>> supplier) {
        return responseBodyResponse -> {
            // 获取referer
            String url = RetrofitUtils.getUrlFromResponse(responseBodyResponse);

            // 检查是否为登录的url
            if (LoginUtil.isLogUrl(url)) {
                // 为登录的url则进行登录 并重新请求
                return Single.create(emitter -> login(
                                () -> emitter.onSuccess(new Object()),
                                () -> emitter.onError(new NetworkUtils.CustomTipException(R.string.need_login))))
                        .flatMap(o -> supplier.get());
            }

            T responseBody = RetrofitUtils.getBodyFromResponse(responseBodyResponse);
            assert responseBody != null;
            return Single.just(responseBody);
        };
    }

    /**
     * 检查是否为登录的处理
     *
     * @param supplier 登录成功后的处理
     * @param <T>      Response中的数据类型
     * @return Response
     */
    @NonNull
    public static <T> Function<Response<T>, SingleSource<Response<T>>> checkLoginHandleSingleResponse(Supplier<Single<Response<T>>> supplier) {
        return responseBodyResponse -> {
            // 获取referer
            String url = RetrofitUtils.getUrlFromResponse(responseBodyResponse);

            // 检查是否为登录的url
            if (LoginUtil.isLogUrl(url)) {
                // 为登录的url则进行登录 并重新请求
                return Single.create(emitter -> login(
                                () -> emitter.onSuccess(new Object()),
                                () -> emitter.onError(new NetworkUtils.CustomTipException(R.string.need_login))))
                        .flatMap(o -> supplier.get());
            }

            return Single.just(responseBodyResponse);
        };
    }

    /**
     * @return 出入校审核状态的字符串
     */
    @NonNull
    @Contract(pure = true)
    public static String getVerify() {
        // 已登录且状态不为空
        return LoginUtil.isLoggedIn() && !TextUtils.isEmpty(Objects.requireNonNull(getUserModel()).status) ?
                (": " + getUserModel().status) : "";
    }

    @Nullable
    public static UserModel getUserModel() {
        return userModelLiveData.getValue();
    }

    public static void setUserModel(UserModel userModel) {
        userModelLiveData.postValue(userModel);
    }

    @NonNull
    public static MutableLiveData<UserModel> getUserModelLiveData() {
        return userModelLiveData;
    }

    /**
     * 获取登录信息, 用于展示在subTitle中
     */
    @NonNull
    public static String getLoginInformation() {
        // 未登录时 只显示欢迎使用
        if (isLoggedIn() && !TextUtils.isEmpty(Objects.requireNonNull(getUserModel()).date)) {
            return ResourcesUtils.getString(R.string.login_today_date_with_current_user, getUserModel().date);
        } else {
            return ResourcesUtils.getString(R.string.login_welcome);
        }
    }

    /**
     * 记录已登录的用户信息的类
     */
    public static class UserModel {
        // 用户账号
        public String username;

        // 出入校审核状态
        public String status;

        // 当前日期
        public String date;

        public UserModel(@NonNull String username) {
            this.username = username;
        }
    }
}
