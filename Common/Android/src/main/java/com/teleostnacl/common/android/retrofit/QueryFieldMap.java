package com.teleostnacl.common.android.retrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.teleostnacl.common.java.util.StringUtils;

import java.util.LinkedHashMap;

public class QueryFieldMap extends LinkedHashMap<String, String> {
    @Nullable
    @Override
    public String put(@NonNull String key, @Nullable String value) {
        // 对value进行处理, 防止null
        return super.put(key, StringUtils.getOrBlank(value));
    }
}
