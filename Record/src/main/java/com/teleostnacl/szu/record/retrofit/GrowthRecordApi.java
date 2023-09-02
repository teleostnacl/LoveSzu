package com.teleostnacl.szu.record.retrofit;

import com.teleostnacl.szu.libs.ehall.retrofit.EHallApi;
import com.teleostnacl.szu.record.json.AllGradeJson;
import com.teleostnacl.szu.record.json.SemesterGradeJson;
import com.teleostnacl.szu.record.json.SemesterJson;
import com.teleostnacl.szu.record.json.SemesterStatusJson;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GrowthRecordApi extends EHallApi {
    String GROWTH_RECORD_APP_ID = "4769553753604771";

    @Override
    default String getAppId() {
        return GROWTH_RECORD_APP_ID;
    }

    /**
     * 获取所有学年学期
     *
     * @param xh    学号
     * @param order 恒为+XNXQDM
     * @return 所有学年学期
     */
    @POST("cxyxkxnxq.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<SemesterJson> getAllSemesters(@Field("XH") String xh, @Field("*order") String order);

    /**
     * 获取全部成绩的gpa和排名
     *
     * @param xh 学号
     * @return 含全部成绩的gpa和排名的json
     */
    @POST("cxxscjtj.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<AllGradeJson> getAllGrade(@Field("XH") String xh);

    /**
     * 获取指定学期的 学期/学年/全部的gpa和排名
     *
     * @param xnxq 指定学期
     * @param xh   学号
     * @param type "01" - 全部, "02" - 学期, "03"学年
     * @return json
     */
    @POST("cxxsjdpm.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<SemesterGradeJson> getSemesterGrade(
            @Field("TJXNXQDM") String xnxq, @Field("XH") String xh, @Field("TJLXDM") String type);

    @POST("cxzcxx.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<SemesterStatusJson> getSemesterStatus(@Field("XNXQDM") String xnxqdm);

}
