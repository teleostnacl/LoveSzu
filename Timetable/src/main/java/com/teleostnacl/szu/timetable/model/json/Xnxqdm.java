package com.teleostnacl.szu.timetable.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Xnxqdm {
    @Expose
    @SerializedName("datas")
    public Datas datas;

    public static class Datas {
        @Expose
        @SerializedName("xskcb")
        public Xskcb xskcb;

        public static class Xskcb {
            @Expose
            @SerializedName("rows")
            public List<Row> rows = null;

            public static class Row {
                //上课教师
                @Expose
                @SerializedName("SKJS")
                public String SKJS;
                //上课时间包括上课地点
                @Expose
                @SerializedName("YPSJDD")
                public String YPSJDD;
                //课序号
                @Expose
                @SerializedName("KXH")
                public String KXH;
                //课程名
                @Expose
                @SerializedName("KCM")
                public String KCM;
                //课程号
                @Expose
                @SerializedName("KCH")
                public String KCH;
            }
        }
    }
}
