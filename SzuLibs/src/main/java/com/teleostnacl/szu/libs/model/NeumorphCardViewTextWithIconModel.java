package com.teleostnacl.szu.libs.model;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.teleostnacl.szu.libs.BR;

/**
 * 带icon的拟态风格卡片所使用的Model
 */
public class NeumorphCardViewTextWithIconModel extends BaseObservable {
    public String title;
    @NonNull
    public final Drawable icon;
    @NonNull
    public final Runnable runnable;

    public NeumorphCardViewTextWithIconModel(String title, @NonNull Drawable icon, @NonNull Runnable runnable) {
        this.title = title;
        this.icon = icon;
        this.runnable = runnable;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }
}
