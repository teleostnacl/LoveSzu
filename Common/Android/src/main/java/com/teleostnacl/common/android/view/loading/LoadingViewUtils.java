package com.teleostnacl.common.android.view.loading;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;

public class LoadingViewUtils {

    // 动画视图的边长
    public final static int ANIMATION_VIEW_LENGTH = ResourcesUtils.getDensityPx(40);
    // 文字距动画的距离
    public final static int TEXT_VIEW_PADDING = ResourcesUtils.getDensityPx(15);

    // 文字的大小
    public final static float TEXT_SIZE = 20;

    // 动画使用的插值器 用于计算动画的进度
    public static final Interpolator interpolator = new AccelerateDecelerateInterpolator();

    // 定义圈的数量
    public static final int circleNum = 5;
    // 定义加载动画中圆圈的大小
    public static final float circleRadius = 3.5f * ResourcesUtils.getDensity();
    // 动画速率系数
    public static final float animationRate = 2;
    // 最后一个圆距第一个圆到终点的时间系数
    public static final float lastTimeDelta = 0.5f;

    // 帧率
    public static final float rate = ResourcesUtils.getRefreshRate();
    // 频率
    public static final float frequency = 1 / rate;

    public static int[] getColors() {
        return ColorResourcesUtils.getDeepColors();
    }
}
