package com.teleostnacl.szu.scheme.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.scheme.R;
import com.teleostnacl.szu.scheme.databinding.FragmentSchemeDatailMainBinding;
import com.teleostnacl.szu.scheme.model.SchemeDetailGroup;
import com.teleostnacl.szu.scheme.model.SchemeDetailModel;
import com.teleostnacl.szu.scheme.viewmodel.SchemeViewModel;

import java.util.ArrayList;
import java.util.List;

public class SchemeDetailMainFragment extends BaseLoadingFragment {
    private SchemeViewModel schemeViewModel;

    private FragmentSchemeDatailMainBinding binding;

    private SchemeDetailModel mSchemeDetailModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        schemeViewModel = new ViewModelProvider(requireActivity()).get(SchemeViewModel.class);

        mSchemeDetailModel = schemeViewModel.getSchemeDetailModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scheme_datail_main, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.scheme_detail_loading);
        disposable.add(schemeViewModel.getSchemeDetail().subscribe(aBoolean -> {
            if (aBoolean) {
                binding.setModel(mSchemeDetailModel);
                initView();
            } else {
                NavigationUtils.popBackStack(requireView());
            }

            requireView().post(this::hideLoadingView);
        }));
    }

    private void initView() {
        NavigationUtils.navPopBackForToolbar(binding.fragmentSchemeDetailMainToolbar);

        // 设置TabLayout的标签值
        List<String> list = new ArrayList<>(mSchemeDetailModel.schemeDetailModule.size());
        for (SchemeDetailGroup module : mSchemeDetailModel.schemeDetailModule) {
            list.add(module.getNameAndCredits());
        }
        for (String tab : list) {
            binding.fragmentSchemeDetailMainTabLayout.addTab(binding.fragmentSchemeDetailMainTabLayout.newTab().setText(tab));
        }

        // 初始化ViewPager2
        binding.fragmentSchemeDetailMainViewPage2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return SchemeDetailModuleFragment.getInstance(position);
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        });
        if (mSchemeDetailModel.schemeDetailModule.size() > 0) {
            binding.fragmentSchemeDetailMainViewPage2.setOffscreenPageLimit(mSchemeDetailModel.schemeDetailModule.size());
        }

        // 与TabLayout进行绑定
        new TabLayoutMediator(binding.fragmentSchemeDetailMainTabLayout, binding.fragmentSchemeDetailMainViewPage2,
                (tab, position) -> tab.setText(list.get(position))).attach();
    }
}
