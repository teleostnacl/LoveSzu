package com.teleostnacl.szu.grade.model;

import android.text.Html;
import android.text.Spanned;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.grade.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 学期成绩使用的Model
 */
public class SemesterGradesModel {
    // 所有成绩
    public final List<GradeModel> gradeModelList = new ArrayList<>();

    // 所选学分
    public float creditsSum;
    // 取得学分
    public float creditsGet;
    // 占百分比
    public String percent;
    // 平均学分绩点
    public String pointsAverage;

    public Spanned getCredits() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.grade_lesson_credit_average) + creditsSum +
                ResourcesUtils.getString(R.string.grade_lesson_credit_get_average) + creditsGet +
                ResourcesUtils.getString(R.string.grade_lesson_percent_average) + percent, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getGrade() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.grade_lesson_grade_points_average) + pointsAverage, Html.FROM_HTML_MODE_COMPACT);
    }
}
