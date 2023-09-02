package com.teleostnacl.szu.documentation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryFileModelJson {
    @SerializedName("data")
    @Expose
    public Data data;

    public static class Data {

        @SerializedName("orders")
        @Expose
        public List<HistoryFileModel> orders = null;

    }
}
