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
import com.teleostnacl.common.android.fragment.BaseDisposableFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.scheme.R;
import com.teleostnacl.szu.scheme.databinding.FragmentSchemeMainBinding;
import com.teleostnacl.szu.scheme.model.YearModel;
import com.teleostnacl.szu.scheme.viewmodel.SchemeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 培养方案主页面
 */
public class SchemeMainFragment extends BaseDisposableFragment {

    private FragmentSchemeMainBinding binding;

    private SchemeViewModel schemeViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        schemeViewModel = new ViewModelProvider(requireActivity()).get(SchemeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scheme_main, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    private void initView() {
        initToolbar();
    }

    private void initToolbar() {
        // 显示返回键
        NavigationUtils.navigationForToolbar(binding.fragmentSchemeMainToolbar, v -> requireActivity().finish());

        // 设置TabLayout的标签值
        List<String> list = new ArrayList<>(schemeViewModel.getSchemeModel().yearModels.size());
        list.add(getString(R.string.scheme_year_all));
        for (YearModel yearModel : schemeViewModel.getSchemeModel().yearModels) {
            list.add(yearModel.name);
        }
        for (String tab : list) {
            binding.fragmentSchemeMainTabLayout.addTab(binding.fragmentSchemeMainTabLayout.newTab().setText(tab));
        }

        // 初始化ViewPager2
        binding.fragmentSchemeMainViewPage2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return YearSchemeFragment.getInstance(position);
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        });

        // 与TabLayout进行绑定
        new TabLayoutMediator(binding.fragmentSchemeMainTabLayout, binding.fragmentSchemeMainViewPage2,
                (tab, position) -> tab.setText(list.get(position))).attach();
    }
}
