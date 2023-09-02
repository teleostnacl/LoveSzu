package com.teleostnacl.szu.library.fragment.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;

import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.recyclerview.ClickedOnceListAdapter;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.FragmentLibraryBookDetailRelatedBookBinding;
import com.teleostnacl.szu.library.databinding.ItemFragmentBookDetailBookRelatedBinding;
import com.teleostnacl.szu.library.model.TitleAndAuthorModel;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.viewmodel.LibraryBookDetailViewModel;
import com.teleostnacl.szu.library.viewmodel.LibraryMainViewModel;

/**
 * 相关图书使用的Fragment
 */
public class RelatedBookFragment extends BaseLoadingFragment {

    private FragmentLibraryBookDetailRelatedBookBinding binding;

    private LibraryBookDetailViewModel libraryBookDetailViewModel;
    private LibraryMainViewModel libraryMainViewModel;

    // 相关借阅使用的Adapter
    private final ListAdapter<TitleAndAuthorModel, DataBindingVH<ItemFragmentBookDetailBookRelatedBinding>>
            bookAdapter = new ClickedOnceListAdapter<>(new DefaultItemCallback<>()) {
        @NonNull
        @Override
        public DataBindingVH<ItemFragmentBookDetailBookRelatedBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DataBindingVH<ItemFragmentBookDetailBookRelatedBinding> viewHolder = new DataBindingVH<>(parent, R.layout.item_fragment_book_detail_book_related);

            setOnClickListener(viewHolder.itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ItemFragmentBookDetailBookRelatedBinding> holder, int position) {
            holder.binding.setHotLoanModel(getItem(position));

            holder.itemView.setTag(getItem(position).bookModel);
        }

        @Override
        public void onClick(@NonNull View v) {
            libraryBookDetailViewModel.setBook((BookModel) v.getTag());
            NavigationUtils.navigate(v, R.id.action_relatedBookFragment_to_bookDetailFragment);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        libraryBookDetailViewModel = new ViewModelProvider(requireActivity()).get(LibraryBookDetailViewModel.class);
        libraryMainViewModel = new ViewModelProvider(requireActivity()).get(LibraryMainViewModel.class);

        showLoadingView(com.teleostnacl.common.android.R.color.white,
                R.string.book_detail_related_book_loading, libraryBookDetailViewModel.getBook().title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library_book_detail_related_book, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        disposable.add(libraryBookDetailViewModel.getRelatedBooksFromNetwork()
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        // 相关借阅为空
                        if (libraryBookDetailViewModel.getBook().relatedBookList.size() == 0) {
                            ToastUtils.makeToast(R.string.book_detail_related_book_none);
                            NavigationUtils.popBackStack(requireView());
                        } else {
                            initView();
                        }

                    } else {
                        NavigationUtils.popBackStack(requireView());
                    }
                }));

    }

    private void initView() {
        binding.setBook(libraryBookDetailViewModel.getBook());

        initActionBar();
        initRecyclerView();

        binding.rootView.setVisibility(View.VISIBLE);
        binding.getRoot().post(this::hideLoadingView);
    }

    /**
     * 初始化ActionBar
     */
    private void initActionBar() {
        // 显示返回键
        NavigationUtils.navPopBackForToolbar(binding.fragmentLibraryBookReserveToolbar);
    }

    /**
     * 展示相关借阅的recyclerView
     */
    private void initRecyclerView() {
        bookAdapter.submitList(libraryBookDetailViewModel.getRelatedBooks(libraryMainViewModel.getTextColor()));
        binding.fragmentLibraryBookDetailRelatedBookRecyclerView.setItemAnimator(null);
        binding.fragmentLibraryBookDetailRelatedBookRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.fragmentLibraryBookDetailRelatedBookRecyclerView.setAdapter(bookAdapter);
    }
}
