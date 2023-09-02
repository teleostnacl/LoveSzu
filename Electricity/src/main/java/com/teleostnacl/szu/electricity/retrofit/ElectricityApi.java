package com.teleostnacl.szu.electricity.retrofit;

import com.teleostnacl.common.android.retrofit.DynamicTimeout;

import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ElectricityApi {

    /**
     * 检查是否可联通电费查询系统, 并获取所有校区信息
     *
     * @return 含校区信息的响应
     */
    @GET("login.do?task=station")
    @DynamicTimeout(timeout = 3)
    Single<ResponseBody> check();

    /**
     * 获取楼栋信息
     *
     * @param client 校区的ip
     * @return 含楼栋信息的响应
     */
    @GET("login.do?task=station")
    Single<ResponseBody> getBuildingId(@Query("client") String client);

    /**
     * 检查所输入的房间号是否有效
     *
     * @param map 请求表单
     * @return 是否有效
     */
    @FormUrlEncoded
    @POST("login.do")
    Single<ResponseBody> checkRoomId(@FieldMap Map<String, String> map);

    /**
     * 查询详细信息
     *
     * @param map 请求表单
     */
    @FormUrlEncoded
    @POST("selectList.do")
    Single<ResponseBody> query(@FieldMap Map<String, String> map);

    /**
     * @return 获取丽湖二期学生宿舍的宿舍楼
     */
    @GET("http://172.25.100.105:8010/")
    Single<ResponseBody> getLiHuSecondBuilding();

    /**
     * @return 获取丽湖二期学生宿舍的电费详细信息
     */
    @POST("http://172.25.100.105:8010/")
    @Headers({"Referer: http://172.25.100.105:8010/",
            "Origin: http://172.25.100.105:8010"})
    @FormUrlEncoded
    Single<ResponseBody> checkLiHuSecondRoom(@FieldMap Map<String, String> map);

    @POST("http://172.25.100.105:8010/usedRecord.aspx")
    @Headers({"Referer: http://172.25.100.105:8010/",
            "Origin: http://172.25.100.105:8010"})
    @FormUrlEncoded
    Single<ResponseBody> queryLiHuSecondUsing(@FieldMap Map<String, String> map);


    @POST("http://172.25.100.105:8010/buyRecord.aspx")
    @Headers({"Referer: http://172.25.100.105:8010/",
            "Origin: http://172.25.100.105:8010"})
    @FormUrlEncoded
    Single<ResponseBody> queryLiHuSecondBuying(@FieldMap Map<String, String> map);
}
