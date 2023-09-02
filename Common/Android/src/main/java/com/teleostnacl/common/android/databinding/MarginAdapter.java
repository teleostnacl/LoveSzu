package com.teleostnacl.common.android.databinding;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

public class MarginAdapter {

    @BindingAdapter("android:layout_margin")
    public static void setMargin(@NonNull View view, int margin) {
        ViewGroup.LayoutParams layoutParams =  view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin =
                    ((ViewGroup.MarginLayoutParams) layoutParams).topMargin =
                            ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin =
                                    ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = margin;
        }
    }

    @BindingAdapter("android:layout_marginVertical")
    public static void setMarginVertical(@NonNull View view, int margin) {
        ViewGroup.LayoutParams layoutParams =  view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).topMargin =
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = margin;
        }
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setMarginTop(@NonNull View view, int margin) {
        ViewGroup.LayoutParams layoutParams =  view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).topMargin =margin;
        }
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setMarginBottom(@NonNull View view, int margin) {
        ViewGroup.LayoutParams layoutParams =  view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = margin;
        }
    }

    @BindingAdapter("android:layout_marginHorizontal")
    public static void setMarginHorizontal(@NonNull View view, int margin) {
        ViewGroup.LayoutParams layoutParams =  view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin =
                    ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = margin;
        }
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setMarginRight(@NonNull View view, int margin) {
        ViewGroup.LayoutParams layoutParams =  view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = margin;
        }
    }

    @BindingAdapter("android:layout_marginLeft")
    public static void setMarginLeft(@NonNull View view, int margin) {
        ViewGroup.LayoutParams layoutParams =  view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin = margin;
        }
    }

    @BindingAdapter("android:layout_marginStart")
    public static void setMarginStart(@NonNull View view, int margin) {
        ViewGroup.LayoutParams layoutParams =  view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).setMarginStart(margin);
        }
    }

    @BindingAdapter("android:layout_marginEnd")
    public static void setMarginEnd(@NonNull View view, int margin) {
        ViewGroup.LayoutParams layoutParams =  view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(margin);
        }
    }



    @BindingAdapter("android:layout_margin")
    public static void setMargin(@NonNull View view, float margin) {
        setMargin(view, (int) margin);
    }

    @BindingAdapter("android:layout_marginVertical")
    public static void setMarginVertical(@NonNull View view, float margin) {
        setMarginVertical(view, (int) margin);
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setMarginTop(@NonNull View view, float margin) {
        setMarginTop(view, (int) margin);
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setMarginBottom(@NonNull View view, float margin) {
        setMarginBottom(view, (int) margin);
    }

    @BindingAdapter("android:layout_marginHorizontal")
    public static void setMarginHorizontal(@NonNull View view, float margin) {
        setMarginHorizontal(view, (int) margin);
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setMarginRight(@NonNull View view, float margin) {
        setMarginRight(view, (int) margin);
    }

    @BindingAdapter("android:layout_marginLeft")
    public static void setMarginLeft(@NonNull View view, float margin) {
        setMarginLeft(view, (int) margin);
    }

    @BindingAdapter("android:layout_marginStart")
    public static void setMarginStart(@NonNull View view, float margin) {
        setMarginStart(view, (int) margin);
    }

    @BindingAdapter("android:layout_marginEnd")
    public static void setMarginEnd(@NonNull View view, float margin) {
        setMarginEnd(view, (int) margin);
    }
}
