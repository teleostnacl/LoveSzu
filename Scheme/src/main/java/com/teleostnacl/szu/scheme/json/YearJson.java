package com.teleostnacl.szu.scheme.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.szu.scheme.model.YearModel;

import java.util.List;

public class YearJson {

    @SerializedName("datas")
    @Expose
    public Datas datas;

    public static class Datas {

        @SerializedName("code")
        @Expose
        public Code code;

        public static class Code {

            @SerializedName("rows")
            @Expose
            public List<YearModel> rows;
        }
    }
}
