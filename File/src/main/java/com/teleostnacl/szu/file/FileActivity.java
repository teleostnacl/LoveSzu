package com.teleostnacl.szu.file;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.utils.HtmlUtils;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.szu.file.databinding.ActivityFileBinding;
import com.teleostnacl.szu.file.viewmodel.FileViewModel;
import com.teleostnacl.szu.libs.activity.BaseLoadingActivity;
import com.teleostnacl.szu.libs.databinding.LayoutItemNeumorphCardViewTextWithIconBinding;
import com.teleostnacl.szu.libs.model.NeumorphCardViewTextWithIconModel;
import com.teleostnacl.szu.libs.view.recyclerview.adapter.NeumorphCardViewTextViewIconListAdapter;

import java.util.List;
import java.util.Objects;

public class FileActivity extends BaseLoadingActivity {

    private ActivityFileBinding binding;

    private FileViewModel fileViewModel;

    private final Adapter adapter = new Adapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_file);

        fileViewModel = new ViewModelProvider(this).get(FileViewModel.class);

        showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color, R.string.file_download);

        binding.getRoot().post(() -> disposable.add(fileViewModel.getFileModels().subscribe(fileModels -> {
            if (fileModels.size() == 0) {
                ToastUtils.makeToast(com.teleostnacl.common.android.R.string.unknown_error);
                finish();
            } else {
                binding.recyclerView.setAdapter(adapter);

                adapter.submitList(fileModels);

                binding.getRoot().setVisibility(View.VISIBLE);
                binding.getRoot().post(this::hideLoadingView);
            }
        })));
    }

    private class Adapter extends NeumorphCardViewTextViewIconListAdapter {
        private static final int TIP_VIEW_TYPE = 0;

        private static final int SEARCH_VIEW_TYPE = 1;

        private static final int MODEL_VIEW_TYPE = -1;

        private final int padding = ResourcesUtils.getDensityPx(8);

        private String searchKey = "";

        // 提示语的占位Model
        private final Model TIP_MODEL = new Model();
        // 搜索占位的Model
        private final Model SEARCH_MODEL = new Model();


        @NonNull
        @Override
        public DataBindingVH<LayoutItemNeumorphCardViewTextWithIconBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            switch (viewType) {
                case TIP_VIEW_TYPE: {
                    TextView textView = new TextView(FileActivity.this);

                    textView.setTextColor(ColorResourcesUtils.getColor(com.teleostnacl.szu.libs.R.color.text_color_common));
                    textView.setLayoutParams(new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setTextSize(16);

                    textView.setPadding(3 * padding, 0, 3 * padding, padding);

                    return new DataBindingVH<>(textView);
                }
                case SEARCH_VIEW_TYPE: {
                    // 初始化SearchView
                    SearchView searchView = new SearchView(FileActivity.this);
                    searchView.setLayoutParams(new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    searchView.setPadding(3 * padding, 0, 3 * padding, padding);

                    searchView.setIconified(false);

                    // 覆盖默认清除行为
                    searchView.findViewById(ResourcesUtils.getIdentifier("search_close_btn", "id", "android"))
                            .setOnClickListener(v -> {
                                CharSequence text = searchView.getQuery();
                                if (TextUtils.isEmpty(text)) {
                                    searchView.clearFocus();
                                } else {
                                    searchView.setQuery("", false);

                                    search("");
                                }
                            });

                    searchView.setQueryHint(getString(R.string.file_download_search_tip));

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            search(query);
                            binding.recyclerView.clearFocus();

                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            // 当文字为空时 更新
                            if (TextUtils.isEmpty(newText) && !TextUtils.isEmpty(searchKey)) {
                                search(newText);
                            }

                            return false;
                        }
                    });

                    searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) {
                            searchView.setQuery(searchKey, false);
                            // 隐藏输入法
                            ContextUtils.hideSoftInputFromWindow(v);
                        }
                    });

                    return new DataBindingVH<>(searchView);
                }
                default: {
                    DataBindingVH<LayoutItemNeumorphCardViewTextWithIconBinding> vh =
                            super.onCreateViewHolder(parent, viewType);

                    vh.binding.itemText.setMaxLines(Integer.MAX_VALUE);
                    vh.binding.itemText.setGravity(Gravity.NO_GRAVITY);

                    ViewGroup.LayoutParams layoutParams = vh.binding.itemIcon.getLayoutParams();
                    layoutParams.width = layoutParams.height = ResourcesUtils.getDensityPx(24);
                    vh.binding.itemIcon.setLayoutParams(layoutParams);

                    // 长按可复制下载链接
                    vh.itemView.setOnLongClickListener(v -> {
                        AlertDialog alertDialog = new AlertDialog.Builder(v.getContext())
                                .setTitle(getString(R.string.file_download_long_click_copy, vh.binding.getModel().title))
                                .setMessage(((Model) v.getTag()).getOnLongClickMessage())
                                .create();

                        alertDialog.show();

                        TextView textView = Objects.requireNonNull(alertDialog.findViewById(android.R.id.message));

                        textView.setTextIsSelectable(true);

                        return true;
                    });

                    return vh;
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<LayoutItemNeumorphCardViewTextWithIconBinding> holder, int position) {
            if (position == TIP_VIEW_TYPE) {
                ((TextView) holder.itemView).setText(HtmlUtils.fromHtml(R.string.file_download_tips, getItemCount() - 2));
            } else if (position == SEARCH_VIEW_TYPE) {
                // 默认清除searchView的焦点
                holder.itemView.clearFocus();
            } else if (position > 1) {
                super.onBindViewHolder(holder, position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                    return TIP_VIEW_TYPE;
                case 1:
                    return SEARCH_VIEW_TYPE;
                default:
                    return MODEL_VIEW_TYPE;
            }
        }

        @Override
        public void onCurrentListChanged(@NonNull List<NeumorphCardViewTextWithIconModel> previousList, @NonNull List<NeumorphCardViewTextWithIconModel> currentList) {
            super.onCurrentListChanged(previousList, currentList);

            // 更新时 移到最前面
            binding.recyclerView.post(() -> binding.recyclerView.scrollToPosition(0));
        }

        /**
         * 搜索
         */
        private void search(String key) {
            // 记录搜索的字符串
            searchKey = key;

            disposable.add(fileViewModel.search(key)
                    .subscribe(fileModels -> adapter.submitList(fileModels)));
        }

        @Override
        public void submitList(@Nullable List<NeumorphCardViewTextWithIconModel> list) {
            if (list != null) {
                // 添加tip和搜索
                list.add(0, TIP_MODEL);
                list.add(1, SEARCH_MODEL);
            }
            super.submitList(list);
        }
    }

    public static class Model extends NeumorphCardViewTextWithIconModel {

        public final String url;

        public final String webVpnUrl;

        public Model(String title, @NonNull Drawable icon, @NonNull Runnable runnable, String url, String webVpnUrl) {
            super(title, icon, runnable);

            this.url = url;
            this.webVpnUrl = webVpnUrl;
        }

        /**
         * 用于创建占位符
         */
        @SuppressWarnings("all")
        public Model() {
            super(null, null, null);
            this.url = "";
            this.webVpnUrl = null;
        }

        /**
         * @return 长按项时 弹出AlertDialog 设置的Message(url + \n\n + webVpnUrl(如果有的话))
         */
        public Spanned getOnLongClickMessage() {
            return (TextUtils.isEmpty(webVpnUrl) ?
                    HtmlUtils.fromHtml(R.string.file_download_long_click_copy_tip, url) :
                    HtmlUtils.fromHtml(R.string.file_download_long_click_copy_tip_web_vpn, url, webVpnUrl));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Model)) return false;
            Model model = (Model) o;
            return Objects.equals(url, model.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url);
        }
    }
}