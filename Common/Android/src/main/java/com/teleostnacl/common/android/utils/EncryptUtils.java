package com.teleostnacl.common.android.utils;

import androidx.annotation.NonNull;

public class EncryptUtils {

    @NonNull
    public static String encrypt(Object object) {
        return encrypt(String.valueOf(object));
    }

    /**
     * 加密
     */
    @NonNull
    public static String encrypt(String origin) {
        return origin;
    }

    /**
     * 解密
     */
    @NonNull
    public static String decrypt(String encrypt) {
        return encrypt;
    }
}
