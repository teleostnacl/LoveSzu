package com.teleostnacl.szu.scheme.retrofit;

import com.teleostnacl.szu.libs.ehall.retrofit.EHallApi;
import com.teleostnacl.szu.scheme.json.SchemeDetailGroupJson;
import com.teleostnacl.szu.scheme.json.SchemeDetailLessonJson;
import com.teleostnacl.szu.scheme.json.SchemeJson;
import com.teleostnacl.szu.scheme.json.YearJson;

import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SchemeApi extends EHallApi {
    String SCHEME_APP_ID = "4766860087431764";

    @Override
    default String getAppId() {
        return SCHEME_APP_ID;
    }

    /**
     * @return 获取全部可查询方案的年级
     */
    @GET("http://ehall.szu.edu.cn/jwapp/code/528f6869-3c24-4be5-be3b-8977cc9ec611.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    Single<YearJson> getAllYears(@Header("referer") String refer);

    /**
     * @return 获取指定年份的所有培养方案
     */
    @POST("modules/pyfacxepg/qxpyfacx.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<SchemeJson> getAllSchemesByYear(@Header("referer") String refer, @FieldMap Map<String, String> map);

    /**
     * @return 获取指定培养方案的课组信息
     */
    @POST("modules/pyfacxepg/kzcx.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<SchemeDetailGroupJson> getSchemeDetailGroups(@Header("referer") String refer, @Field("PYFADM") String pyfadm);

    /**
     * @return 获取指定课组的课程信息
     */
    @POST("modules/pyfacxepg/kzkccx.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<SchemeDetailLessonJson> getSchemeDetailLessons(@Header("referer") String refer, @Field("PYFADM") String pyfadm);
}
