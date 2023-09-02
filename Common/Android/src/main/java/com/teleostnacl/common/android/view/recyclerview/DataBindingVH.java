package com.teleostnacl.common.android.view.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public class DataBindingVH<T extends  ViewDataBinding> extends RecyclerView.ViewHolder {

    public final T binding;

    public DataBindingVH(@NonNull T binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public DataBindingVH(Context context, @LayoutRes int id, ViewGroup parent, boolean attachToPosition) {
        this(DataBindingUtil.inflate(LayoutInflater.from(context), id, parent, attachToPosition));
    }

    public DataBindingVH(@NonNull View view) {
        super(view);
        binding = null;
    }

    public DataBindingVH(ViewGroup parent, @LayoutRes int id) {
        this(parent.getContext(), id, parent, false);
    }
}
