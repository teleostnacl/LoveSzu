package com.teleostnacl.common.android.utils;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.log.Logger;

import java.net.URLEncoder;

public class UrlUtils {
    public static String encode(@NonNull String s, @NonNull String enc) {
        try {
            return URLEncoder.encode(s, enc);
        } catch (Exception e) {
            Logger.e(e);
            return "";
        }
    }
}
