package com.teleostnacl.common.android.view.recyclerview;

import android.os.SystemClock;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ClickedOnceListAdapter<T, VH extends RecyclerView.ViewHolder> extends ListAdapter<T, VH> {

    protected boolean clicked = false;

    protected ClickedOnceListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    protected ClickedOnceListAdapter(@NonNull AsyncDifferConfig<T> config) {
        super(config);
    }

    @Override
    @CallSuper
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        clicked = false;
    }

    private void setClicked() {
        clicked = true;
        new Thread(() -> {
            SystemClock.sleep(getClickGap());
            clicked = false;
        }).start();
    }

    /**
     * 注册点击监听器
     *
     * @param view View
     */
    public void setOnClickListener(@NonNull View view) {
        view.setOnClickListener(v -> {
            // 已经点击了 则不响应
            if (clicked) {
                return;
            }

            // 设为已经点击状态
            setClicked();

            // 响应事件
            onClick(v);
        });
    }

    /**
     * @return 获取两次点击的间隔时间
     */
    protected int getClickGap() {
        return 100;
    }

    /**
     * 点击响应事件
     */
    public abstract void onClick(@NonNull View v);
}
