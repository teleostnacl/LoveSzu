package com.teleostnacl.szu.library.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;

import com.teleostnacl.common.android.paging.adapter.ClickOncePagingDataAdapter;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseDisposableFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.library.LibraryActivity;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.FragmentLibraryListBinding;
import com.teleostnacl.szu.library.databinding.ItemFragmentLibraryListBinding;
import com.teleostnacl.szu.library.databinding.ItemFragmentLibraryListCollectionSiteBinding;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.list.LibraryBookListCollectionSiteModel;
import com.teleostnacl.szu.library.model.list.LibraryBookListModel;
import com.teleostnacl.szu.library.viewmodel.LibraryBookDetailViewModel;
import com.teleostnacl.szu.library.viewmodel.LibraryListViewModel;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

/**
 * 用于展示添加进清单的图书和馆藏位置
 */
public class LibraryListFragment extends BaseDisposableFragment {
    private LibraryListViewModel libraryListViewModel;

    private FragmentLibraryListBinding binding;

    private final LibraryListAdapter libraryListAdapter = new LibraryListAdapter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        libraryListViewModel = new ViewModelProvider(requireActivity()).get(LibraryListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library_list, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    private void initView() {
        initToolbar();
        initRecyclerView();
    }

    private void initToolbar() {
        NavigationUtils.navPopBackForToolbar(binding.fragmentLibraryListToolbar);

        binding.fragmentLibraryListToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.my_library_fragment_menu_select_all) {
                libraryListAdapter.selectAll();
            } else if (item.getItemId() == R.id.library_list_fragment_menu_delete) {
                List<LibraryBookListModel> list = libraryListAdapter.getDeleteModels();

                // 未勾选项
                if (list.size() == 0) {
                    ToastUtils.makeToast(R.string.library_list_fragment_delete_none);
                } else {
                    new AlertDialog.Builder(requireContext())
                            .setMessage(R.string.library_list_fragment_delete)
                            .setPositiveButton(com.teleostnacl.common.android.R.string.delete, (dialog, which) -> {
                                libraryListViewModel.removeLibraryBookListModel(list);
                                libraryListAdapter.refresh();
                            })
                            .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                            .show();
                }
            }

            return true;
        });
    }

    private void initRecyclerView() {
        binding.fragmentLibraryListPagingRecyclerView.setAdapter(libraryListAdapter);

        disposable.add(libraryListViewModel.getLibraryBookListFlowable().subscribe(libraryBookListModelPagingData ->
                libraryListAdapter.submitData(getLifecycle(), libraryBookListModelPagingData)));

        binding.fragmentLibraryListPagingRecyclerView.setNoneText(R.string.library_list_fragment_none);

        binding.fragmentLibraryListPagingRecyclerView.getAdapter().addLoadStateListener(combinedLoadStates -> {
            LoadState loadState = combinedLoadStates.getRefresh();
            if (loadState instanceof LoadState.NotLoading) {
                // 数量为空 不显示menu的全选和删除键
                if (libraryListAdapter.getItemCount() == 0) {
                    binding.fragmentLibraryListToolbar.getMenu().getItem(0).setVisible(false);
                    binding.fragmentLibraryListToolbar.getMenu().getItem(1).setVisible(false);
                } else {
                    binding.fragmentLibraryListToolbar.getMenu().getItem(0).setVisible(true);
                    binding.fragmentLibraryListToolbar.getMenu().getItem(1).setVisible(true);
                }
            }

            return Unit.INSTANCE;
        });

        if (libraryListViewModel.libraryBookListFlowableNeedRefresh) {
            libraryListAdapter.refresh();
            libraryListViewModel.libraryBookListFlowableNeedRefresh = false;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // 显示该Fragment时 隐藏按钮
        ((LibraryActivity) requireActivity()).floatingActionButton.setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // 该Fragment不可见时 显示按钮
        ((LibraryActivity) requireActivity()).floatingActionButton.setVisibility(View.VISIBLE);
    }

    private class LibraryListAdapter extends ClickOncePagingDataAdapter<Object, DataBindingVH<ViewDataBinding>> {

        private final int LIBRARY_BOOK_LIST_MODEL_VIEW_TYPE = 1;
        private final int LIBRARY_BOOK_LIST_COLLECTION_SITE_MODEL_VIEW_TYPE = 2;

        public LibraryListAdapter() {
            super(new DefaultItemCallback<>());
        }

        @NonNull
        @Override
        public DataBindingVH<ViewDataBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            DataBindingVH<ViewDataBinding> vh;

            if (viewType == LIBRARY_BOOK_LIST_MODEL_VIEW_TYPE) {
                vh = new DataBindingVH<>(parent, R.layout.item_fragment_library_list);

                setOnClickListener(vh.itemView);
            } else if (viewType == LIBRARY_BOOK_LIST_COLLECTION_SITE_MODEL_VIEW_TYPE) {
                vh = new DataBindingVH<>(parent, R.layout.item_fragment_library_list_collection_site);
            } else {
                throw new IllegalArgumentException();
            }

            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ViewDataBinding> holder, int position) {
            Object model = getItem(position);

            if (model instanceof LibraryBookListModel && holder.binding instanceof ItemFragmentLibraryListBinding) {
                ((ItemFragmentLibraryListBinding) holder.binding).setModel((LibraryBookListModel) model);
                holder.itemView.setTag(model);

                ((ItemFragmentLibraryListBinding) holder.binding).itemFragmentLibraryListCheckBox.setChecked(((LibraryBookListModel) model).check);
            } else if (model instanceof LibraryBookListCollectionSiteModel && holder.binding instanceof ItemFragmentLibraryListCollectionSiteBinding) {
                ((ItemFragmentLibraryListCollectionSiteBinding) holder.binding).setModel((LibraryBookListCollectionSiteModel) model);

                ((ItemFragmentLibraryListCollectionSiteBinding) holder.binding).itemFragmentLibraryListCollectionSiteCheckBox.setChecked(((LibraryBookListCollectionSiteModel) model).check);
            }
        }

        @Override
        public void onClick(@NonNull View v) {
            // 设置图书信息显示的Book
            new ViewModelProvider(requireActivity()).get(LibraryBookDetailViewModel.class).setBook((BookModel) v.getTag());
            NavigationUtils.navigate(v, R.id.action_libraryListFragment_to_bookDetailFragment);
        }

        @Override
        public int getItemViewType(int position) {
            Object object = getItem(position);

            if (object instanceof LibraryBookListModel) {
                return LIBRARY_BOOK_LIST_MODEL_VIEW_TYPE;
            } else if (object instanceof LibraryBookListCollectionSiteModel) {
                return LIBRARY_BOOK_LIST_COLLECTION_SITE_MODEL_VIEW_TYPE;
            }

            return 0;
        }

        /**
         * 全选
         */
        public void selectAll() {
            for (int i = 0; i < getItemCount(); i++) {
                Object o = getItem(i);
                if (o instanceof LibraryBookListCollectionSiteModel) {
                    ((LibraryBookListCollectionSiteModel) o).setCheck(true);
                }
            }
        }

        /**
         * @return 获取需从图书清单中移除的图书
         */
        @NonNull
        public List<LibraryBookListModel> getDeleteModels() {
            List<LibraryBookListModel> list = new ArrayList<>();

            for (int i = 0; i < getItemCount(); i++) {
                Object o = getItem(i);
                if (o instanceof LibraryBookListModel) {
                    if (((LibraryBookListModel) o).check) {
                        list.add((LibraryBookListModel) o);
                    }
                }
            }

            return list;
        }
    }
}
