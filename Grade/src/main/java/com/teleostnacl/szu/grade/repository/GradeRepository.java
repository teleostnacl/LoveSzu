package com.teleostnacl.szu.grade.repository;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.retrofit.RetrofitUtils;
import com.teleostnacl.szu.grade.model.GradeModel;
import com.teleostnacl.szu.grade.model.GradesModel;
import com.teleostnacl.szu.grade.model.SemesterGradesModel;
import com.teleostnacl.szu.grade.retrofit.GradeApi;
import com.teleostnacl.szu.grade.retrofit.GradeRetrofit;
import com.teleostnacl.szu.libs.ehall.repository.EHallRepository;

import java.util.Locale;
import java.util.Objects;

import io.reactivex.rxjava3.core.Single;

public class GradeRepository {
    private final GradeApi api = GradeRetrofit.getInstance().gradeApi();

    /**
     * 获取所有成绩
     *
     * @param gradesModel GradesModel
     * @return 是否查询成功
     */
    public Single<Boolean> getAllGrade(@NonNull GradesModel gradesModel) {
        gradesModel.grades.clear();

        // 先尝试进入成绩查询模块 获取cookies和referer
        return EHallRepository.getEHallApp(api).flatMap(responseBodyResponse -> {
                    // 获取referer
                    gradesModel.referer = RetrofitUtils.getUrlFromResponse(responseBodyResponse);

                    // 获取学号
                    String body = Objects.requireNonNull(RetrofitUtils.getBodyFromResponse(responseBodyResponse)).string();
                    gradesModel.no = body.split("\"userId\":\"")[1].split("\"")[0];

                    // 获取成绩
                    return api.getAllGrade(gradesModel.referer, gradesModel.no, "1");
                })
                .map(gradeModelJson -> {
                    boolean success = "查询成功".equals(gradeModelJson.datas.xscjcxtjgl.extParams.msg);

                    // 查询成功 则记录成绩
                    if (success) {
                        for (GradeModel gradeModel : gradeModelJson.datas.xscjcxtjgl.rows) {
                            // 获取该学期名记录成绩的list, 没有则进行创建
                            SemesterGradesModel semesterGradesModel = gradesModel.grades.computeIfAbsent(
                                    gradeModel.xnxqdmDisplay, s -> new SemesterGradesModel());
                            // 添加该成绩
                            semesterGradesModel.gradeModelList.add(gradeModel);
                        }

                        // 计算各学期的 所选学分 取得学分 占百分比 平均学分绩点
                        for (SemesterGradesModel model : gradesModel.grades.values()) {
                            // 记录总取得的学分绩点 用于计算平均基点
                            float tmp = 0;

                            for (GradeModel gradeModel : model.gradeModelList) {
                                model.creditsSum += gradeModel.xf;
                                model.creditsGet += gradeModel.qdxf;
                                tmp += gradeModel.jdf;
                            }

                            // 计算百分比
                            model.percent = String.format(Locale.CHINA, "%d%%",
                                    (int) (model.creditsGet * 100 / model.creditsSum));

                            // 计算平均绩点
                            model.pointsAverage = String.format(Locale.CHINA,
                                    "%.02f", tmp / model.creditsSum);
                        }
                    }

                    return success;
                });
    }
}
