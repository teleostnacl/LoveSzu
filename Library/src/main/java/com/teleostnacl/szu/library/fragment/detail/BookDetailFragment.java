package com.teleostnacl.szu.library.fragment.detail;

import static com.teleostnacl.szu.library.fragment.detail.ReserveFragment.ARG_IS_RESERVE;

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
import androidx.recyclerview.widget.ListAdapter;

import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.recyclerview.ClickedOnceListAdapter;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.FragmentLibraryBookDetailBinding;
import com.teleostnacl.szu.library.databinding.FragmentLibraryBookDetailLayoutBasicInformationBinding;
import com.teleostnacl.szu.library.databinding.FragmentLibraryBookDetailLayoutBorrowInformationBinding;
import com.teleostnacl.szu.library.databinding.FragmentLibraryBookDetailLayoutInformationBinding;
import com.teleostnacl.szu.library.databinding.ItemFragmentLibraryBookDetailCollectionBinding;
import com.teleostnacl.szu.library.databinding.LayoutScoreViewBinding;
import com.teleostnacl.szu.library.model.detail.LibraryCollectionModel;
import com.teleostnacl.szu.library.model.detail.ScoreModel;
import com.teleostnacl.szu.library.model.list.LibraryBookListModel;
import com.teleostnacl.szu.library.viewmodel.LibraryBookDetailViewModel;
import com.teleostnacl.szu.library.viewmodel.LibraryListViewModel;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 展示图书详细的Fragment
 */
public class BookDetailFragment extends BaseLoadingFragment {

    // region 统一rxjava请求onNext的对象
    // 收藏时的请求
    private static final String COLLECT = "collect";
    // 评分
    private static final String SC_1 = "1";
    private static final String SC_2 = "2";
    private static final String SC_3 = "3";
    private static final String SC_4 = "4";
    private static final String SC_5 = "5";

    // 取消的请求
    private static final String CANCEL = "CANCEL";
    // endregion

    // region 用于显示不同类型视图的占位符
    // 基本信息
    private static final LibraryCollectionModel BASE_INFORMATION_MODEL = new LibraryCollectionModel();
    // 图书简介
    private static final LibraryCollectionModel INFORMATION_MODEL = new LibraryCollectionModel();
    // 借阅信息
    private static final LibraryCollectionModel BORROW_INFORMATION_MODEL = new LibraryCollectionModel();
    // endregion

    private LibraryBookDetailViewModel libraryBookDetailViewModel;

    private FragmentLibraryBookDetailBinding binding;

