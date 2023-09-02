package com.teleostnacl.szu.scheme.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class YearModel {
    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("name")
    @Expose
    public String name;
}
