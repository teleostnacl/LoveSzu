package com.teleostnacl.szu.login.retrofit;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface LoginApi {
    //登录的标题
    String LOGIN_TITLE = "统一身份认证";
    //登录成功后的标题
    String LOGIN_SUCCESSFUL_TITLE = "个人中心";

    /**
     * @return 获取登录表单
     */
    @GET("login")
    Observable<ResponseBody> getLogin();

    /**
     * @return 密码加密的js
     */
    @GET("custom/js/encrypt.js")
    Observable<ResponseBody> getEncrypt();

    /**
     * @return 获取验证码
     */
    @GET("captcha.html")
    Call<ResponseBody> fetchCaptcha();

    /**
     * 进行登录
     *
     * @param loginModelMap 登录表单
     * @return 登录结果
     */
    @FormUrlEncoded
    @POST("login")
    Observable<ResponseBody> login(@FieldMap Map<String, String> loginModelMap);

    /**
     * @return 获取出入校审核状态
     */
    @GET("https://www1.szu.edu.cn/NCP/iQRcode/")
    Observable<ResponseBody> getStatus();

    /**
     * @return 获取当前的日期
     */
    @GET("https://www1.szu.edu.cn/")
    Observable<ResponseBody> getDate();
}
