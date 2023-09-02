package com.teleostnacl.szu.documentation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 可下载的证明文件
 */
public class ItemModel {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("reportUrlPath")
    @Expose
    public String reportUrlPath;
}
