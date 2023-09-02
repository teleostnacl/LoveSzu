package com.teleostnacl.szu.bulletin.retrofit;

import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BulletinApi {
    /**
     * 用于获取搜索时间和发文单位
     *
     * @return 公文通首页
     */
    @GET("https://www1.szu.edu.cn/board/")
    Single<ResponseBody> getInfo();

    /**
     * @param infoType 查询公文通的类型
     * @return 获取10天内所查询类别的公文通
     */
    @GET("infolist.asp")
    Single<ResponseBody> getBulletinsByInfoType(@Query(value = "infotype", encoded = true) String infoType);

    /**
     * @param id 公文通id
     * @return 获取公文通详细信息
     */
    @GET("view.asp")
    Single<ResponseBody> getBulletinContent(@Query("id") String id);

    @POST("infolist.asp")
    @FormUrlEncoded
    Single<ResponseBody> searchBulletin(@FieldMap(encoded = true) Map<String, String> map);
}
