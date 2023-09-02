package com.teleostnacl.common.android.view.cardview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.teleostnacl.common.android.context.ResourcesUtils;


/**
 * 胶囊视图
 */
public class CapsuleShapeView extends CardView {
    public CapsuleShapeView(@NonNull Context context) {
        this(context, null);
    }

    public CapsuleShapeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CapsuleShapeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        float radius = (float) Math.min(width, height) / 2;
        setRadius(radius);
        super.onLayout(changed, left, top, right, bottom);
    }

    private void init() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme()
                .resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        TypedArray typedArray = getContext()
                .obtainStyledAttributes(typedValue.resourceId,
                        new int[]{android.R.attr.selectableItemBackground});
        Drawable foreground = typedArray.getDrawable(0);
        typedArray.recycle();

        setLayoutParams(new MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setClickable(true);

        setForeground(foreground);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if(params instanceof MarginLayoutParams) {
            //margin必须大于4
            final int margin = ResourcesUtils.dp_int_4;
            MarginLayoutParams layoutParams = (MarginLayoutParams) params;
            if(layoutParams.bottomMargin < margin) layoutParams.bottomMargin = margin;
            if(layoutParams.topMargin < margin) layoutParams.topMargin = margin;
            if(layoutParams.leftMargin < margin) layoutParams.leftMargin = margin;
            if(layoutParams.rightMargin < margin) layoutParams.rightMargin = margin;
            super.setLayoutParams(params);
        }
    }

    public void setMargins(int left, int top, int right, int bottom) {
        MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
        layoutParams.setMargins(left, top, right, bottom);
        setLayoutParams(layoutParams);
    }
}
