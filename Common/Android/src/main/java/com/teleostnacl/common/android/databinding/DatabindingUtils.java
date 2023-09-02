package com.teleostnacl.common.android.databinding;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

public final class DatabindingUtils {
    /**
     * 如果提供的字符串为空, 则不显示视图
     */
    @BindingAdapter("visibility_by_string")
    public static void setVisibilityByString(@NonNull View view, String s) {
        view.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
    }
}
