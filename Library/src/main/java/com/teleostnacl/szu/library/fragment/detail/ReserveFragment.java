package com.teleostnacl.szu.library.fragment.detail;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.java.util.StringUtils;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.FragmentLibraryBookDetailReserveBinding;
import com.teleostnacl.szu.library.viewmodel.LibraryBookDetailViewModel;

import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

/**
 * 预约预借使用的Fragment
 */
public class ReserveFragment extends BaseLoadingFragment {

    // 提交预约
    private static final String SUBMIT_RESERVE = "1";
    // 提交预借
    private static final String SUBMIT_BORROW_ADVANCE = "2";
    // 取消请求
    private static final String CANCEL_SUBMIT = "3";

    // 传参 是否为预约
    public static final String ARG_IS_RESERVE = "1";

    private FragmentLibraryBookDetailReserveBinding binding;

    private LibraryBookDetailViewModel libraryBookDetailViewModel;

    // 是否为预约的标记
    private boolean reserve;

    private ObservableEmitter<String> submitEmitter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reserve = requireArguments().getBoolean(ARG_IS_RESERVE);

        libraryBookDetailViewModel = new ViewModelProvider(requireActivity()).get(LibraryBookDetailViewModel.class);

        showLoadingView(com.teleostnacl.common.android.R.color.white, reserve ? R.string.library_reserve_query_information : R.string.library_borrow_advance_query_information);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library_book_detail_reserve, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        disposable.add(Observable.just(new Object())
                .flatMap(o -> {
                    if (reserve) {
                        return libraryBookDetailViewModel.getReserveInformation();
                    } else {
                        return libraryBookDetailViewModel.getBorrowAdvanceInformation();
                    }
                })
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        initView();
                    } else {
                        NavigationUtils.popBackStack(view);
                    }
                }));
    }

    @Override
    public boolean onBackPressed() {
        if (submitEmitter != null && isLoadingViewShowing()) {
            submitEmitter.onNext(CANCEL_SUBMIT);
            hideLoadingView();
            ToastUtils.makeLongToast(com.teleostnacl.common.android.R.string.cancel);
            return true;
        }

        return false;
    }

    private void initView() {
        binding.setReserve(reserve);
        binding.setBook(libraryBookDetailViewModel.getBook());
        binding.setReserveModel(libraryBookDetailViewModel.getBook().reserveModel);

        initActionBar();
        initVolumeSpinner();
        initPlaceSpinner();
        initSubmit();

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
     * 初始化显示卷期的Spinner
     */
    private void initVolumeSpinner() {
        //初始化卷期的Spinner
        Set<Map.Entry<String, String>> volumesEntry = libraryBookDetailViewModel.getBook().reserveModel.volumes.entrySet();
        //设置存储key的数组
        String[] volumesKeys = new String[volumesEntry.size()];
        //设置存储值的数组
        String[] volumes = new String[volumesEntry.size()];

        int i = 0;
        for (Map.Entry<String, String> entry : volumesEntry) {
            volumesKeys[i] = entry.getKey();
            volumes[i] = entry.getValue();
            i++;
        }

        //设置adapter
        ArrayAdapter<String> volumeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, volumes);
        volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fragmentLibraryBookReserveVolumeSpinner.setAdapter(volumeAdapter);
        binding.fragmentLibraryBookReserveVolumeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                libraryBookDetailViewModel.getBook().reserveModel.vol = volumesKeys[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 初始化显示地点的Spinner
     */
    private void initPlaceSpinner() {
        //初始化地点的Spinner
        Set<Map.Entry<String, String>> placesEntry = libraryBookDetailViewModel.getBook().reserveModel.places.entrySet();
        //设置存储key的数组
        String[] placesKeys = new String[placesEntry.size()];
        //设置存储值的数组
        String[] places = new String[placesEntry.size()];

        int i = 0;
        for (Map.Entry<String, String> entry : placesEntry) {
            placesKeys[i] = entry.getKey();
            places[i] = entry.getValue();
            i++;
        }

        //设置adapter
        ArrayAdapter<String> placeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, places);
        placeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fragmentLibraryBookReservePlaceSpinner.setAdapter(placeAdapter);
        binding.fragmentLibraryBookReservePlaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                libraryBookDetailViewModel.getBook().reserveModel.ddlfl = placesKeys[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    /**
     * 初始化提交预约
     */
    private void initSubmit() {
        disposable.add(Observable.create((ObservableOnSubscribe<String>) emitter -> submitEmitter = emitter)
                .switchMap(s -> {
                    switch (s) {
                        case SUBMIT_RESERVE:
                            return libraryBookDetailViewModel.submitReserve();
                        case SUBMIT_BORROW_ADVANCE:
                            return libraryBookDetailViewModel.submitBorrowAdvance();
                        case CANCEL_SUBMIT:
                        default:
                            return Observable.empty();
                    }
                })
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        NavigationUtils.popBackStack(requireView());
                    }
                    // 预约或预借失败 隐藏动画
                    else {
                        hideLoadingView();
                    }
                }));

        binding.fragmentLibraryBookReserveSubmitButton.setOnClickListener(v -> {
            // 预约
            if (reserve) {
                if (!StringUtils.isEmail(binding.getReserveModel().txtemail)) {
                    ToastUtils.makeToast(R.string.book_detail_fragment_reserve_error_tip);
                    return;
                }

                showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.book_detail_fragment_reserve_handling);
                submitEmitter.onNext(SUBMIT_RESERVE);
            }
            // 预借
            else {
                if (TextUtils.isEmpty(binding.getReserveModel().txtphone) ||
                        !StringUtils.isEmail(binding.getReserveModel().txtemail)) {
                    ToastUtils.makeToast(R.string.book_detail_fragment_reserve_error_tip);
                    return;
                }

                showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.book_detail_fragment_borrow_in_advance_handling);
                submitEmitter.onNext(SUBMIT_BORROW_ADVANCE);
            }
        });
    }
}
