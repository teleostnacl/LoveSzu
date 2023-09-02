package com.teleostnacl.szu.file.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class FileModel {
    // 文件名
    @Expose
    @SerializedName("name")
    public String fileName;

    // 文件下载路径
    @Expose
    @SerializedName("url")
    public String fileUrl;

    // 文件格式
    @Expose
    @SerializedName("format")
    public String fileFormat;

    // webVpn的下载地址
    @Expose
    @SerializedName("web_vpn_url")
    public String webVpnUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileModel)) return false;
        FileModel fileModel = (FileModel) o;
        return Objects.equals(fileName, fileModel.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }
}
