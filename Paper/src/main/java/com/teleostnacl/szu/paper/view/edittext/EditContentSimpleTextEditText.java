package com.teleostnacl.szu.paper.view.edittext;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.teleostnacl.common.android.view.edittext.IRelationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditContentSimpleTextEditText extends TextInputEditText implements IRelationView {

    private final List<View> relationViews = new ArrayList<>();

    private OnKeyListener mOnKeyListener;

    public EditContentSimpleTextEditText(@NonNull Context context) {
        super(context);
    }

    public EditContentSimpleTextEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EditContentSimpleTextEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnKeyListener(OnKeyListener onKeyListener) {
        this.mOnKeyListener = onKeyListener;
    }

    @Nullable
    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        return new InputConnectionWrapper(super.onCreateInputConnection(outAttrs), true) {
            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                // 消耗了事件
                if (mOnKeyListener != null && mOnKeyListener.onKey(EditContentSimpleTextEditText.this, event.getKeyCode(), event)) {
                    return true;
                }
                return super.sendKeyEvent(event);
            }
        };
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
