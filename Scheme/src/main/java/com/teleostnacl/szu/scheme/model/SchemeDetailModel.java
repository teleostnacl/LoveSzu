package com.teleostnacl.szu.scheme.model;

import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.utils.HtmlUtils;
import com.teleostnacl.szu.scheme.R;

import java.util.ArrayList;
import java.util.List;

public class SchemeDetailModel {

    // 专业代码
    @SerializedName("ZYDM")
    @Expose
    public String zydm;

    // 学制年份
    @SerializedName("XZNX")
    @Expose
    public Integer xznx;

    // 年级代码
    @SerializedName("NJDM_DISPLAY")
    @Expose
    public String njdmDisplay;

    // 说明
    @SerializedName("PYMB")
    @Expose
    public String pymb;

    // 开始学期
    @SerializedName("KSXQDM_DISPLAY")
    @Expose
    public String ksxqdmDisplay;

    // 最少要求学分
    @SerializedName("ZSYQXF")
    @Expose
    public Float zsyqxf;

    // 开始学年
    @SerializedName("KSXNDM_DISPLAY")
    @Expose
    public String ksxndmDisplay;

    // 专业方向
    @SerializedName("ZYFXDM_DISPLAY")
    @Expose
    public String zyfxdmDisplay;

    // 专业
    @SerializedName("ZYDM_DISPLAY")
    @Expose
    public String zydmDisplay;

    // 年级
    @SerializedName("NJDM")
    @Expose
    public String njdm;

    // 学期类型
    @SerializedName("XQLXDM_DISPLAY")
    @Expose
    public String xqlxdmDisplay;

    @SerializedName("PYFADM")
    @Expose
    public String pyfadm;

    @SerializedName("WID")
    @Expose
    public String wid;

    // 类型
    @SerializedName("XDLXDM_DISPLAY")
    @Expose
    public String xdlxdmDisplay;

    // 名称
    @SerializedName("PYFAMC")
    @Expose
    public String name;


    @SerializedName("XDLXDM")
    @Expose
    public String xdlxdm;
    @SerializedName("KSXQDM")
    @Expose
    public String ksxqdm;

    @SerializedName("KSXNDM")
    @Expose
    public String ksxndm;


    @SerializedName("XQLXDM")
    @Expose
    public String xqlxdm;

    // 学位
    @SerializedName("XWDM_DISPLAY")
    @Expose
    public String xwdmDisplay;

    // 学院
    @SerializedName("DWDM_DISPLAY")
    @Expose
    public String dwdmDisplay;

    // 记录该培养方案中的所有的模块
    public final List<SchemeDetailGroup> schemeDetailModule = new ArrayList<>();

    // region DataBinding
    public Spanned getName() {
        return HtmlUtils.fromHtml(ResourcesUtils.getString(R.string.item_year_scheme_name, name));
    }

    public Spanned getYear() {
        return HtmlUtils.fromHtml(R.string.item_year_scheme_year_and_major_type, njdmDisplay + " " + xdlxdmDisplay);
    }

    public Spanned getCollegeAndMajor() {
        return HtmlUtils.fromHtml(R.string.item_year_scheme_college_and_major, dwdmDisplay + " " + zydmDisplay);
    }

    public Spanned getMajorFlag() {
        return HtmlUtils.fromHtml(R.string.item_year_scheme_major_flag, zyfxdmDisplay);
    }

    public int getMajorFlagVisibility() {
        return TextUtils.isEmpty(zyfxdmDisplay) ? View.GONE : View.VISIBLE;
    }

    public Spanned getStartYear() {
        return HtmlUtils.fromHtml(R.string.item_year_scheme_start_year, ksxndmDisplay + ksxqdmDisplay);
    }

    public Spanned getCredits() {
        return HtmlUtils.fromHtml(R.string.item_year_scheme_credits, zsyqxf);
    }

    public String getTitleCredits() {
        return ResourcesUtils.getString(R.string.scheme_detail_title_credits, zsyqxf, xdlxdmDisplay);
    }
    // endregion
}
