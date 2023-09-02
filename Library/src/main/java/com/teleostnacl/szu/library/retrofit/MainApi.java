package com.teleostnacl.szu.library.retrofit;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * 进入主页时所使用的Api
 */
public interface MainApi {
    /**
     * 进入图书馆主页面, 进行获取基本信息
     * @return 基本信息
     */
    @GET("search.aspx")
    Observable<Response<ResponseBody>> getForm();

    /**
     * @return 热门搜索
     */
    @GET("top100.aspx?sparaname=anywords")
    Observable<ResponseBody> getHotSearch();

    /**
     * @return 热门收藏
     */
    @GET("mc_rank.aspx")
    Observable<ResponseBody> getHotCollection();

    /**
     * @param rankMap 表单
     * @return 借阅排行
     */
    @GET("bookrankresult.aspx")
    Observable<ResponseBody> getRankResult(@QueryMap Map<String, String> rankMap);

    /**
     * @return 热门评价
     */
    @GET("user_score_rank.aspx")
    Observable<ResponseBody> getScoreRank();
}
