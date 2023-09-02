package com.teleostnacl.common.android.databinding;

import android.text.SpannableString;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;

public class TextViewAdapter {
    @BindingAdapter("android:text")
    public static void setText(@NonNull TextView view, SpannableString string) {
        view.setText(string);
    }

    @NonNull
    @InverseBindingAdapter(attribute = "android:text", event = "android:textAttrChanged")
    public static SpannableString getText(@NonNull TextView view) {
        CharSequence charSequence = view.getText();

        if (charSequence instanceof SpannableString) {
            return (SpannableString) charSequence;
        }

        return new SpannableString(charSequence);
    }
}
