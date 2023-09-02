package com.teleostnacl.szu.libs.ehall.retrofit;

import com.teleostnacl.common.android.retrofit.DynamicTimeout;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 深圳大学网上办事大厅公共api
 */
public interface EHallApi {

    /**
     * @return 该模块在EHall对应的Id值
     */
    String getAppId();

    /**
     * 进入指定app页面 获取cookies和referer等信息
     *
     * @param appId 办事大厅模块的appId
     * @return 响应页面
     */
    @GET("http://ehall.szu.edu.cn/appShow")
    @DynamicTimeout(timeout = 60)
    Single<Response<ResponseBody>> get(@Query("appId") String appId);
}
