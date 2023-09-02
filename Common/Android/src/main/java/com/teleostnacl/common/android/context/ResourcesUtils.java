package com.teleostnacl.common.android.context;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.ArrayRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.Objects;

@SuppressLint("UseCompatLoadingForDrawables")
public class ResourcesUtils extends ContextUtils {
    //region 常用的尺寸(4dp 8dp 12dp 16dp)
    public static final float dp_float_16 = 16 * getDensity();
    public static final int dp_int_16 = (int) dp_float_16;
    public static final float dp_float_12 = 12 * getDensity();
    public static final int dp_int_12 = (int) dp_float_12;
    public static final float dp_float_8 = 8 * getDensity();
    public static final int dp_int_8 = (int) dp_float_8;
    public static final float dp_float_4 = 4 * getDensity();
    public static final int dp_int_4 = (int) dp_float_4;
    //endregion

    // 屏幕刷新率
    private static float refreshRate;

    public static Resources getResources() {
        return getContext().getResources();
    }

    @Nullable
    public static Drawable getDrawable(@DrawableRes int id) {
        return getContext().getDrawable(id);
    }

    @NonNull
    public static CharSequence getText(@StringRes int resId) {
        return getContext().getText(resId);
    }

    @NonNull
    public static String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }

    @NonNull
    public static String getString(@StringRes int resId, Object... formatArgs) {
        return getContext().getString(resId, formatArgs);
    }

    @NonNull
    public static String[] getStringArray(@ArrayRes int id) {
        return getResources().getStringArray(id);
    }

    public static int getInteger(@IntegerRes int id) {
        return getResources().getInteger(id);
    }

    public static float getDimension(@DimenRes int id) {
        return getResources().getDimension(id);
    }

    /**
     * @return 1dp对应的px大小
     */
    public static float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    /**
     * @return 1dp对应的px大小
     */
    public static int getDensityPx() {
        return (int) (getResources().getDisplayMetrics().density + 0.5);
    }

    /**
     * @return 指定dp对应的px大小
     */
    public static float getDensity(float dp) {
        return dp * getDensity();
    }

    /**
     * @return 指定dp对应的px大小
     */
    public static int getDensityPx(float dp) {
        return (int) (dp * getDensity() + 0.5);
    }

    /**
     * @return 设备高的像素值
     */
    public static float getHeightPixels() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * @return 设备宽的像素值
     */
    public static float getWidthPixels() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * @return 点击水波纹资源selectableItemBackground
     */
    @NonNull
    public static Drawable getSelectableItemBackground() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);

        return Objects.requireNonNull(getDrawable(typedValue.resourceId));
    }

    /**
     * @return 点击水波纹资源selectableItemBackground
     */
    @NonNull
    public static Drawable getSelectableItemBackgroundBorderless() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true);

        return Objects.requireNonNull(getDrawable(typedValue.resourceId));

    }

    /**
     * @return 屏幕刷新率
     */
    public static float getRefreshRate() {
        if (refreshRate == 0) {
            Display display = ((WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            refreshRate = display.getRefreshRate();
        }

        return refreshRate;
    }

    /**
     * @return 获取指定资源的id
     */
    @SuppressLint("DiscouragedApi")
    public static int getIdentifier(String name, String defType, String defPackage) {
        return getResources().getIdentifier(name, defType, defPackage);
    }
}
