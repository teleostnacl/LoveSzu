package com.teleostnacl.szu.library.retrofit;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MyLibraryApi {
    /**
     * @return 获取个人信息
     */
    @GET("user/userinfo.aspx")
    Single<Response<ResponseBody>> getUserInfo();

    /**
     * @return 获取修改联系方式的信息
     */
    @GET("user/modifyuserinfo.aspx")
    Observable<ResponseBody> getModifyUserInfo();

    /**
     * 修改联系方式
     *
     * @param fieldMap 修改联系方式使用的表单
     * @return 响应
     */
    @FormUrlEncoded
    @POST("user/modifyuserinfo.aspx")
    @Headers({"referer: http://www.lib.szu.edu.cn/opac/user/modifyuserinfo.aspx"})
    Observable<ResponseBody> modifyUserInfo(@FieldMap Map<String, String> fieldMap);

    /**
     * 获取当前借阅
     *
     * @return 当前借阅的响应
     */
    @GET("user/bookborrowed.aspx")
    @Headers("Referer: http://www.lib.szu.edu.cn/opac/user/bookborrowed.aspx")
    Single<ResponseBody> getCurrentBorrowedBooks();

    /**
     * 获取预约图书
     *
     * @return 含预约图书信息
     */
    @GET("user/resvp.aspx")
    Single<ResponseBody> getReservation();

    /**
     * 取消预约
     *
     * @param map 请求表单
     * @return 结果
     */
    @POST("user/resvp.aspx")
    @FormUrlEncoded
    Observable<ResponseBody> cancelReserve(@FieldMap Map<String, String> map);

    /**
     * 获取预借图书
     *
     * @param page 页码
     * @return 预借图书的信息
     */
    @GET("user/myfb.aspx")
    Single<ResponseBody> getBorrowAdvance(@Query("page") int page);

    //取消预借
    @GET("user/deletefbAjax.aspx")
    @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest",
            "Referer: http://www.lib.szu.edu.cn/opac/user/myfb.aspx?page=1"
    })
    Observable<ResponseBody> cancelBorrowAdvance(@Query("fid") String fid, @Query("_") String blank);

    /**
     * 获取借阅历史
     *
     * @param page 借阅历史的页数
     * @return 含借阅历史的响应
     */
    @GET("user/bookborrowedhistory.aspx")
    Single<ResponseBody> getBookBorrowHistory(@Query("page") int page);

    /**
     * 获取收藏图书
     *
     * @param page 页数
     * @return 含收藏图书的响应
     */
    @GET("user/mybookshelf.aspx")
    Single<ResponseBody> getCollection(@Query("page") int page);

    /**
     * 取消收藏书
     *
     * @param ctrlno 图书控制号
     * @param blank  空字符串""
     * @return 取消结果
     */
    @GET("user/deletebookshelfAjax.aspx?")
    @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest",
            "Referer: http://www.lib.szu.edu.cn/opac/user/mybookshelf.aspx"
    })
    Observable<ResponseBody> cancelCollectBook(@Query("id") String ctrlno, @Query("_") String blank);
}
