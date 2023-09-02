package com.teleostnacl.szu.record.repository;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.retrofit.RetrofitUtils;
import com.teleostnacl.szu.libs.ehall.repository.EHallRepository;
import com.teleostnacl.szu.record.model.GrowthRecordModel;
import com.teleostnacl.szu.record.model.SemesterModel;
import com.teleostnacl.szu.record.retrofit.GrowthRecordApi;
import com.teleostnacl.szu.record.retrofit.GrowthRecordRetrofit;

import java.util.Objects;

import io.reactivex.rxjava3.core.Single;

public class GrowthRecordRepository {
    private final GrowthRecordApi api = GrowthRecordRetrofit.getInstance().growthRecordApi();

    /**
     * 获取全部学期
     *
     * @return 是否成功
     */
    public Single<Boolean> getAllSemesters(GrowthRecordModel model) {
        return EHallRepository.getEHallApp(api).flatMap(responseBodyResponse -> {
            model.referer = RetrofitUtils.getUrlFromResponse(responseBodyResponse);

            // 获取学号
            String body = Objects.requireNonNull(RetrofitUtils.getBodyFromResponse(responseBodyResponse)).string();
            model.no = body.split("\"USERID\":\"")[1].split("\"")[0];

            return Single.zip(api.getAllSemesters(model.no, "+XNXQDM"), api.getAllGrade(model.no), (semesterJson, allGradeJson) -> {

                // 所有学年学期
                model.semesterModelList.addAll(semesterJson.datas.cxyxkxnxq.rows);

                // 总gpa
                model.gpa = allGradeJson.datas.cxxscjtj.rows.get(0).gpa;
                // 总排名
                model.pm = allGradeJson.datas.cxxscjtj.rows.get(0).pm.intValue();

                return true;
            });
        });
    }

    /**
     * 获取指定学年的成长记录
     */
    public Single<SemesterModel> getSemesterGrowthRecord(GrowthRecordModel model, SemesterModel semesterModel) {
        return Single.zip(getSemesterGrade(model, semesterModel), getSemesterStatus(semesterModel),
                (semesterModel1, semesterModel2) -> semesterModel1);
    }

    /**
     * 获取指定学期的成绩
     */
    private Single<SemesterModel> getSemesterGrade(@NonNull GrowthRecordModel model, @NonNull SemesterModel semesterModel) {
        return Single.zip(
                // 截止到该学期的全部成绩
                api.getSemesterGrade(semesterModel.xnxqdm, model.no, "01"),
                // 该学期的学期成绩
                api.getSemesterGrade(semesterModel.xnxqdm, model.no, "02"),
                // 该学期所在学年的成绩
                api.getSemesterGrade(semesterModel.xnxqdm, model.no, "03"),
                (semesterGradeJson, semesterGradeJson2, semesterGradeJson3) -> {
                    try {
                        semesterModel.semesterGPA = semesterGradeJson2.datas.cxxsjdpm.rows.get(0).gpa;
                        semesterModel.semesterPM = semesterGradeJson2.datas.cxxsjdpm.rows.get(0).pm.intValue();
                        semesterModel.semesterSum = semesterGradeJson2.datas.cxxsjdpm.rows.get(0).cypmrs.intValue();
                        semesterModel.semesterXDPM = semesterGradeJson2.datas.cxxsjdpm.rows.get(0).xdpm;

                        semesterModel.gradeGPA = semesterGradeJson3.datas.cxxsjdpm.rows.get(0).gpa;
                        semesterModel.gradePM = semesterGradeJson3.datas.cxxsjdpm.rows.get(0).pm.intValue();
                        semesterModel.gradeSum = semesterGradeJson3.datas.cxxsjdpm.rows.get(0).cypmrs.intValue();
                        semesterModel.gradeXDPM = semesterGradeJson3.datas.cxxsjdpm.rows.get(0).xdpm;

                        semesterModel.allGPA = semesterGradeJson.datas.cxxsjdpm.rows.get(0).gpa;
                        semesterModel.allPM = semesterGradeJson.datas.cxxsjdpm.rows.get(0).pm.intValue();
                        semesterModel.allSum = semesterGradeJson.datas.cxxsjdpm.rows.get(0).cypmrs.intValue();
                        semesterModel.allXDPM = semesterGradeJson.datas.cxxsjdpm.rows.get(0).xdpm;

                    } catch (Exception ignored) {
                        semesterModel.gradeBlank = true;
                    }

                    return semesterModel;
                });
    }

    /**
     * 获取指定学期的状态
     */
    private Single<SemesterModel> getSemesterStatus(@NonNull SemesterModel semesterModel) {
        return api.getSemesterStatus(semesterModel.xnxqdm).map(semesterStatusJson -> {
            semesterModel.inSchoolStatus = "1".equals(semesterStatusJson.datas.cxzcxx.rows.get(0).sfzx);
            semesterModel.reportStatus = "1".equals(semesterStatusJson.datas.cxzcxx.rows.get(0).sfbd);
            semesterModel.registerStatus = "1".equals(semesterStatusJson.datas.cxzcxx.rows.get(0).sfzc);
            semesterModel.payStatus = "1".equals(semesterStatusJson.datas.cxzcxx.rows.get(0).jfzt);

            return semesterModel;
        });
    }
}
