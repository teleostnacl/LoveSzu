package com.teleostnacl.szu.examination.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class XNXQModelJson {
    @SerializedName("datas")
    @Expose
    public Datas datas;

    public static class Datas {
        @SerializedName("xnxqcx")
        @Expose
        public Xnxqcx xnxqcx;

        public static class Xnxqcx {
            @SerializedName("rows")
            @Expose
            public List<XNXQModel> rows = null;
        }
    }
}
