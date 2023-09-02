package com.teleostnacl.szu.grade.retrofit;

import com.teleostnacl.szu.grade.model.GradeModelJson;
import com.teleostnacl.szu.libs.ehall.retrofit.EHallApi;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GradeApi extends EHallApi {
    String GRADE_APP_ID = "4768574631264620";

    @Override
    default String getAppId() {
        return GRADE_APP_ID;
    }

    /**
     * 获取全部成绩
     *
     * @param referer referer
     * @param XH      学号
     * @param SFYX    未知 暂定传1
     * @return 全部成绩
     */
    @FormUrlEncoded
    @POST("xscjcxtjgl.do")
    @Headers("X-Requested-With: XMLHttpRequest")
    Single<GradeModelJson> getAllGrade(@Header("referer") String referer,
                                       @Field("XH") String XH,
                                       @Field("SFYX") String SFYX);
}
