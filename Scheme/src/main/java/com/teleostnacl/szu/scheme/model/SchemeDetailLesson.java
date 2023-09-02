package com.teleostnacl.szu.scheme.model;

import android.text.Spanned;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.common.android.utils.HtmlUtils;
import com.teleostnacl.szu.scheme.R;

public class SchemeDetailLesson {
    @SerializedName("XDXQ")
    @Expose
    public String xdxq;
    @SerializedName("PYFADM")
    @Expose
    public String pyfadm;
    @SerializedName("SFZGKC_DISPLAY")
    @Expose
    public String sfzgkcDisplay;
    @SerializedName("XDXNXQ")
    @Expose
    public Object xdxnxq;
    @SerializedName("XS")
    @Expose
    public String xs;
    @SerializedName("KCXZDM_DISPLAY")
    @Expose
    public String kcxzdmDisplay;
    @SerializedName("WID")
    @Expose
    public String wid;
    @SerializedName("XNXQ_DISPLAY")
    @Expose
    public String xnxqDisplay;
    @SerializedName("KSLXDM")
    @Expose
    public String kslxdm;
    @SerializedName("JHXQDM")
    @Expose
    public Object jhxqdm;
    @SerializedName("KZH")
    @Expose
    public String kzh;
    @SerializedName("TYKCBS")
    @Expose
    public String tykcbs;
    @SerializedName("BZ")
    @Expose
    public Object bz;
    @SerializedName("XXKC")
    @Expose
    public Object xxkc;
    @SerializedName("XNXQ")
    @Expose
    public String xnxq;
    @SerializedName("SFZGKC")
    @Expose
    public String sfzgkc;
    @SerializedName("XF")
    @Expose
    public String xf;
    @SerializedName("KSLXDM_DISPLAY")
    @Expose
    public String kslxdmDisplay;
    @SerializedName("JHXNDM")
    @Expose
    public Object jhxndm;
    @SerializedName("CXXQ")
    @Expose
    public Object cxxq;
    @SerializedName("KCM")
    @Expose
    public String kcm;
    @SerializedName("KCXZDM")
    @Expose
    public String kcxzdm;
    @SerializedName("PX")
    @Expose
    public Object px;
    @SerializedName("KCH")
    @Expose
    public String kch;

    // region DataBinding
    public Spanned getName() {
        return HtmlUtils.fromHtml(R.string.scheme_detail_lesson_name, kcm);
    }

    public Spanned getNumber() {
        return HtmlUtils.fromHtml(R.string.scheme_detail_lesson_number, kch, tykcbs);
    }

    public Spanned getDate() {
        return HtmlUtils.fromHtml(R.string.scheme_detail_lesson_date, xnxqDisplay);
    }

    public Spanned getType() {
        return HtmlUtils.fromHtml(R.string.scheme_detail_lesson_type, kcxzdmDisplay, xf);
    }
    // endregion
}
