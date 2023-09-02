package com.teleostnacl.common.android.paging.adapter;

import android.os.SystemClock;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import kotlinx.coroutines.CoroutineDispatcher;

public abstract class ClickOncePagingDataAdapter<T, VH extends RecyclerView.ViewHolder> extends PagingDataAdapter<T, VH> {

    protected boolean clicked = false;

    public ClickOncePagingDataAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, @NonNull CoroutineDispatcher mainDispatcher) {
        super(diffCallback, mainDispatcher);
    }

    public ClickOncePagingDataAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public ClickOncePagingDataAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, @NonNull CoroutineDispatcher mainDispatcher, @NonNull CoroutineDispatcher workerDispatcher) {
        super(diffCallback, mainDispatcher, workerDispatcher);
    }

    @Override
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
