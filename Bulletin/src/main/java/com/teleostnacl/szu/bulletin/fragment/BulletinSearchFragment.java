package com.teleostnacl.szu.bulletin.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.paging.PagingRecyclerView;
import com.teleostnacl.common.android.fragment.BaseDisposableFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.bulletin.R;
import com.teleostnacl.szu.bulletin.databinding.FragmentBulletinSearchBinding;
import com.teleostnacl.szu.bulletin.recyclerview.adapter.BulletinPagingDataAdapter;
import com.teleostnacl.szu.bulletin.viewmodel.BulletinViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索页面
 */
public class BulletinSearchFragment extends BaseDisposableFragment {

    private final SearchPagingAdapter pagingDataAdapter = new SearchPagingAdapter();

    private BulletinViewModel bulletinViewModel;

    private FragmentBulletinSearchBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bulletinViewModel = new ViewModelProvider(requireActivity()).get(BulletinViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bulletin_search, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    private void initView() {
        initToolbar();
        initSearchView();
        initSpinners();
        initRecyclerView();
    }

    /**
     * 初始化Toolbar
     */
    private void initToolbar() {
        // 显示返回键
        NavigationUtils.navPopBackForToolbar(binding.fragmentBulletinSearchToolbar);
    }

    /**
     * 初始化SearchView
     */
    private void initSearchView() {
        binding.fragmentBulletinSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                bulletinViewModel.bulletinModel.searchKey = query;
                pagingDataAdapter.refresh();
                binding.fragmentBulletinSearchPagingRecyclerView.scrollToTop();
                binding.fragmentBulletinSearch.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        binding.fragmentBulletinSearch.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                binding.fragmentBulletinSearch.setQuery(bulletinViewModel.bulletinModel.searchKey, false);
            }
        });
    }

    /**
     * 初始化Spinner
     */
    private void initSpinners() {
        // 搜索时间
        List<String> dayyList = new ArrayList<>(bulletinViewModel.bulletinModel.dayyMap.keySet());
        ArrayAdapter<String> dayyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, dayyList);
        dayyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fragmentBulletinSearchTime.setAdapter(dayyAdapter);
        binding.fragmentBulletinSearchTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bulletinViewModel.bulletinModel.dayy = bulletinViewModel.bulletinModel.dayyMap.get(dayyList.get(position));
                pagingDataAdapter.refresh();
                binding.fragmentBulletinSearchPagingRecyclerView.scrollToTop();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 发文单位
        List<String> usernameFromList = new ArrayList<>(bulletinViewModel.bulletinModel.fromUsernameMap.keySet());
        ArrayAdapter<String> usernameFromAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, usernameFromList);
        usernameFromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fragmentBulletinSearchFrom.setAdapter(usernameFromAdapter);
        binding.fragmentBulletinSearchFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bulletinViewModel.bulletinModel.fromUsername = bulletinViewModel.bulletinModel.fromUsernameMap.get(usernameFromList.get(position));
                pagingDataAdapter.refresh();
                binding.fragmentBulletinSearchPagingRecyclerView.scrollToTop();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        binding.fragmentBulletinSearchPagingRecyclerView.setSwipeRefreshEnabled(false);
        binding.fragmentBulletinSearchPagingRecyclerView.setAdapter(pagingDataAdapter);
        disposable.add(bulletinViewModel.getSearchBulletinsResultPagingData()
                .subscribe(pagingData -> pagingDataAdapter.submitData(getLifecycle(), pagingData)));
    }

    /**
     * 展示搜索结果的PagingAdapter, 继承ICheckShowNone接口 只有当搜索内容不为空时才显示结果空白的View
     */
    private class SearchPagingAdapter extends BulletinPagingDataAdapter implements PagingRecyclerView.ICheckShowNone {

        @Override
        public boolean isShowNone() {
            return !TextUtils.isEmpty(binding.fragmentBulletinSearch.getQuery());
        }
    }
}
