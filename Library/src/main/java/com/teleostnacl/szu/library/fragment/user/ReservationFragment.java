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
import com.teleostnacl.szu.library.databinding.ItemFragmentMyLibraryReservationBinding;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.user.ReservationModel;
import com.teleostnacl.szu.library.viewmodel.LibraryBookDetailViewModel;
import com.teleostnacl.szu.library.viewmodel.MyLibraryMainViewModel;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;

public class ReservationFragment extends BaseLoadingFragment {
    // 传递参数 是否为为预约(否则为预借)
    public static final String ARG_RESERVATION = "1";

    private MyLibraryMainViewModel myLibraryMainViewModel;

    private boolean isReserve;

    private final PagingDataAdapter<ReservationModel, DataBindingVH<ItemFragmentMyLibraryReservationBinding>> pagingDataAdapter =
            new ClickOncePagingDataAdapter<>(new DefaultItemCallback<>()) {
                @NonNull
                @Override
                public DataBindingVH<ItemFragmentMyLibraryReservationBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    DataBindingVH<ItemFragmentMyLibraryReservationBinding> viewHolder = new DataBindingVH<>(
                            parent, R.layout.item_fragment_my_library_reservation);

                    // 跳转到图书详细页 显示图书信息
                    setOnClickListener(viewHolder.itemView);

                    // 长按取消预约
                    viewHolder.itemView.setOnLongClickListener(v -> {
                        showCancelReserveDialog(viewHolder.binding.getReservationModel());
                        return true;
                    });

                    return viewHolder;
                }

                @Override
                public void onBindViewHolder(@NonNull DataBindingVH<ItemFragmentMyLibraryReservationBinding> holder, int position) {
                    holder.binding.setReservationModel(getItem(position));

                    holder.itemView.setTag(getItem(position));
                }

                @Override
                public void onClick(@NonNull View v) {
                    // 设置图书信息显示的Book
                    new ViewModelProvider(requireActivity()).get(LibraryBookDetailViewModel.class).setBook((ReservationModel) v.getTag());

                    NavigationUtils.navigate(v, R.id.action_myLibraryMainFragment_to_bookDetailFragment);

                }
            };

    private ObservableEmitter<BookModel> emitter;

    private ReservationFragment() {
    }

    /**
     * @param isReserve 是否为预约 否则为预借
     * @return 实例
     */
    @NonNull
    public static ReservationFragment getInstance(boolean isReserve) {
        ReservationFragment reservationFragment = new ReservationFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_RESERVATION, isReserve);
        reservationFragment.setArguments(bundle);
        return reservationFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myLibraryMainViewModel = new ViewModelProvider(requireActivity()).get(MyLibraryMainViewModel.class);

        isReserve = requireArguments().getBoolean(ARG_RESERVATION);
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

        ((PagingRecyclerView) view).setAdapter(pagingDataAdapter)
                .setNoneText(R.string.my_library_reservation_reserve_none);
        ((PagingRecyclerView) view).getRecyclerView().setNestedScrollingEnabled(false);

        // 获取预约或预借的数据
        disposable.add(isReserve ?
                myLibraryMainViewModel.getReservableModelsFlowable()
                        .subscribe(pagingData -> pagingDataAdapter.submitData(getLifecycle(), pagingData)) :
                myLibraryMainViewModel.getRequestModelsFlowable().subscribe(
                        pagingData -> pagingDataAdapter.submitData(getLifecycle(), pagingData)));

        // 取消收藏图书
        disposable.add(Observable.create((ObservableOnSubscribe<BookModel>) emitter -> ReservationFragment.this.emitter = emitter)
                .switchMap((Function<BookModel, ObservableSource<Boolean>>) bookModel -> {
                    // 取消的事件
                    if (bookModel == MyLibraryMainViewModel.CANCEL_BOOK_MODEL) {
                        return Observable.empty();
                    } else if (isReserve) {
                        showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.my_library_reservation_reserve_canceling);
                        return myLibraryMainViewModel.cancelReserve((ReservationModel) bookModel);
                    } else {
                        showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.my_library_reservation_borrow_advance_canceling);
                        return myLibraryMainViewModel.cancelBorrowAdvance((ReservationModel) bookModel);
                    }
                })
                .subscribe(aBoolean -> {
                    hideLoadingView();

                    if (isReserve) {
                        ToastUtils.makeToast(aBoolean ? R.string.my_library_reservation_reserve_cancel_successful : R.string.my_library_reservation_cancel_reserve_fail);
                    } else {
                        ToastUtils.makeToast(aBoolean ? R.string.my_library_reservation_borrow_advance_cancel_successful : R.string.my_library_reservation_borrow_advance_cancel_fail);
                    }

                    // 更新预约数据
                    if (aBoolean) {
                        pagingDataAdapter.refresh();
                    }
                }));

    }

    @Override
    public boolean onBackPressed() {
        // 取消正在提交的取消预约申请
        if (emitter != null && isLoadingViewShowing()) {
            emitter.onNext(MyLibraryMainViewModel.CANCEL_BOOK_MODEL);
            hideLoadingView();
            return true;
        }

        return false;
    }

    /**
     * 展示询问是否取消预约或预借图书的Dialog
     */
    private void showCancelReserveDialog(@NonNull ReservationModel reservationModel) {
        if (isReserve) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.my_library_reservation_reserve_cancel_sure, reservationModel.titleAndAuthor))
                    .setPositiveButton(com.teleostnacl.common.android.R.string.yes,
                            (dialog, which) -> emitter.onNext(reservationModel))
                    .setNegativeButton(com.teleostnacl.common.android.R.string.back, null)
                    .show();
        } else {
            //如果状态号不为0或1则表示当前状态不可取消预约
            if (!("0".equals(reservationModel.borrowAdvanceStatusNo) ||
                    "1".equals(reservationModel.borrowAdvanceStatusNo))) {
                ToastUtils.makeToast(R.string.my_library_reservation_borrow_advance_can_not_cancel);
            } else {
                new AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.my_library_reservation_borrow_advance_cancel_sure, reservationModel.title))
                        .setPositiveButton(com.teleostnacl.common.android.R.string.yes,
                                (dialog, which) -> emitter.onNext(reservationModel))
                        .setNegativeButton(com.teleostnacl.common.android.R.string.back, null)
                        .show();
            }
        }
    }
}