    private final ListAdapter<LibraryCollectionModel, DataBindingVH<ViewDataBinding>> adapter = new ClickedOnceListAdapter<>(new DefaultItemCallback<>()) {
        @Override
        public void onClick(@NonNull View v) {
            Object libraryCollectionModel = v.getTag();

            if (libraryCollectionModel instanceof LibraryCollectionModel) {
                new ViewModelProvider(requireActivity()).get(LibraryListViewModel.class).addLibraryBookListModel(
                        new LibraryBookListModel(libraryBookDetailViewModel.getBook(), (LibraryCollectionModel) libraryCollectionModel));
                ToastUtils.makeToast(R.string.library_list_fragment_add);
            }
        }

        @NonNull
        @Override
        public DataBindingVH<ViewDataBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                // 图书基本信息
                case 0:
                    return new DataBindingVH<>(parent, R.layout.fragment_library_book_detail_layout_basic_information);
                // 图书简介
                case 1:
                    return new DataBindingVH<>(parent, R.layout.fragment_library_book_detail_layout_information);
                // 借阅信息
                case 2:
                    return new DataBindingVH<>(parent, R.layout.fragment_library_book_detail_layout_borrow_information);
                // 馆藏信息
                default: {
                    DataBindingVH<ViewDataBinding> vh = new DataBindingVH<>(parent, R.layout.item_fragment_library_book_detail_collection);
                    setOnClickListener(vh.itemView);
                    return vh;
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ViewDataBinding> holder, int position) {
            // 图书基本信息
            if (holder.binding instanceof FragmentLibraryBookDetailLayoutBasicInformationBinding) {
                ((FragmentLibraryBookDetailLayoutBasicInformationBinding) holder.binding).setBook(libraryBookDetailViewModel.getBook());
            }
            // 图书简介
            else if (holder.binding instanceof FragmentLibraryBookDetailLayoutInformationBinding) {
                ((FragmentLibraryBookDetailLayoutInformationBinding) holder.binding).setBook(libraryBookDetailViewModel.getBook());
            }
            // 图书借阅信息
            else if (holder.binding instanceof FragmentLibraryBookDetailLayoutBorrowInformationBinding) {
                ((FragmentLibraryBookDetailLayoutBorrowInformationBinding) holder.binding).setBook(libraryBookDetailViewModel.getBook());
            }
            // 图书馆藏书信息
            else if (holder.binding instanceof ItemFragmentLibraryBookDetailCollectionBinding && position > 2) {
                LibraryCollectionModel libraryCollectionModel = getItem(position);
                ((ItemFragmentLibraryBookDetailCollectionBinding) holder.binding).setCollection(libraryCollectionModel);
                holder.itemView.setTag(libraryCollectionModel);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    };

    // 统一管理发送请求的Emitter
    private ObservableEmitter<String> emitter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        libraryBookDetailViewModel = new ViewModelProvider(requireActivity()).get(LibraryBookDetailViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library_book_detail, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 下拉更新事件
        binding.fragmentLibraryBookDetailSwipeRefreshLayout.setOnRefreshListener(() -> {
            // 设置未初始化完成
            libraryBookDetailViewModel.getBook().init = false;
            getBookDetail();
            binding.fragmentLibraryBookDetailSwipeRefreshLayout.setRefreshing(false);
        });

        getBookDetail();
    }

    @Override
    public boolean onBackPressed() {
        if (libraryBookDetailViewModel.getBook().init && emitter != null && isLoadingViewShowing()) {
            emitter.onNext(CANCEL);
            hideLoadingView();
            ToastUtils.makeToast(com.teleostnacl.common.android.R.string.cancel);
            return true;
        }

        // 返回时 从bookModels堆栈弹出
        libraryBookDetailViewModel.removeBook();
        return false;
    }

    /**
     * 获取图书详细信息
     */
    private void getBookDetail() {
        // 禁用下拉更新
        binding.fragmentLibraryBookDetailSwipeRefreshLayout.setEnabled(false);

        // 展示loading
        showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.library_book_detail_fragment_loading);

        disposable.add(libraryBookDetailViewModel.getBookDetail().subscribe(aBoolean -> {
            if (aBoolean) {
                initView();
            } else {
                NavigationUtils.popBackStack(binding.getRoot());
            }
        }));
    }

    private void initView() {
        binding.setBook(libraryBookDetailViewModel.getBook());
        initActionBar();
        initRecyclerView();

        // 初始化统一管理菜单请求
        disposable.add(createDisposable());

        binding.getRoot().setVisibility(View.VISIBLE);
        binding.getRoot().post(() -> {
            hideLoadingView();
            binding.fragmentLibraryBookDetailSwipeRefreshLayout.setEnabled(true);
        });
    }

    /**
     * 创建统一管理请求的disposable
     */
    @NonNull
    @Contract(" -> new")
    private Disposable createDisposable() {
        return Observable.create((ObservableOnSubscribe<String>) emitter -> BookDetailFragment.this.emitter = emitter)
                .switchMap(s -> {
                    switch (s) {
                        // 收藏
                        case COLLECT: {
                            return libraryBookDetailViewModel.collectBook();
                        }

                        // 评分
                        case SC_1:
                        case SC_2:
                        case SC_3:
                        case SC_4:
                        case SC_5: {
                            return libraryBookDetailViewModel.scoreBook(s);
                        }

                        // 取消请求
                        case CANCEL:
                        default:
                            return Observable.empty();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> hideLoadingView());
    }

    /**
     * 初始化ActionBar
     */
    private void initActionBar() {
        NavigationUtils.navPopBackForToolbar(binding.fragmentLibraryBookDetailToolbar);

        initMenu();
    }

    /**
     * 初始化显示的内容
     */
    private void initRecyclerView() {
        libraryBookDetailViewModel.getBook().bookCollectionLiveData.observe(getViewLifecycleOwner(), libraryCollectionModels -> {
            List<LibraryCollectionModel> list = new ArrayList<>();
            // 用于占位符, 图书基本信息
            list.add(BASE_INFORMATION_MODEL);
            // 图书简介
            list.add(INFORMATION_MODEL);
            // 借阅信息
            list.add(BORROW_INFORMATION_MODEL);
            // 馆藏信息
            list.addAll(libraryCollectionModels);
            adapter.submitList(list);
        });
        binding.fragmentLibraryBookDetailRecyclerView.setItemAnimator(null);
        binding.fragmentLibraryBookDetailRecyclerView.setAdapter(adapter);
    }

    /**
     * 初始化菜单
     */
    private void initMenu() {
        binding.fragmentLibraryBookDetailToolbar.inflateMenu(R.menu.menu_library_fragment_book_detail);

        binding.fragmentLibraryBookDetailToolbar.setOnMenuItemClickListener(item -> {
            // 我要收藏
            if (item.getItemId() == R.id.fragment_library_book_detail_menu_collect) {
                showLoadingView(com.teleostnacl.common.android.R.color.white,
                        R.string.book_detail_fragment_collection_handling);
                emitter.onNext(COLLECT);
            }
            // 我要评分
            else if (item.getItemId() == R.id.fragment_library_book_detail_menu_score) {
                showScoreAlertDialog();
            }
            // 预约和预借
            else if (item.getItemId() == R.id.fragment_library_book_detail_menu_borrowing_advanced ||
                    item.getItemId() == R.id.fragment_library_book_detail_menu_reserve) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(ARG_IS_RESERVE, item.getItemId() == R.id.fragment_library_book_detail_menu_reserve);
                NavigationUtils.navigate(requireView(), R.id.action_bookDetailFragment_to_reserveFragment, bundle);
            } else if (item.getItemId() == R.id.fragment_library_book_detail_menu_related_borrowing) {
                NavigationUtils.navigate(requireView(), R.id.action_bookDetailFragment_to_relatedBookFragment);
            }
            // 用浏览器打开
            else if (item.getItemId() == R.id.fragment_library_book_detail_menu_open_in_browser) {
                ContextUtils.startBrowserActivity(libraryBookDetailViewModel.getBook().referer);
            }
            return true;
        });
    }

    /**
     * 展示评分的AlertDialog
     */
    private void showScoreAlertDialog() {
        // 初始化视图
        LayoutScoreViewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()),
                R.layout.layout_score_view, null, false);
        ScoreModel scoreModel = new ScoreModel();
        binding.setScore(scoreModel);

        // 创建AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.book_detail_fragment_score)
                .setView(binding.getRoot())
                .show();

        // 设置点击分数的事件
        scoreModel.setOnClickListener(v -> {
            String sc = "";
            if (v.getId() == R.id.layout_score_view_one) sc = "1";
            else if (v.getId() == R.id.layout_score_view_two) sc = "2";
            else if (v.getId() == R.id.layout_score_view_three) sc = "3";
            else if (v.getId() == R.id.layout_score_view_four) sc = "4";
            else if (v.getId() == R.id.layout_score_view_five) sc = "5";
            alertDialog.hide();
            // 评分
            emitter.onNext(sc);
            showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.book_detail_fragment_score_handling);
        });
    }
}
