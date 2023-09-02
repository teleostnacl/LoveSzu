package com.teleostnacl.common.android.view.edittext;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 当使用BaseActivity时 点击EditText外部时清除EditText的焦点
 * 部分EditText与其他View具有联动性, 当触摸这些View时不应清除其焦点
 * 故继承AppCompatEditText并定义变量List存储具有联动性的View
 * 当点击这些view时 不清楚其焦点
 */
public class RelationViewEditText extends androidx.appcompat.widget.AppCompatEditText implements IRelationView {

    private final List<View> relationViews = new ArrayList<>();

    public RelationViewEditText(@NonNull Context context) {
        super(context);
    }

    public RelationViewEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RelationViewEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置并更新与EditText有关的View
     *
     * @param views 与EditText有关的View
     */
    public void setRelationViews(View... views) {
        relationViews.clear();
        relationViews.addAll(Arrays.asList(views));
    }

    /**
     * 判断所触摸位置的坐标是否在关联的View中
     *
     * @param rawX 触摸位置的X坐标
     * @param rawY 触摸位置的Y坐标
     * @return 触摸位置的坐标是否在关联的View中
     */
    @Override
    public boolean isInRelation(int rawX, int rawY) {
        for (View v : relationViews) {
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            if (outRect.contains(rawX, rawY)) {
                return true;
            }
        }
        return false;
    }
}
