package com.teleostnacl.common.android.utils;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT;

import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.text.HtmlCompat;

import com.teleostnacl.common.android.context.ResourcesUtils;

public class HtmlUtils {
    @NonNull
    public static Spanned fromHtml(@NonNull String s) {
        return HtmlCompat.fromHtml(s, FROM_HTML_MODE_COMPACT);
    }

    @NonNull
    public static Spanned fromHtml(@StringRes int stringId) {
        return fromHtml(ResourcesUtils.getString(stringId));
    }

    @NonNull
    public static Spanned fromHtml(@StringRes int resId, Object... formatArgs) {
        return fromHtml(ResourcesUtils.getString(resId, formatArgs));
    }
}
