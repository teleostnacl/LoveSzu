package com.teleostnacl.szu.examination.model;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.examination.R;

import java.util.List;
import java.util.Objects;

/**
 * 考试数据模型
 */
public class ExaminationDataModel {
    // 考场地点
    @SerializedName("JASMC")
    @Expose
    public String jasmc;
    // 考试时间
    @SerializedName("KSSJMS")
    @Expose
    public String kssjms;

    // 开课单位
    @SerializedName("KKDWDM_DISPLAY")
    @Expose
    public String kkdwdmDisplay;
    // 授课教师
    @SerializedName("ZJJS")
    @Expose
    public String zjjs;
    // 课序号
    @SerializedName("KXH")
    @Expose
    public String kxh;
    // 课程名
    @SerializedName("KCM")
    @Expose
    public String kcm;
    // 课程号
    @SerializedName("KCH")
    @Expose
    public String kch;

    // 状态 已完成考试, 未安排时间考试, 未完成考试
    public String status;

    /**
     * 未安排考试的课程JSON
     */
    public static class UnscheduledJson {
        @SerializedName("datas")
        @Expose
        public Datas datas;

        public static class Datas {
            @SerializedName("wapks")
            @Expose
            public Wapks wapks;

            public static class Wapks {
                @SerializedName("rows")
                @Expose
                public List<ExaminationDataModel> rows = null;
            }
        }
    }

    /**
     * 已安排考试的课程JSON
     */
    public static class ScheduledJson {
        @SerializedName("datas")
        @Expose
        public Datas datas;

        public static class Datas {
            @SerializedName("wdksap")
            @Expose
            public Wdksap wdksap;

            public static class Wdksap {
                @SerializedName("rows")
                @Expose
                public List<ExaminationDataModel> rows = null;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExaminationDataModel that = (ExaminationDataModel) o;
        return Objects.equals(kch, that.kch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kch);
    }

    // region Databinding
    public Spanned getLessonName() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.examination_lesson_name) + kcm, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLessonNo() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.examination_lesson_no) + kch + "[" + kxh + "]", Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLessonCollege() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.examination_lesson_college) + kkdwdmDisplay, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLessonTeacher() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.examination_lesson_teacher) + zjjs, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLessonTime() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.examination_lesson_time) + kssjms, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLessonLocation() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.examination_lesson_location) + jasmc, Html.FROM_HTML_MODE_COMPACT);
    }

    public int getTimeAndLocationVisibility() {
        return (TextUtils.isEmpty(jasmc) || TextUtils.isEmpty(kssjms)) ? View.GONE : View.VISIBLE;
    }

    public Spanned getStatus() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.examination_lesson_status) + status, Html.FROM_HTML_MODE_COMPACT);
    }
    // endregion
}
