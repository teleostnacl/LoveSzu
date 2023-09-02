package com.teleostnacl.szu.examination.retrofit;

import com.teleostnacl.szu.examination.model.ExaminationDataModel;
import com.teleostnacl.szu.examination.model.XNXQModelJson;
import com.teleostnacl.szu.libs.ehall.retrofit.EHallApi;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ExaminationApi extends EHallApi {

    @Override
    default String getAppId() {
        return "4768687067472349";
    }

    /**
     * @return 获取学年学期信息
     */
    @POST("modules/wdksap/xnxqcx.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<XNXQModelJson> getXNXQ(@Header("referer") String referer,
                                  @Field("*order") String order);

    /**
     * @return 获取当前学年学期
     */
    @POST("modules/wdksap/cxkspc.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<ResponseBody> getDQXNXQ(@Header("referer") String referer,
                                   @Field("*order") String order,
                                   @Field("pageSize") String pageSize,
                                   @Field("pageNumber") String pageNumber);

    /**
     * @param order  排序方式 -KSRQ
     * @param KSRWZT 1
     * @param XNXQDM 学年学期代码
     * @return 获取安排的考试信息
     */
    @POST("modules/wdksap/wdksap.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<ExaminationDataModel.ScheduledJson> getExaminationData(@Header("referer") String referer,
                                                                  @Field("*order") String order,
                                                                  @Field("KSRWZT") String KSRWZT,
                                                                  @Field("XNXQDM") String XNXQDM);

    /**
     * @return 获取未安排的考试信息
     */
    @POST("modules/wdksap/wapks.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<ExaminationDataModel.UnscheduledJson> getExaminationData(@Header("referer") String referer,
                                                                    @Field("querySetting") String querySetting);

    /**
     * @return 获取服务器的时间
     */
    @POST("modules/wdksap/cxdqfwqsj.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    Single<ResponseBody> getTime(@Header("referer") String referer);
}
