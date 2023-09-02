package com.teleostnacl.szu.scheme.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.szu.scheme.model.SchemeDetailModel;

import java.util.List;

public class SchemeJson {
    @SerializedName("datas")
    @Expose
    public Datas datas;

    public static class Datas {

        @SerializedName("qxpyfacx")
        @Expose
        public Qxpyfacx qxpyfacx;

        public static class Qxpyfacx {

            @SerializedName("totalSize")
            @Expose
            public Integer totalSize;
            @SerializedName("pageNumber")
            @Expose
            public Integer pageNumber;
            @SerializedName("pageSize")
            @Expose
            public Integer pageSize;
            @SerializedName("rows")
            @Expose
            public List<SchemeDetailModel> rows;
        }
    }
}
