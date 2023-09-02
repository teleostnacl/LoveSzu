package com.teleostnacl.szu.record.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SemesterStatusJson {
    @SerializedName("datas")
    @Expose
    public Datas datas;

    public static class Datas {

        @SerializedName("cxzcxx")
        @Expose
        public Cxzcxx cxzcxx;

        public static class Cxzcxx {

            @SerializedName("rows")
            @Expose
            public List<Row> rows;

            public static class Row {
                @SerializedName("JFZT")
                @Expose
                public String jfzt;
                @SerializedName("SFZX")
                @Expose
                public String sfzx;
                @SerializedName("SFBD")
                @Expose
                public String sfbd;
                @SerializedName("SFZC")
                @Expose
                public String sfzc;
            }
        }

    }
}
