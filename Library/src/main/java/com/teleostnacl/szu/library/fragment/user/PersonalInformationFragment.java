package com.teleostnacl.szu.library.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.java.util.StringUtils;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.FragmentMyLibraryPersonInformatinBinding;
import com.teleostnacl.szu.library.databinding.LayoutModifyUserInfoBinding;
import com.teleostnacl.szu.library.viewmodel.MyLibraryMainViewModel;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;

public class PersonalInformationFragment extends BaseLoadingFragment {
    private FragmentMyLibraryPersonInformatinBinding binding;

    private MyLibraryMainViewModel myLibraryMainViewModel;

    private ObservableEmitter<Object> emitter;

    // 发送给Emitter取消请求的Model
    private static final Object CANCEL_MODEL = new Object();

    // 刷新页面的Disposable
    private Disposable refreshDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myLibraryMainViewModel = new ViewModelProvider(requireActivity()).get(MyLibraryMainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_library_person_informatin, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setUserInfo(myLibraryMainViewModel.myLibraryModel.userInfoModel);

        binding.fragmentPersonalInformationEditContact.setOnClickListener(v -> showModifyInfoDialog());

        disposable.add(Observable.create(emitter ->
                        PersonalInformationFragment.this.emitter = emitter)
                .switchMap(myLibraryUserInfoModel -> {
                    if (myLibraryUserInfoModel == CANCEL_MODEL) {
                        return Observable.empty();
                    } else {
                        showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.my_library_user_info_modifying);
                        return myLibraryMainViewModel.modifyUserInfo();
                    }
                })
                .subscribe(aBoolean -> {
                    ToastUtils.makeToast(aBoolean ? R.string.my_library_user_info_modify_successful : R.string.my_library_user_info_modify_fail);

                    if (aBoolean) {
                        refresh();
                    } else {
                        hideLoadingView();
                    }
                }));

        // 下拉刷新事件
        binding.swipeRefreshLayout.setOnRefreshListener(this::refresh);
    }

    /**
     * 刷新页面
     */
    private void refresh() {
        showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.my_library_loading);
        if (refreshDisposable != null) {
            refreshDisposable.dispose();
            disposable.delete(refreshDisposable);
            refreshDisposable = null;
        }

        refreshDisposable = myLibraryMainViewModel.getUserInfo(true).subscribe((aBoolean, throwable) -> {
            if (!aBoolean) {
                NavigationUtils.popBackStack(requireView());
            } else {
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.setUserInfo(myLibraryMainViewModel.myLibraryModel.userInfoModel);
            }
            hideLoadingView();
        });
        disposable.add(refreshDisposable);
    }

    @Override
    public boolean onBackPressed() {
        // 取消正在提交的
        if (emitter != null && isLoadingViewShowing()) {
            emitter.onNext(CANCEL_MODEL);
            hideLoadingView();
            return true;
        }

        return false;
    }

    /**
     * 展示修改联系方式的Dialog
     */
    public void showModifyInfoDialog() {
        LayoutModifyUserInfoBinding binding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()),
                R.layout.layout_modify_user_info, null, false);

        binding.setUserInfo(myLibraryMainViewModel.myLibraryModel.userInfoModel);

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.my_library_user_info_edit_contact_information_title)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.my_library_user_info_edit, null)
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, (dialog, which) ->
                        myLibraryMainViewModel.myLibraryModel.userInfoModel.clearEdit())
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // 邮箱地址无误 发送修改的请求
            if (StringUtils.isEmail(myLibraryMainViewModel.myLibraryModel.userInfoModel.editMail)) {
                emitter.onNext(new Object());
                alertDialog.dismiss();
            } else {
                ToastUtils.makeToast(R.string.my_library_user_info_email_error);
            }
        });
    }
}
