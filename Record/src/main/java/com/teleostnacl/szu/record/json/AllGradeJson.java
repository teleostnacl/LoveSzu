package com.teleostnacl.szu.record.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 全部成绩GPA 和 排名
 */
public class AllGradeJson {
    @SerializedName("datas")
    @Expose
    public Datas datas;

    public static class Datas {

        @SerializedName("cxxscjtj")
        @Expose
        public Cxxscjtj cxxscjtj;


        public static class Cxxscjtj {
            @SerializedName("rows")
            @Expose
            public List<Row> rows;

            public static class Row {
                @SerializedName("GPA")
                @Expose
                public Float gpa;
                @SerializedName("PM")
                @Expose
                public Float pm;
            }
        }

    }
}
