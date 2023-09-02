package com.teleostnacl.szu.library.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingDataAdapter;

import com.teleostnacl.common.android.paging.PagingRecyclerView;
import com.teleostnacl.common.android.paging.adapter.ClickOncePagingDataAdapter;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.ItemFragmentMyLibraryCollectionBinding;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.user.CollectionModel;
import com.teleostnacl.szu.library.viewmodel.LibraryBookDetailViewModel;
import com.teleostnacl.szu.library.viewmodel.MyLibraryMainViewModel;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;

public class CollectionBookFragment extends BaseLoadingFragment {

    private MyLibraryMainViewModel myLibraryMainViewModel;

    private ObservableEmitter<BookModel> emitter;

    private final PagingDataAdapter<CollectionModel, DataBindingVH<ItemFragmentMyLibraryCollectionBinding>> pagingDataAdapter =
            new ClickOncePagingDataAdapter<>(new DefaultItemCallback<>()) {
                @NonNull
                @Override
                public DataBindingVH<ItemFragmentMyLibraryCollectionBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                    DataBindingVH<ItemFragmentMyLibraryCollectionBinding> viewHolder =
                            new DataBindingVH<>(parent, R.layout.item_fragment_my_library_collection);

                    // 跳转到图书详细页 显示图书信息
                    setOnClickListener(viewHolder.itemView);

                    // 长按取消收藏
                    viewHolder.itemView.setOnLongClickListener(v -> {
                        showCancelCollectDialog(viewHolder.binding.getBook());
                        return true;
                    });

                    return viewHolder;
                }

                @Override
                public void onBindViewHolder(@NonNull DataBindingVH<ItemFragmentMyLibraryCollectionBinding> holder, int position) {
                    holder.binding.setBook(getItem(position));
                    holder.itemView.setTag(getItem(position));
                }

                @Override
                public void onClick(@NonNull View v) {
                    // 设置图书信息显示的Book
                    new ViewModelProvider(requireActivity()).get(LibraryBookDetailViewModel.class).setBook((BookModel) v.getTag());

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
        ((PagingRecyclerView) view).getRecyclerView().setNestedScrollingEnabled(false);

        // 获取收藏数据
        disposable.add(myLibraryMainViewModel.getCollectionModelsFlowable().subscribe(
                pagingData -> pagingDataAdapter.submitData(getLifecycle(), pagingData)));

        // 取消收藏图书
        disposable.add(Observable.create((ObservableOnSubscribe<BookModel>) emitter -> CollectionBookFragment.this.emitter = emitter)
                .switchMap((Function<BookModel, ObservableSource<Boolean>>) bookModel -> {
                    // 取消的事件
                    if (bookModel == MyLibraryMainViewModel.CANCEL_BOOK_MODEL) {
                        return Observable.empty();
                    } else {
                        showLoadingView(com.teleostnacl.common.android.R.color.white,
                                R.string.my_library_collection_canceling);
                        return myLibraryMainViewModel.cancelCollectBook((CollectionModel) bookModel);
                    }
                })
                .subscribe(aBoolean -> {
                    hideLoadingView();

                    ToastUtils.makeToast(aBoolean ?
                            R.string.my_library_collection_cancel_successful :
                            R.string.my_library_collection_cancel_fail);

                    // 更新收藏数据
                    if (aBoolean) {
                        pagingDataAdapter.refresh();
                    }
                }));
    }

    @Override
    public boolean onBackPressed() {
        // 取消正在提交的
        if (emitter != null && isLoadingViewShowing()) {
            emitter.onNext(MyLibraryMainViewModel.CANCEL_BOOK_MODEL);
            hideLoadingView();
            return true;
        }

        return false;
    }

    /**
     * 展示询问是否取消收藏图书的Dialog
     */
    private void showCancelCollectDialog(@NonNull CollectionModel collectionModel) {
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.my_library_collection_cancel_sure, collectionModel.title))
                .setPositiveButton(com.teleostnacl.common.android.R.string.yes,
                        (dialog, which) -> emitter.onNext(collectionModel))
                .setNegativeButton(com.teleostnacl.common.android.R.string.back, null)
                .show();
    }
}
