package com.teleostnacl.common.android.databinding;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

public class ImageAdapter {
    @BindingAdapter("android:src")
    public static void setSrc(@NonNull ImageView view, int id) {view.setImageResource(id);}

    @BindingAdapter("android:src")
    public static void setSrc(@NonNull ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }

    @BindingAdapter("srcCompat")
    public static void setSrcCompat(@NonNull AppCompatImageView view, int id) {view.setImageResource(id);}

    @BindingAdapter("srcCompat")
    public static void setSrcCompat(@NonNull AppCompatImageView view, Bitmap bitmap) {view.setImageBitmap(bitmap);}

    @BindingAdapter("srcCompat")
    public static void setSrcCompat(@NonNull AppCompatImageView view, Drawable drawable) {
        view.setImageDrawable(drawable);
    }
}
