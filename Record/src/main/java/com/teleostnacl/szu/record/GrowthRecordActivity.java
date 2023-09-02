package com.teleostnacl.szu.record;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.teleostnacl.common.android.activity.IBaseBackFragmentActivity;
import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.libs.activity.BaseLoadingActivity;
import com.teleostnacl.szu.record.databinding.ActivityGrowthRecordBinding;
import com.teleostnacl.szu.record.fragment.SemesterGrowthRecordFragment;
import com.teleostnacl.szu.record.viewmodel.GrowthRecordViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GrowthRecordActivity extends BaseLoadingActivity implements IBaseBackFragmentActivity {

    private WeakReference<BaseBackFragment> mCurrentFragment;

    private ActivityGrowthRecordBinding binding;

    private GrowthRecordViewModel growthRecordViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_growth_record);

        growthRecordViewModel = new ViewModelProvider(this).get(GrowthRecordViewModel.class);

        showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color, R.string.growth_record_loading);

        disposable.add(growthRecordViewModel.getAllSemesters().subscribe(aBoolean -> {
            if (aBoolean) {
                initView();
            } else {
                finish();
            }
        }));
    }

    private void initView() {
        // 显示返回键
        NavigationUtils.navigationForToolbar(binding.activityGrowthRecordToolbar, v -> finish());

        // 设置标题为带学号
        binding.activityGrowthRecordToolbar.setTitle(getString(R.string.item_growth_record) + " - " + growthRecordViewModel.getGrowthRecordModel().no);

        binding.setModel(growthRecordViewModel.getGrowthRecordModel());

        // 设置TabLayout的标签值
        List<String> list = new ArrayList<>(growthRecordViewModel.getGrowthRecordModel().semesterModelList.size());
        for (int i = 0; i < growthRecordViewModel.getGrowthRecordModel().semesterModelList.size(); i++) {
            list.add(growthRecordViewModel.getGrowthRecordModel().semesterModelList.get(i).xnxqmc);
        }

        for (String tab : list) {
            binding.activityGrowthRecordTabLayout.addTab(binding.activityGrowthRecordTabLayout.newTab().setText(tab));
        }

        // 初始化ViewPager2
        binding.activityGrowthRecordViewPage2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return SemesterGrowthRecordFragment.getInstance(position);
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        });

        // 与TabLayout进行绑定
        new TabLayoutMediator(binding.activityGrowthRecordTabLayout, binding.activityGrowthRecordViewPage2,
                (tab, position) -> tab.setText(list.get(position))).attach();

        binding.rootView.setVisibility(View.VISIBLE);
        binding.getRoot().post(this::hideLoadingView);
    }

    @Override
    public void setCurrentFragment(BaseBackFragment fragment) {
        this.mCurrentFragment = new WeakReference<>(fragment);
    }

    @Override
    public BaseBackFragment getCurrentFragment() {
        if (mCurrentFragment == null) {
            return null;
        }
        return mCurrentFragment.get();
    }
}