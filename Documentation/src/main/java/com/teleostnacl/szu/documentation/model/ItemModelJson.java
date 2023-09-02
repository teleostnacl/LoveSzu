package com.teleostnacl.szu.documentation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ItemModelJson {
    @SerializedName("data")
    @Expose
    public Data data;

    public static class Data {
        @SerializedName("records")
        @Expose
        public List<ItemModel> records = null;
    }
}
