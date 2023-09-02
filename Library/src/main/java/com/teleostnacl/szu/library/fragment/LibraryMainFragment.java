package com.teleostnacl.szu.library.fragment;

import static com.teleostnacl.szu.library.fragment.LibrarySearchFragment.ARG_QUERY;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.cardview.CapsuleShapeView;
import com.teleostnacl.common.android.view.recyclerview.ClickedOnceListAdapter;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.FragmentLibraryMainBinding;
import com.teleostnacl.szu.library.databinding.ItemFragmentLibraryMainHotLoanBinding;
import com.teleostnacl.szu.library.model.TitleAndAuthorModel;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.view.recyclerview.VerticalStaggeredGridLayoutManager;
import com.teleostnacl.szu.library.viewmodel.LibraryBookDetailViewModel;
import com.teleostnacl.szu.library.viewmodel.LibraryMainViewModel;

import java.util.List;

public class LibraryMainFragment extends BaseLoadingFragment {

    private FragmentLibraryMainBinding binding;

    private LibraryMainViewModel libraryMainViewModel;

    // 热门搜索使用的Adapter
    private final ListAdapter<HotSearchModel, HotSearchViewHolder> hotSearchListAdapter =
            new ClickedOnceListAdapter<>(new DefaultItemCallback<>()) {
                @NonNull
                @Override
                public HotSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    final int padding = ResourcesUtils.getDensityPx(8);
                    final int margin = ResourcesUtils.getDensityPx(4);

                    TextView textView = new TextView(parent.getContext());

                    textView.setPadding(2 * padding, padding,
                            2 * padding, padding);
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    textView.setGravity(Gravity.CENTER);

                    CapsuleShapeView view = new CapsuleShapeView(requireContext());
                    view.setCardBackgroundColor(ColorResourcesUtils.getColor(com.teleostnacl.szu.libs.R.color.light_white_background));

                    view.setMargins(margin, margin, margin, margin);

                    view.setCardElevation(0);

                    view.addView(textView);

                    // 单击进行搜索
                    setOnClickListener(view);

                    return new HotSearchViewHolder(view);
                }

                @Override
                public void onBindViewHolder(@NonNull HotSearchViewHolder holder, int position) {
                    holder.setText(getItem(position));

                    holder.itemView.setTag(getItem(position).text);
                }

                @Override
                public void onClick(@NonNull View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(ARG_QUERY, (String) v.getTag());
                    NavigationUtils.navigate(v, R.id.action_libraryMainFragment_to_librarySearchFragment, bundle);
                }
            };

    // 热门借阅与收藏使用的Adapter
    private final ListAdapter<TitleAndAuthorModel, DataBindingVH<ItemFragmentLibraryMainHotLoanBinding>>
            hotLoanListAdapter = new ClickedOnceListAdapter<>(new DefaultItemCallback<>()) {

        @NonNull
        @Override
        public DataBindingVH<ItemFragmentLibraryMainHotLoanBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DataBindingVH<ItemFragmentLibraryMainHotLoanBinding> viewHolder = new DataBindingVH<>(parent, R.layout.item_fragment_library_main_hot_loan);

            setOnClickListener(viewHolder.itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ItemFragmentLibraryMainHotLoanBinding> holder, int position) {
            holder.binding.setHotLoanModel(getItem(position));

            holder.itemView.setTag(getItem(position).bookModel);
        }

        @Override
        public void onClick(@NonNull View v) {
            new ViewModelProvider(requireActivity()).get(LibraryBookDetailViewModel.class).setBook((BookModel) v.getTag());

            NavigationUtils.navigate(v, R.id.action_libraryMainFragment_to_bookDetailFragment);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        libraryMainViewModel = new ViewModelProvider(requireActivity()).get(LibraryMainViewModel.class);

        showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color, R.string.library_loading);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library_main, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        disposable.add(libraryMainViewModel.init().subscribe(aBoolean -> {
            if (aBoolean) {
                initView();
            } else {
                requireActivity().finish();
                hideLoadingView();
            }
        }));
    }

    private void initView() {
        // 点击搜索提示文字 进入搜索页面
        binding.fragmentLibraryMainSearchViewText.setOnClickListener(v ->
                NavigationUtils.navigate(v, R.id.action_libraryMainFragment_to_librarySearchFragment));

        // 获取热门搜索数据
        getHotSearch(false);
        // 初始化热门搜索关键字的recyclerview
        binding.fragmentLibraryMainHotSearchRecyclerView.setLayoutManager(new VerticalStaggeredGridLayoutManager() {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.fragmentLibraryMainHotSearchRecyclerView.setAdapter(hotSearchListAdapter);

        // 获取热门借阅与收藏的数据
        getHotLoan(false);
        binding.fragmentLibraryMainHotLoansRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.fragmentLibraryMainHotLoansRecyclerView.setAdapter(hotLoanListAdapter);

        // 下拉刷新
        binding.fragmentLibraryMainSwipeRefreshLayout.setOnRefreshListener(() -> {
            getHotSearch(true);
            getHotLoan(true);

            binding.fragmentLibraryMainSwipeRefreshLayout.setRefreshing(false);
        });

        binding.fragmentLibraryMainUserButton.setOnClickListener(v ->
                NavigationUtils.navigate(v, R.id.action_libraryMainFragment_to_myLibraryMainFragment));

        binding.getRoot().setVisibility(View.VISIBLE);

        binding.getRoot().post(this::hideLoadingView);
    }

    /**
     * 获取热门搜索
     *
     * @param refresh 是否为刷新
     */
    private void getHotSearch(boolean refresh) {
        List<HotSearchModel> list = libraryMainViewModel.getHotSearch(refresh);

        hotSearchListAdapter.submitList(list);

        if (list == null || list.size() == 0) {
            binding.fragmentLibraryMainHotSearchRecyclerView.setVisibility(View.GONE);
            binding.fragmentLibraryMainHotSearchBlank.setVisibility(View.VISIBLE);
        } else {
            binding.fragmentLibraryMainHotSearchRecyclerView.setVisibility(View.VISIBLE);
            binding.fragmentLibraryMainHotSearchBlank.setVisibility(View.GONE);
        }
    }

    private void getHotLoan(boolean refresh) {
        List<TitleAndAuthorModel> list = libraryMainViewModel.getHotLoan(refresh);

        hotLoanListAdapter.submitList(list);

        if (list == null || list.size() == 0) {
            binding.fragmentLibraryMainHotLoansRecyclerView.setVisibility(View.GONE);
            binding.fragmentLibraryMainHotLoansBlank.setVisibility(View.VISIBLE);
        } else {
            binding.fragmentLibraryMainHotLoansRecyclerView.setVisibility(View.VISIBLE);
            binding.fragmentLibraryMainHotLoansBlank.setVisibility(View.GONE);
        }
    }


    /**
     * 热门搜索使用的模型
     */
    public static class HotSearchModel {
        // 热门搜索关键字
        public String text;
        // 热门搜索关键字的字体颜色
        @ColorInt
        public int textColor;

        public HotSearchModel(String text, @ColorInt int textColor) {
            this.text = text;
            this.textColor = textColor;
        }
    }

    /**
     * 热门搜索的ViewHolder
     */
    private static class HotSearchViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public HotSearchViewHolder(@NonNull ViewGroup itemView) {
            super(itemView);

            textView = (TextView) itemView.getChildAt(0);
        }

        public void setText(HotSearchModel model) {
            textView.setText(model.text);
            textView.setTextColor(model.textColor);
        }
    }
}
