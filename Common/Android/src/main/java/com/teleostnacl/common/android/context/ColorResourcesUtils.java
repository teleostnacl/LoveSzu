package com.teleostnacl.common.android.context;

import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.teleostnacl.common.android.R;

/**
 * 与颜色相关的资源工具类
 */
public class ColorResourcesUtils extends ResourcesUtils {

    private static int[] lightColors;
    private static int[] deepColors;

    private static int colorSum;

    public static int[] getLightColors() {
        if (lightColors == null) {
            lightColors = getColorArray(R.array.light_colors_list);
        }

        return lightColors;
    }

    public static int[] getDeepColors() {
        if (deepColors == null) {
            deepColors = getColorArray(R.array.deep_colors_list);
        }

        return deepColors;
    }


    public static int getColorSum() {
        if (colorSum == 0) {
            colorSum = getInteger(R.integer.colors_sum);
        }
        return colorSum;
    }

    @NonNull
    public static int[] getColorArray(@ArrayRes int id) {
        TypedArray colorList = getResources().obtainTypedArray(id);
        int[] colors = new int[colorList.length()];
        for (int i = 0; i < colorList.length(); i++) {
            colors[i] = colorList.getColor(i, 0);
        }
        colorList.recycle();

        return colors;
    }

    @ColorInt
    public static int getColor(@ColorRes int id) {
        return getContext().getColor(id);
    }

    /**
     * 根据颜色资源的id创建ColorDrawable
     *
     * @param id 颜色资源的id
     * @return 颜色资源id所对应的颜色创建的ColorDrawable
     */
    @NonNull
    public static ColorDrawable getColorDrawable(@ColorRes int id) {
        return new ColorDrawable(getColor(id));
    }
}
