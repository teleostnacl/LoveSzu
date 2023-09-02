package com.teleostnacl.szu.grade;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.grade.databinding.ActivityGradeBinding;
import com.teleostnacl.szu.grade.fragment.SemesterGradeFragment;
import com.teleostnacl.szu.grade.viewmodel.GradeViewModel;
import com.teleostnacl.szu.libs.activity.BaseLoadingActivity;

import java.util.ArrayList;
import java.util.List;

public class GradeActivity extends BaseLoadingActivity {

    private ActivityGradeBinding binding;

    private GradeViewModel gradeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_grade);
        gradeViewModel = new ViewModelProvider(this).get(GradeViewModel.class);

        showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color,
                R.string.grade_activity_loading);

        // 获取成绩
        disposable.add(gradeViewModel.getAllGrade().subscribe(aBoolean -> {
            if (!aBoolean) {
                finish();
            } else {
                binding.getRoot().post(this::initView);
            }
        }));
    }

    /**
     * 初始化视图
     */
    private void initView() {
        // 显示返回键
        NavigationUtils.navigationForToolbar(binding.activityGradeToolbar, v -> finish());

        // 设置标题为带学号
        binding.activityGradeToolbar.setTitle(getString(R.string.item_grade) + " - " + gradeViewModel.getGradesModel().no);

        // 设置TabLayout的标签值
        List<String> list = new ArrayList<>(gradeViewModel.getGradesModel().grades.keySet());
        for (String tab : list) {
            binding.activityGradeTabLayout.addTab(binding.activityGradeTabLayout.newTab().setText(tab));
        }

        // 初始化ViewPager2
        binding.activityGradeViewPage2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return SemesterGradeFragment.getInstance(list.get(position));
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        });

        // 与TabLayout进行绑定
        new TabLayoutMediator(binding.activityGradeTabLayout, binding.activityGradeViewPage2,
                (tab, position) -> tab.setText(list.get(position))).attach();

        binding.rootView.setVisibility(View.VISIBLE);
        binding.getRoot().post(this::hideLoadingView);
    }
}