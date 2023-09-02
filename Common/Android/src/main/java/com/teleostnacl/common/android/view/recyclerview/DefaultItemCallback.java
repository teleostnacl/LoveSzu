package com.teleostnacl.common.android.view.recyclerview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.teleostnacl.common.android.log.Logger;

import java.util.Objects;

public class DefaultItemCallback<T> extends DiffUtil.ItemCallback<T> {

    private final String TAG = "DefaultItemCallback[" + hashCode() + "]";

    @Override
    public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        boolean isSame = oldItem == newItem;

        log("areItemsTheSame:\n" + " oldItem = " + oldItem);
        log("areItemsTheSame:\n" + " newItem = " + newItem);
        log("areItemsTheSame:" + isSame);

        return isSame;
    }

    @Override
    public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        boolean isSame = Objects.equals(oldItem, newItem);

        log("areContentsTheSame:\n" + " oldItem = " + oldItem);
        log("areContentsTheSame:\n" + " newItem = " + newItem);
        log("areContentsTheSame:" + isSame);

        return isSame;
    }

    public boolean showLog() {
        return false;
    }

    private void log(String s) {
        if (showLog()) Logger.v(TAG, s);
    }
}
