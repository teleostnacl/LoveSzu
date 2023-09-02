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
import com.teleostnacl.common.android.fragment.BaseLogFragment;
import com.teleostnacl.szu.scheme.R;
import com.teleostnacl.szu.scheme.databinding.FragmentSchemeDatailModuleBinding;
import com.teleostnacl.szu.scheme.model.SchemeDetailGroup;
import com.teleostnacl.szu.scheme.viewmodel.SchemeViewModel;

import java.util.ArrayList;
import java.util.List;

public class SchemeDetailModuleFragment extends BaseLogFragment {

    private static final String ARG_MODULE_INDEX = "1";

    private FragmentSchemeDatailModuleBinding binding;

    private SchemeDetailGroup mSchemeModule;

    private SchemeViewModel schemeViewModel;

    // 记录该Fragment是第几个课程模块
    private int mModuleIndex;

    private SchemeDetailModuleFragment() {
    }

    @NonNull
    public static SchemeDetailModuleFragment getInstance(int moduleIndex) {
        SchemeDetailModuleFragment fragment = new SchemeDetailModuleFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(ARG_MODULE_INDEX, moduleIndex);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        schemeViewModel = new ViewModelProvider(requireActivity()).get(SchemeViewModel.class);

        mModuleIndex = requireArguments().getInt(ARG_MODULE_INDEX);

        mSchemeModule = schemeViewModel.getSchemeDetailModel().schemeDetailModule.get(mModuleIndex);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scheme_datail_module, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置TabLayout的标签值
        List<String> list = new ArrayList<>(mSchemeModule.schemeDetailGroups.size());
        for (SchemeDetailGroup group : mSchemeModule.schemeDetailGroups) {
            list.add(group.getNameAndCredits());
        }
        for (String tab : list) {
            binding.fragmentSchemeDetailModuleTabLayout.addTab(binding.fragmentSchemeDetailModuleTabLayout.newTab().setText(tab));
        }

        // 初始化ViewPager2
        binding.fragmentSchemeDetailModuleViewPage2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return SchemeDetailFragment.getInstance(mModuleIndex, position);
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        });

        // 与TabLayout进行绑定
        new TabLayoutMediator(binding.fragmentSchemeDetailModuleTabLayout, binding.fragmentSchemeDetailModuleViewPage2,
                (tab, position) -> tab.setText(list.get(position))).attach();
    }
}
