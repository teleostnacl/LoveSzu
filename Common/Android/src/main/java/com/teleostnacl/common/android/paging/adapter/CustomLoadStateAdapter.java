package com.teleostnacl.common.android.paging.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.teleostnacl.common.android.view.loading.LoadingView;
import com.teleostnacl.common.android.R;

public class CustomLoadStateAdapter extends LoadStateAdapter<CustomLoadStateAdapter.LoadStateViewHolder> {

    // 重试的操作
    private final Runnable retryRunnable;

    private final String errorMsg;

    public CustomLoadStateAdapter(Runnable retryRunnable) {
        this.retryRunnable = retryRunnable;
        errorMsg = null;
    }

    public CustomLoadStateAdapter(Runnable retryRunnable, String errorMsg) {
        this.retryRunnable = retryRunnable;
        this.errorMsg = errorMsg;
    }

    @NonNull
    @Override
    public LoadStateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, @NonNull LoadState loadState) {
        LoadStateViewHolder loadStateViewHolder = new LoadStateViewHolder(viewGroup, retryRunnable);

        if (errorMsg != null) {
            loadStateViewHolder.getError().setText(errorMsg);
        }

        return loadStateViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LoadStateViewHolder viewHolder, @NonNull LoadState loadState) {
        viewHolder.bind(loadState);
    }

    public static class LoadStateViewHolder extends RecyclerView.ViewHolder {
        private final LoadingView loadingView;
        private final View error;
        private final TextView errorMsg;

        public LoadStateViewHolder(@NonNull ViewGroup parent, Runnable retryRunnable) {
            super(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_item_paging_recycler_view_footer, parent, false));

            loadingView = itemView.findViewById(R.id.loading);
            error = itemView.findViewById(R.id.fail);
            errorMsg = itemView.findViewById(R.id.fail_text);

            error.setOnClickListener(v -> retryRunnable.run());
        }

        public void bind(LoadState loadState) {
            if (loadState instanceof LoadState.Error) {
                error.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
            } else if (loadState instanceof LoadState.Loading) {
                error.setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);
            }
        }

        public LoadingView getLoadingView() {
            return loadingView;
        }

        public TextView getError() {
            return errorMsg;
        }
    }
}
