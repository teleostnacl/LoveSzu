package com.teleostnacl.szu.library.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.library.R;

/**
 * 图书清单所使用的Checkbox
 */
public class LibraryListFragmentCheckBox extends AppCompatCheckBox {
    public LibraryListFragmentCheckBox(@NonNull Context context) {
        this(context, null);
    }

    public LibraryListFragmentCheckBox(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(new ContextThemeWrapper(context, R.style.library_list_fragment_CheckBox), attrs, androidx.appcompat.R.attr.checkboxStyle);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public LibraryListFragmentCheckBox(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setButtonDrawable(context.getDrawable(R.drawable.background_library_list_fragment_check_box));

        //如果有文字,则设置在左边8dp的padding
        if (getText() != null) setPadding(Math.max(getPaddingLeft(), ResourcesUtils.dp_int_8),
                getPaddingTop(), getPaddingRight(), getPaddingBottom());

        setGravity(Gravity.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int min = Math.min(width, height);

        //如果最小尺寸为0,则使用另一个尺寸
        if (min == 0) {
            //另一个尺寸为0,则使用默认尺寸
            if ((min = Math.max(width, height)) == 0) {
                min = getContext().getResources().getDimensionPixelSize(R.dimen.library_list_fragment_check_box_size);
            }
        }
        //将Background调整为方形,取较小的边作为边长
        Drawable drawable = getButtonDrawable();
        drawable.setBounds(0, 0, min, min);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        setPadding(Math.max(getPaddingLeft(), ResourcesUtils.dp_int_8),
                getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }
}
