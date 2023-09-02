package com.teleostnacl.szu.scheme.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.szu.scheme.model.SchemeDetailGroup;

import java.util.List;

public class SchemeDetailGroupJson {
    @SerializedName("datas")
    @Expose
    public Datas datas;

    public static class Datas {

        @SerializedName("kzcx")
        @Expose
        public Kzcx kzcx;

        public static class Kzcx {
            @SerializedName("rows")
            @Expose
            public List<SchemeDetailGroup> rows;
        }
    }
}
