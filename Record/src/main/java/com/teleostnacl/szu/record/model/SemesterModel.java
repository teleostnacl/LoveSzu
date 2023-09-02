package com.teleostnacl.szu.record.model;

import android.text.Spanned;
import android.view.View;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.common.android.utils.HtmlUtils;
import com.teleostnacl.szu.record.R;

public class SemesterModel {
    @SerializedName("XNXQMC")
    @Expose
    public String xnxqmc;
    @SerializedName("XNXQDM")
    @Expose
    public String xnxqdm;

    // 标记该学期是否无成绩记录
    public boolean gradeBlank = false;

    // 学期的GPA
    public float semesterGPA;
    // 学期的排名
    public int semesterPM;
    // 学期人数
    public int semesterSum;
    // 学期的相对排名
    public float semesterXDPM;

    // 学年的GPA
    public float gradeGPA;
    // 学年的排名
    public int gradePM;
    // 学年人数
    public int gradeSum;
    // 学年的相对排名
    public float gradeXDPM;

    // 全部的GPA
    public float allGPA;
    // 全部的排名
    public int allPM;
    // 全部 人数
    public int allSum;
    // 全部的相对排名
    public float allXDPM;

    // 在校状态
    public boolean inSchoolStatus;
    // 报道状态
    public boolean reportStatus;
    // 注册状态
    public boolean registerStatus;
    // 缴费状态
    public boolean payStatus;

    // region DataBinding
    public Spanned getSemesterName() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_name, xnxqmc);
    }

    public Spanned getSemesterGPA() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_gpa, semesterGPA);
    }

    public Spanned getSemesterPM() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_ranking, semesterPM, semesterSum);
    }

    public Spanned getSemesterXDPM() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_ranking_percent, semesterXDPM);
    }

    public Spanned getGrade() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_grade);
    }

    public Spanned getGradeGPA() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_grade_gpa, gradeGPA);
    }

    public Spanned getGradePM() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_ranking, gradePM, gradeSum);
    }

    public Spanned getGradeXDPM() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_ranking_percent, gradeXDPM);
    }

    public Spanned getAllGrade() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_grade_all);
    }

    public Spanned getAllGPA() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_all_gpa, allGPA);
    }

    public Spanned getAllPM() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_ranking, allPM, allSum);
    }

    public Spanned getAllXDPM() {
        return HtmlUtils.fromHtml(R.string.growth_record_semester_ranking_percent, allXDPM);
    }

    public Spanned getInSchoolStatus() {
        return HtmlUtils.fromHtml(inSchoolStatus ?
                R.string.growth_record_semester_in_school_status :
                R.string.growth_record_semester_in_school_status_no);
    }


    public Spanned getReportStatus() {
        return HtmlUtils.fromHtml(reportStatus ?
                R.string.growth_record_semester_report_status :
                R.string.growth_record_semester_report_status_no);
    }

    public Spanned getRegisterStatus() {
        return HtmlUtils.fromHtml(registerStatus ?
                R.string.growth_record_semester_register_status :
                R.string.growth_record_semester_register_status_no);
    }

    public Spanned getPayStatus() {
        return HtmlUtils.fromHtml(registerStatus ?
                R.string.growth_record_semester_pay_status :
                R.string.growth_record_semester_pay_status_no);
    }

    public int getGradeVisibility() {
        return gradeBlank ? View.GONE : View.VISIBLE;
    }

    // endregion
}
