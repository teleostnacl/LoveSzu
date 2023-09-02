package com.teleostnacl.szu.grade.model;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.grade.R;

/**
 * 单科成绩使用的Model
 */
public class GradeModel {
    @SerializedName("TSYYMC")
    @Expose
    public String tsyymc;
    @SerializedName("KCXZDM_DISPLAY")
    @Expose
    public String kcxzdmDisplay;
    @SerializedName("QDXF")
    @Expose
    public Float qdxf;
    @SerializedName("JDF")
    @Expose
    public Float jdf;
    @SerializedName("XF")
    @Expose
    public Float xf;
    @SerializedName("XNXQDM_DISPLAY")
    @Expose
    public String xnxqdmDisplay;
    @SerializedName("DJCJMC")
    @Expose
    public String djcjmc;
    @SerializedName("KCM")
    @Expose
    public String kcm;
    @SerializedName("XFJD")
    @Expose
    public String xfjd;
    @SerializedName("KCH")
    @Expose
    public String kch;

    public Spanned getLessonNo() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.grade_lesson_no) + kch + " [" + kcxzdmDisplay + "]", Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLessonName() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.grade_lesson_name) + kcm, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getCredits() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.grade_lesson_credit) + xf +
                ResourcesUtils.getString(R.string.grade_lesson_credit_get) + qdxf, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getGrade() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.grade_lesson_grade) + djcjmc +
                ResourcesUtils.getString(R.string.grade_lesson_points) + xfjd +
                ResourcesUtils.getString(R.string.grade_lesson_grade_points) + jdf, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getTips() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.grade_lesson_tips) + tsyymc, Html.FROM_HTML_MODE_COMPACT);
    }

    public int getTipsVisibility() {
        return TextUtils.isEmpty(tsyymc) ? View.GONE : View.VISIBLE;
    }
}
