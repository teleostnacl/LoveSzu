package com.teleostnacl.szu.scheme.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.scheme.R;

import java.util.ArrayList;
import java.util.List;

public class SchemeDetailGroup {
    @SerializedName("PYFADM")
    @Expose
    public String pyfadm;
    @SerializedName("KCXZDM_DISPLAY")
    @Expose
    public String kcxzdmDisplay;
    @SerializedName("WID")
    @Expose
    public String wid;
    @SerializedName("KZLXDM_DISPLAY")
    @Expose
    public String kzlxdmDisplay;
    @SerializedName("BZ")
    @Expose
    public Object bz;
    @SerializedName("SFXGXKZ")
    @Expose
    public String sfxgxkz;
    @SerializedName("KCZXS")
    @Expose
    public Float kczxs;
    @SerializedName("ZYFXMC")
    @Expose
    public Object zyfxmc;
    @SerializedName("KCXZDM")
    @Expose
    public String kcxzdm;
    @SerializedName("XDYQ")
    @Expose
    public Object xdyq;
    @SerializedName("FKZH")
    @Expose
    public String fkzh;
    @SerializedName("KCZXF")
    @Expose
    public Float kczxf;
    @SerializedName("SFXGXKZ_DISPLAY")
    @Expose
    public String sfxgxkzDisplay;
    @SerializedName("KCLBDM_DISPLAY")
    @Expose
    public String kclbdmDisplay;
    @SerializedName("KZH")
    @Expose
    public String kzh;
    @SerializedName("GGKZH")
    @Expose
    public Object ggkzh;
    @SerializedName("ZSXDXF")
    @Expose
    public Float zsxdxf;
    @SerializedName("KZLXDM")
    @Expose
    public String kzlxdm;
    @SerializedName("ZSXDMS")
    @Expose
    public Object zsxdms;
    @SerializedName("XGXKLBDM")
    @Expose
    public Object xgxklbdm;
    @SerializedName("KZM")
    @Expose
    public String name;
    @SerializedName("KCLBDM")
    @Expose
    public String kclbdm;
    @SerializedName("ZYFXDM")
    @Expose
    public Object zyfxdm;
    @SerializedName("PX")
    @Expose
    public Object px;
    @SerializedName("ZSWCKZS")
    @Expose
    public Object zswckzs;
    @SerializedName("KCZMS")
    @Expose
    public Float kczms;

    // 若实例为模块 则记录课程组别
    public final List<SchemeDetailGroup> schemeDetailGroups = new ArrayList<>();

    // 若实例为课程组别, 则记录课程
    public final List<SchemeDetailLesson> schemeDetailLessons = new ArrayList<>();

    public String getCredits() {
        return ResourcesUtils.getString(R.string.scheme_detail_credits, zsxdxf);
    }

    public String getNameAndCredits() {
        return name + (zsxdxf == null ? "" : "\n" + getCredits());
    }
}
