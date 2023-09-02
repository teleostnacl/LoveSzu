package com.teleostnacl.szu.record.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.szu.record.model.SemesterModel;

import java.util.List;

public class SemesterJson {
    @SerializedName("datas")
    @Expose
    public Datas datas;

    public static class Datas {

        @SerializedName("cxyxkxnxq")
        @Expose
        public Cxyxkxnxq cxyxkxnxq;

        public static class Cxyxkxnxq {
            @SerializedName("rows")
            @Expose
            public List<SemesterModel> rows;
        }

    }
}
