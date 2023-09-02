package com.teleostnacl.szu.grade.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 从服务器获取成绩得到的json
 */
public class GradeModelJson {
    @SerializedName("datas")
    @Expose
    public Datas datas;


    public static class Datas {
        @SerializedName("xscjcxtjgl")
        @Expose
        public Xscjcxtjgl xscjcxtjgl;
    }

    public static class Xscjcxtjgl {

        @SerializedName("rows")
        @Expose
        public List<GradeModel> rows = null;
        @SerializedName("extParams")
        @Expose
        public ExtParams extParams;

        public static class ExtParams {
            // 查询结果(期望值: 查询成功)
            @SerializedName("msg")
            @Expose
            public String msg;
        }

    }
}
