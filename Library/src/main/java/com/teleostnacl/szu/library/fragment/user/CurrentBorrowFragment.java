package com.teleostnacl.szu.library.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.teleostnacl.common.android.paging.PagingRecyclerView;
import com.teleostnacl.common.android.paging.adapter.ClickOncePagingDataAdapter;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.fragment.BaseDisposableFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.ItemFragmentMyLibraryCurrentBorrowBinding;
import com.teleostnacl.szu.library.model.user.CurrentBorrowModel;
import com.teleostnacl.szu.library.viewmodel.LibraryBookDetailViewModel;
import com.teleostnacl.szu.library.viewmodel.MyLibraryMainViewModel;

public class CurrentBorrowFragment extends BaseDisposableFragment {

    private MyLibraryMainViewModel myLibraryMainViewModel;

    private final PagingDataAdapter<CurrentBorrowModel, DataBindingVH<ItemFragmentMyLibraryCurrentBorrowBinding>> pagingDataAdapter =
            new ClickOncePagingDataAdapter<>(new DefaultItemCallback<>()) {
                @NonNull
                @Override
                public DataBindingVH<ItemFragmentMyLibraryCurrentBorrowBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    DataBindingVH<ItemFragmentMyLibraryCurrentBorrowBinding> viewHolder =
                            new DataBindingVH<>(parent, R.layout.item_fragment_my_library_current_borrow);

                    // 跳转到图书详细页 显示图书信息
                    setOnClickListener(viewHolder.itemView);

                    return viewHolder;
                }

                @Override
                public void onBindViewHolder(@NonNull DataBindingVH<ItemFragmentMyLibraryCurrentBorrowBinding> holder, int position) {
                    holder.binding.setCurrentBorrowModel(getItem(position));
                    holder.itemView.setTag(getItem(position));
                }

                @Override
                public void onClick(@NonNull View v) {
                    // 设置图书信息显示的Book
                    new ViewModelProvider(requireActivity()).get(LibraryBookDetailViewModel.class).setBook((CurrentBorrowModel) v.getTag());

                    NavigationUtils.navigate(v, R.id.action_myLibraryMainFragment_to_bookDetailFragment);
                }
            };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myLibraryMainViewModel = new ViewModelProvider(requireActivity()).get(MyLibraryMainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return new PagingRecyclerView(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((PagingRecyclerView) view).setAdapter(pagingDataAdapter);
        ((PagingRecyclerView) view).getRecyclerView().setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        ((PagingRecyclerView) view).getRecyclerView().setNestedScrollingEnabled(false);

        int padding = ResourcesUtils.getDensityPx(4);
        view.setPadding(padding, 0, padding, 0);

        disposable.add(myLibraryMainViewModel.getCurrentBorrowModelsFlowable()
                .subscribe(pagingData -> pagingDataAdapter.submitData(getLifecycle(), pagingData)));
    }
}
