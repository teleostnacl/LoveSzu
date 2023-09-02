package com.teleostnacl.szu.examination;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.examination.databinding.ActivityExaminationBinding;
import com.teleostnacl.szu.examination.fragment.SemesterExaminationFragment;
import com.teleostnacl.szu.examination.model.XNXQModel;
import com.teleostnacl.szu.examination.viewmodel.ExaminationViewModel;
import com.teleostnacl.szu.libs.activity.BaseLoadingActivity;

import java.util.List;

public class ExaminationActivity extends BaseLoadingActivity {

    private ExaminationViewModel examinationViewModel;

    private ActivityExaminationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_examination);

        examinationViewModel = new ViewModelProvider(this).get(ExaminationViewModel.class);

        showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color, R.string.examination_loading);
        disposable.add(examinationViewModel.init().subscribe(aBoolean -> {
            if (aBoolean) {
                initView();
            } else {
                finish();
            }
        }));
    }

    /**
     * 初始化视图
     */
    private void initView() {
        // 显示返回键
        NavigationUtils.navigationForToolbar(binding.activityExaminationToolbar, v -> finish());

        // 设置标题为带学号
        binding.activityExaminationToolbar.setTitle(getString(R.string.item_examination) + " - " + examinationViewModel.getExaminationModel().no);

        // 设置TabLayout的标签值
        List<XNXQModel> list = examinationViewModel.getExaminationModel().xnxqModels;

        for (XNXQModel ignored : examinationViewModel.getExaminationModel().xnxqModels) {
            binding.activityExaminationTabLayout.addTab(binding.activityExaminationTabLayout.newTab());
        }

        // 初始化ViewPager2
        binding.activityExaminationViewPage2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return SemesterExaminationFragment.getInstance(list.get(position));
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        });

        // 去除边界水波纹效果
        binding.activityExaminationViewPage2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        // 与TabLayout进行绑定
        new TabLayoutMediator(binding.activityExaminationTabLayout, binding.activityExaminationViewPage2,
                (tab, position) -> tab.setText(list.get(position).mc)).attach();

        binding.rootView.setVisibility(View.VISIBLE);
        binding.getRoot().post(this::hideLoadingView);
    }
}