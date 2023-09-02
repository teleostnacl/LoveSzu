package com.teleostnacl.common.android.view.spinner;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

/**
 * 重复选择某一项会触发onItemSelected
 */
public class RefreshSpinner extends AppCompatSpinner {

    public RefreshSpinner(@NonNull Context context) {
        super(context);
    }

    public RefreshSpinner(@NonNull Context context, int mode) {
        super(context, mode);
    }

    public RefreshSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RefreshSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public RefreshSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
    }

    @Override
    public void setSelection(int position, boolean animate) {
        if(getAdapter() != null && position < getAdapter().getCount()) {
            //判断是否重复选择
            boolean sameSelected = position == getSelectedItemPosition();
            super.setSelection(position, animate);
            if (sameSelected) {
                // 若是选择项是Spinner当前已选择的项,则 OnItemSelectedListener并不会触发,所以这里手动触发回调
                getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }
    }

    @Override
    public void setSelection(int position) {
        if(getAdapter() != null && position < getAdapter().getCount()) {
            //判断是否重复选择
            boolean sameSelected = position == getSelectedItemPosition();
            super.setSelection(position);
            if (sameSelected) {
                // 若是选择项是Spinner当前已选择的项,则 OnItemSelectedListener并不会触发,所以这里手动触发回调
                getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }
    }
}
