package com.teleostnacl.szu.library.retrofit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * 搜索时用的api
 */
public interface SearchApi {

    /**
     * @param searchMode 检索范围
     * @param keywords 检索的关键字
     * @return 搜索建议
     */
    @GET("showsearchsuggestionAjax.aspx") @Headers({
            "X-Prototype-Version: 1.5.0",
            "X-Requested-With: XMLHttpRequest",
            "Referer: http://www.lib.szu.edu.cn/opac/search.aspx"
    })
    Observable<ResponseBody> showSearchSuggestion(@Query("st") String searchMode, @Query("s") String keywords);

    /**
     * 搜索
     * @param path 检索内容所形成的链接
     * @return 搜索结果
     */
    @GET
    Single<Response<ResponseBody>> search(@Url String path);

    /**
     * 获取搜索范围
     * @param path - 搜索范围所形成的链接
     * @return 搜索范围的结果
     */
    @GET
    Observable<Response<ResponseBody>> getSearchFields(@Url String path);
}
