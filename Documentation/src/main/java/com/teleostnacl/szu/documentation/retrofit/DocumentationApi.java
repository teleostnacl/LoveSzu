package com.teleostnacl.szu.documentation.retrofit;

import com.teleostnacl.szu.documentation.model.HistoryFileModelJson;
import com.teleostnacl.szu.documentation.model.ItemModelJson;

import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface DocumentationApi {
    /**
     * 登录 并获取cookies
     *
     * @return 登录结果
     */
    @POST("https://authserver.szu.edu.cn/authserver/login?service=https%3A%2F%2Fjwzzfw.szu.edu.cn%2Fwec-self-print-app-console%2Fapp%2Flogin%2FIDS%3Ftype%3D2%26returnUrl%3D%252Fmobile%252FitemList")
    @Headers("Referer: https://authserver.szu.edu.cn/authserver/login?service=https%3A%2F%2Fjwzzfw.szu.edu.cn%2Fwec-self-print-app-console%2Fapp%2Flogin%2FIDS%3Ftype%3D2%26returnUrl%3D%252Fmobile%252FitemList")
    Single<Response<ResponseBody>> init();

    /**
     * 获取个人信息
     *
     * @return 含个人信息的响应
     */
    @GET("app/login/gateway/2")
    @Headers({"X-Requested-With: XMLHttpRequest",
            "Referer: https://jwzzfw.szu.edu.cn/wec-self-print-app-console/mobile.html?profile=hybrid-prod",
            "LoginMode: MOBILE",
            "appId: 80019"})
    Single<ResponseBody> getUserInfo();

    /**
     * @return 可下载的证明文件
     */
    @POST("item/sp-print-item/list/terminal")
    @Headers({"X-Requested-With: XMLHttpRequest",
            "Referer: https://jwzzfw.szu.edu.cn/wec-self-print-app-console/mobile.html?profile=hybrid-prod",
            "LoginMode: MOBILE",
            "appId: 80019"})
    @FormUrlEncoded
    Single<ItemModelJson> getItemList(@FieldMap Map<String, String> map);

    /**
     * @return 历史下载的证明文件
     */
    @GET("order/list")
    @Headers({"X-Requested-With: XMLHttpRequest",
            "Referer: https://jwzzfw.szu.edu.cn/wec-self-print-app-console/mobile.html?profile=hybrid-prod",
            "LoginMode: MOBILE",
            "appId: 80019"})
    Single<HistoryFileModelJson> getHistoryFileList(@QueryMap Map<String, String> map);

    /**
     * 创建扫描件的下载链接
     */
    @POST("order/create")
    @Headers({"X-Requested-With: XMLHttpRequest",
            "Referer: https://jwzzfw.szu.edu.cn/wec-self-print-app-console/mobile.html?profile=hybrid-prod",
            "LoginMode: MOBILE",
            "appId: 80019"})
    @FormUrlEncoded
    Single<ResponseBody> create(@FieldMap Map<String, String> map);
}
