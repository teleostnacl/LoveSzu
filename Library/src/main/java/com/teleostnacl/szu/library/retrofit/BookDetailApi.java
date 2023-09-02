package com.teleostnacl.szu.library.retrofit;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * 图书详细页的Api
 */
public interface BookDetailApi {

    /**
     * 获取图书信息
     *
     * @param ctrlNo 控制号
     * @return 图书信息
     */
    @GET("bookinfo.aspx")
    Observable<Response<ResponseBody>> getBookDetail(@Query("ctrlno") String ctrlNo);

    /**
     * 获取图书封面的url
     *
     * @param map     Post表单 BookModel.getBookCoverUrlMap()
     * @param referer refer
     * @return 图书封面url
     */
    @FormUrlEncoded
    @POST("getBookCoverByDuXiuAjax.aspx")
    @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest"
    })
    Observable<ResponseBody> getBookCoverUrl(@Header("referer") String referer,
                                             @FieldMap Map<String, String> map);

    /**
     * 获取图书封面
     *
     * @param url 图书封面url
     * @return 图书封面
     */
    @GET
    Observable<ResponseBody> getBookCover(@Url String url);

    /**
     * 通过Ajax获取图书的可预约可预借和馆藏信息
     *
     * @param map     请求表单 BookModel.getCommonMap()
     * @param referer refer
     * @return 图书的借阅信息
     */
    @GET("bookinfowithresvAjax.aspx")
    @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest"
    })
    Observable<ResponseBody> getBookCollectionByAjax(@Header("referer") String referer,
                                                     @QueryMap Map<String, String> map);

    /**
     * 获取图书的评分信息
     *
     * @param referer refer
     * @param map     请求表单 BookModel.getCommonMap()
     * @return 评分信息
     */
    @GET("getBookScoreAjax.aspx")
    @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest"
    })
    Observable<ResponseBody> getBookScore(@Header("referer") String referer,
                                          @QueryMap Map<String, String> map);

    /**
     * 获取图书的收藏信息
     *
     * @param referer refer
     * @param map     请求表单 BookModel.getCommonMap()
     * @return 收藏信息
     */
    @GET("getbookcollectcountAjax.aspx")
    @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest"
    })
    Observable<ResponseBody> getBookCollectCount(@Header("referer") String referer,
                                                 @QueryMap Map<String, String> map);

    /**
     * 获取相关图书
     *
     * @param map     请求表单
     * @param referer refer
     * @return 响应
     */
    @GET("showrelatedborrowAjax.aspx")
    @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest"
    })
    Observable<ResponseBody> getRelatedBook(@Header("referer") String referer,
                                            @QueryMap Map<String, String> map);


    /**
     * 收藏书
     *
     * @param referer refer
     * @param map     请求表单 BookModel.getCommonMap()
     * @return 请求结果
     */
    @GET("addBookShelfAjax.aspx")
    @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest"
    })
    Observable<ResponseBody> collectBook(@Header("referer") String referer,
                                         @QueryMap Map<String, String> map);

    /**
     * 评分
     *
     * @param referer refer
     * @param map     请求表单 BookModel.getCommonMap()
     * @param sc      评分
     * @return 评分结果
     */
    @GET("addBookScoreAjax.aspx")
    @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest",
    })
    Observable<ResponseBody> scoreBook(@Header("referer") String referer,
                                       @QueryMap Map<String, String> map,
                                       @Query("sc") String sc);

    /**
     * 检查是否已经登录的请求
     *
     * @return 请求结果
     */
    @GET("login.aspx")
    Observable<Response<ResponseBody>> checkLogin();

    // region 预约与预借

    /**
     * 获取预借信息
     *
     * @param ctrlno 图书控制号
     * @return 预借信息
     */
    @GET("admin_findbook/addfb.aspx")
    Observable<ResponseBody> getBorrowAdvanceInformation(@Query("ctrlno") String ctrlno);

    /**
     * 提交预借
     *
     * @param ctrlno 图书控制号
     * @param map    请求的表单信息
     * @return 预借的结果
     */
    @FormUrlEncoded
    @POST("admin_findbook/addfb.aspx")
    Observable<ResponseBody> borrowInAdvance(@Query("ctrlno") String ctrlno,
                                             @FieldMap Map<String, String> map);

    /**
     * 获取预约信息
     *
     * @param map 查询预约信息的请求表单
     * @return 预约信息
     */
    @GET("resvpresult.aspx")
    Observable<ResponseBody> getReserveInformation(@QueryMap Map<String, String> map);

    /**
     * 提交预约
     *
     * @param queryMap 表单
     * @param fieldMap 表单
     * @return 预约结果
     */
    @FormUrlEncoded
    @POST("resvpresult.aspx")
    Observable<ResponseBody> reserveBook(@QueryMap Map<String, String> queryMap,
                                         @FieldMap Map<String, String> fieldMap);

    // endregion
}
