package com.teleostnacl.common.android.context;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.common.java.util.NumberUtils;

import java.util.Objects;

/**
 * SharedPreference工具类, 用于写入时加密
 */
public final class SPUtils extends ContextUtils {

    /**
     * 获取SharedPreference
     *
     * @param name 文件名
     * @return SharedPreference
     */
    public static SharedPreferences getSP(String name) {
        return getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    @Nullable
    public static String getString(@NonNull SharedPreferences sharedPreferences, String key, @Nullable String defValue) {
        try {
            String tmp = sharedPreferences.getString(EncryptUtils.encrypt(key), null);
            // 所得结果不为空时, 才进行解密
            if (tmp != null) {
                return EncryptUtils.decrypt(tmp);
            }
        } catch (Exception e) {
            Logger.e(e);
        }

        // 返回默认值
        return defValue;
    }

    public static int getInt(@NonNull SharedPreferences SP, String key, int defValue) {
        try {
            return NumberUtils.parseInt(Objects.requireNonNull(getString(SP, key, String.valueOf(defValue))), 0);
        } catch (Exception e) {
            Logger.e(e);
            return defValue;
        }
    }

    public static long getLong(@NonNull SharedPreferences SP, String key, long defValue) {
        try {
            return NumberUtils.parseLong(Objects.requireNonNull(getString(SP, key, String.valueOf(defValue))), 0);
        } catch (Exception e) {
            Logger.e(e);
            return defValue;
        }
    }

    public static float getFloat(@NonNull SharedPreferences SP, String key, float defValue) {
        try {
            return NumberUtils.parseFloat(Objects.requireNonNull(getString(SP, key, String.valueOf(defValue))), 0);
        } catch (Exception e) {
            Logger.e(e);
            return defValue;
        }
    }

    public static boolean getBoolean(@NonNull SharedPreferences SP, String key, boolean defValue) {
        try {
            return Boolean.parseBoolean(Objects.requireNonNull(getString(SP, key, String.valueOf(defValue))));
        } catch (Exception e) {
            return defValue;
        }
    }

    public static SharedPreferences.Editor putString(@NonNull SharedPreferences.Editor editor, String key, @Nullable String value) {
        return editor.putString(EncryptUtils.encrypt(key), EncryptUtils.encrypt(value));
    }

    public static SharedPreferences.Editor putInt(@NonNull SharedPreferences.Editor editor, String key, int value) {
        return putString(editor, key, String.valueOf(value));
    }

    public static SharedPreferences.Editor putLong(@NonNull SharedPreferences.Editor editor, String key, long value) {
        return putString(editor, key, String.valueOf(value));
    }

    public static SharedPreferences.Editor putFloat(@NonNull SharedPreferences.Editor editor, String key, float value) {
        return putString(editor, key, String.valueOf(value));
    }

    public static SharedPreferences.Editor putBoolean(@NonNull SharedPreferences.Editor editor, String key, boolean value) {
        return putString(editor, key, String.valueOf(value));
    }

    public static SharedPreferences.Editor remove(@NonNull SharedPreferences.Editor editor, String key) {
        return editor.remove(EncryptUtils.encrypt(key));
    }
}
