package com.teleostnacl.szu.timetable.retrofit;

import com.teleostnacl.szu.libs.ehall.retrofit.EHallApi;
import com.teleostnacl.szu.timetable.model.json.Xnxqdm;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TimetableApi extends EHallApi {
    @Override
    default String getAppId() {
        return "4770397878132218";
    }

    //获取特定学年学期的课程表的所有课程
    @FormUrlEncoded
    @POST("xskcb/xskcb.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    Single<Xnxqdm> getXSKCB(@Field("XNXQDM") String XNXQDM);

    //获取当前的学期
    @POST("jshkcb/dqxnxq.do")
    @Headers("XMLHttpRequest: XMLHttpRequest")
    Single<ResponseBody> getDQXNXQ();

    //获取学生的学号和入学年份
    @GET("xskcb/cxxsjbxx.do")
    Single<ResponseBody> getXZNJ();

    //获取课程表的开学日期
    @FormUrlEncoded
    @POST("jshkcb/cxjcs.do")
    @Headers("XMLHttpRequest: XMLHttpRequest")
    Single<ResponseBody> getCXJCS(@Field("XN") String xn, @Field("XQ") int xq);
}
