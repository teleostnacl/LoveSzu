package com.teleostnacl.szu.bulletin.fragment;

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
import com.teleostnacl.szu.bulletin.R;
import com.teleostnacl.szu.bulletin.databinding.FragmentBulletinMainBinding;
import com.teleostnacl.szu.bulletin.model.BulletinModel;
import com.teleostnacl.szu.bulletin.viewmodel.BulletinViewModel;

/**
 * 公文通主Fragment
 */
public class BulletinMainFragment extends BaseLoadingFragment {

    private BulletinViewModel bulletinViewModel;

    private FragmentBulletinMainBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bulletinViewModel = new ViewModelProvider(requireActivity()).get(BulletinViewModel.class);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bulletin_main, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        String[] tabs = new String[]{getString(R.string.bulletin_main_fragment_info_type_all), getString(R.string.bulletin_main_fragment_info_type_lecture), getString(R.string.bulletin_main_fragment_info_type_teaching), getString(R.string.bulletin_main_fragment_info_type_scientific), getString(R.string.bulletin_main_fragment_info_type_administration), getString(R.string.bulletin_main_fragment_info_type_student), getString(R.string.bulletin_main_fragment_info_type_living)};

        // 设置tabLayout的tab
        for (int i = 0; i < tabs.length; i++) {
            binding.fragmentBulletinMainTabLayout.addTab(binding.fragmentBulletinMainTabLayout.newTab());
        }

        // 设置ViewPager2的Adapter
        binding.fragmentBulletinMainViewPage2.setAdapter(new FragmentStateAdapter(requireActivity()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                String model;
                switch (position) {
                    case 0:
                        model = BulletinModel.INFO_TYPE_ALL;
                        break;
                    case 1:
                        model = BulletinModel.INFO_TYPE_LECTURE;
                        break;
                    case 2:
                        model = BulletinModel.INFO_TYPE_EDUCTION;
                        break;
                    case 3:
                        model = BulletinModel.INFO_TYPE_SCIENTIFIC;
                        break;
                    case 4:
                        model = BulletinModel.INFO_TYPE_ADMINISTRATION;
                        break;
                    case 5:
                        model = BulletinModel.INFO_TYPE_STUDENT;
                        break;
                    case 6:
                        model = BulletinModel.INFO_TYPE_LIVING;
                        break;
                    default:
                        model = "";
                }
                return BulletinInfoTypeFragment.getInstance(model);
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        // 与TabLayout进行绑定
        new TabLayoutMediator(binding.fragmentBulletinMainTabLayout, binding.fragmentBulletinMainViewPage2,
                (tab, position) -> tab.setText(tabs[position])).attach();

        binding.fragmentBulletinMainToolbar.setOnMenuItemClickListener(item -> {
            NavigationUtils.navigate(requireView(), R.id.action_bulletinMainFragment_to_bulletinSearchFragment);
            return true;
        });

        binding.getRoot().post(this::hideLoadingView);
    }
}
