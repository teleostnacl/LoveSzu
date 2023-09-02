package com.teleostnacl.common.android.databinding;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import soup.neumorphism.NeumorphCardView;

public class NeumorphAdapter {
    @BindingAdapter("neumorph_shapeType")
    public static void setShapeType(@NonNull NeumorphCardView view, int shapeType) {
        view.setShapeType(shapeType);
    }
}
