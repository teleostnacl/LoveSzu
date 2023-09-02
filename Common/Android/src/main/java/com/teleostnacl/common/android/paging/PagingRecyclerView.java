package com.teleostnacl.common.android.paging;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.paging.LoadState;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.teleostnacl.common.android.paging.adapter.CustomLoadStateAdapter;
import com.teleostnacl.common.android.view.loading.LoadingView;
import com.teleostnacl.common.android.R;

import kotlin.Unit;

/**
 * 分页加载的RecyclerView, 实现统一管理加载动画 加载失败暂无数据的view
 */
public class PagingRecyclerView extends FrameLayout {

    // 数据为空时的View
    private final View noneView;
    // 数据为空时的TextView
    private final TextView noneTextView;
    // 加载失败时的View
    private final View failView;
    // 加载失败时的TextView
    private final TextView failTextView;
    // 展示数据的RecyclerView
    private final RecyclerView recyclerView;
    // 刷新数据用的SwipeRefreshLayout
    private final SwipeRefreshLayout swipeRefreshLayout;
    // 记录是否可刷新
    private boolean swipeRefreshEnable = true;
    // 加载动画的view
    private final LoadingView loadingView;

    private PagingDataAdapter<?, ?> adapter;

    public PagingRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public PagingRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagingRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PagingRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.layout_paging_recycler_view, this);

        noneView = findViewById(R.id.none_view);
        noneTextView = findViewById(R.id.none_text);
        failView = findViewById(R.id.fail);
        failTextView = findViewById(R.id.fail_text);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        loadingView = findViewById(R.id.loading);

        // TODO 暂时禁用recyclerview的动画
        recyclerView.setItemAnimator(null);
    }

    public PagingRecyclerView setNoneText(String noneText) {
        noneTextView.setText(noneText);
        return this;
    }

    public PagingRecyclerView setNoneText(@StringRes int noneTextId) {
        noneTextView.setText(noneTextId);
        return this;
    }

    public PagingRecyclerView setNoneText(@StringRes int noneTextId, Object... formatArgs) {
        return setNoneText(getContext().getString(noneTextId, formatArgs));
    }

    public PagingRecyclerView setFailText(String failText) {
        failTextView.setText(failText);
        return this;
    }

    public PagingRecyclerView setFailText(@StringRes int failTextId) {
        return setFailText(failTextId, (Object) null);
    }

    public PagingRecyclerView setFailText(@StringRes int failTextId, Object... formatArgs) {
        return setFailText(getContext().getString(failTextId, formatArgs));
    }

    public PagingRecyclerView setAdapter(@NonNull PagingDataAdapter<?, ?> pagingDataAdapter) {
        adapter = pagingDataAdapter;

        // 添加初始加载和加载失败的展示逻辑
        pagingDataAdapter.addLoadStateListener(combinedLoadStates -> {
            loadingView.setVisibility(View.GONE);
            failView.setVisibility(View.GONE);
            noneView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(false);

            LoadState loadState = combinedLoadStates.getRefresh();
            if (loadState instanceof LoadState.Loading) {
                loadingView.setVisibility(View.VISIBLE);
            } else if (loadState instanceof LoadState.NotLoading) {
                recyclerView.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setEnabled(swipeRefreshEnable);
                swipeRefreshLayout.setRefreshing(false);
                if (pagingDataAdapter.getItemCount() == 0) {
                    // 是CheckShowNonePagingDataAdapter类, 且需要显示为空
                    if (!(pagingDataAdapter instanceof ICheckShowNone) ||
                            ((ICheckShowNone) pagingDataAdapter).isShowNone()) {
                        noneView.setVisibility(View.VISIBLE);
                    }
                }
            } else if (loadState instanceof LoadState.Error) {
                failView.setVisibility(View.VISIBLE);
            }

            return Unit.INSTANCE;
        });

        // 设置错误视图单击重试
        failView.setOnClickListener(v -> pagingDataAdapter.retry());

        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(pagingDataAdapter::refresh);

        recyclerView.setAdapter(pagingDataAdapter.withLoadStateFooter(
                new CustomLoadStateAdapter(pagingDataAdapter::retry, failTextView.getText().toString())));

        return this;
    }

    public PagingRecyclerView setSwipeRefreshEnabled(boolean enabled) {
        swipeRefreshEnable = enabled;
        swipeRefreshLayout.setEnabled(enabled);
        return this;
    }

    /**
     * 回到顶部
     */
    public void scrollToTop() {
        getRecyclerView().scrollToPosition(0);
    }

    public TextView getNoneView() {
        return noneTextView;
    }

    public TextView getFailView() {
        return failTextView;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public LoadingView getLoadingView() {
        return loadingView;
    }

    public PagingDataAdapter<?, ?> getAdapter() {
        return adapter;
    }

    public static abstract class CheckShowNonePagingDataAdapter<T, VH extends RecyclerView.ViewHolder> extends PagingDataAdapter<T, VH> implements ICheckShowNone {

        public CheckShowNonePagingDataAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
            super(diffCallback);
        }
    }

    public interface ICheckShowNone {
        /**
         * @return 是否显示数据为空的视图
         */
        boolean isShowNone();
    }
}
