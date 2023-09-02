package com.teleostnacl.szu.scheme.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.teleostnacl.szu.scheme.model.SchemeDetailLesson;

import java.util.List;

public class SchemeDetailLessonJson {
    @SerializedName("datas")
    @Expose
    public Datas datas;

    public static class Datas {
        @SerializedName("kzkccx")
        @Expose
        public Kzkccx kzkccx;

        public static class Kzkccx {
            @SerializedName("rows")
            @Expose
            public List<SchemeDetailLesson> rows;

        }
    }


}
