package com.teleostnacl.common.android.model;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.teleostnacl.common.android.context.ResourcesUtils;

import java.util.Objects;

/**
 * key - value 基础Model
 */
public class KeyValueModel {
    // 标题
    public final String key;
    // 值
    public final String value;

    public KeyValueModel(@StringRes int keyId, String value) {
        this.key = ResourcesUtils.getString(keyId);
        this.value = value;
    }

    public KeyValueModel(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyValueModel that = (KeyValueModel) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}