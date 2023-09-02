package com.teleostnacl.szu.timetable.fragment;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.fragment.BaseLogFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.timetable.R;
import com.teleostnacl.szu.timetable.databinding.FragmentEditTimetableBinding;
import com.teleostnacl.szu.timetable.model.Timetable;
import com.teleostnacl.szu.timetable.viewmodel.TimetableViewModel;

public class EditTimetableFragment extends BaseLogFragment {

    private FragmentEditTimetableBinding binding;

    // 修改或新建的Timetable
    private Timetable timetable;

    private TimetableViewModel timetableViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timetableViewModel = new ViewModelProvider(requireActivity()).get(TimetableViewModel.class);

        timetable = timetableViewModel.getCurrentModifyTimetable();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_timetable, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (timetable.timetableName == null || timetable.timetableName.equals("")) {
            binding.fragmentEditTimetableToolbar.setTitle(R.string.timetable_edit_timetable_title_new);
        } else {
            binding.fragmentEditTimetableToolbar.setTitle(R.string.timetable_edit_timetable_title);

            // 显示删除按钮
            binding.editTimetableDeleteTimetableButton.setVisibility(View.VISIBLE);

            // 删除按钮
            binding.editTimetableDeleteTimetableButton.setOnClickListener(v ->
                    new AlertDialog.Builder(requireContext())
                            .setMessage(R.string.timetable_edit_timetable_delete_timetable_sure)
                            .setPositiveButton(com.teleostnacl.common.android.R.string.delete, (dialog, which) -> {
                                timetableViewModel.deleteTimetable(timetable);
                                NavigationUtils.popBackStack(requireView());
                            })
                            .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                            .show());
        }

        NavigationUtils.navPopBackForToolbar(binding.fragmentEditTimetableToolbar);

        binding.setTimetable(timetable);
        if (!timetable.checkTime()) {
            // 重新设置上下课时间
            timetable.resetStartAndEndTime();
        } else {
            timetable.submitLessonModel();
        }

        // 设置自动计算上下课时间的CheckBox切换事件,使用动画显示或者隐藏设置单节课和课间休息一般时长
        binding.itemEditTimetablePeriodFrameLayout.post(() -> {
            int itemEditTimetablePeriodFrameLayoutHeight =
                    binding.itemEditTimetablePeriodFrameLayout.getMeasuredHeight();
            ViewGroup.LayoutParams layoutParams = binding.itemEditTimetablePeriodFrameLayout.getLayoutParams();

            binding.itemEditTimetablePeriodFrameLayout.setPivotX(0);
            binding.itemEditTimetablePeriodFrameLayout.setPivotY(0);

            binding.itemEditTimetableAutoSetTimeSet.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ValueAnimator objectAnimator;
                if (isChecked) {
                    objectAnimator = ValueAnimator.ofInt(
                            0, itemEditTimetablePeriodFrameLayoutHeight);
                } else {
                    objectAnimator = ValueAnimator.ofInt(
                            itemEditTimetablePeriodFrameLayoutHeight, 0);
                }

                objectAnimator.setDuration(256);
                objectAnimator.setInterpolator(new DecelerateInterpolator());
                objectAnimator.addUpdateListener(animation -> {
                    layoutParams.height = (int) animation.getAnimatedValue();
                    binding.itemEditTimetablePeriodFrameLayout.requestLayout();
                });

                objectAnimator.start();
            });
        });

        // 保存操作
        binding.save.setOnClickListener(v -> {
            if (timetable.timetableName == null || timetable.timetableName.equals("")) {
                Toast.makeText(requireContext(), R.string.timetable_edit_timetable_need_name, Toast.LENGTH_SHORT).show();
                return;
            }

            timetableViewModel.saveTimetable(timetable);
            NavigationUtils.popBackStack(requireView());
        });

        // 是否默认显示
        binding.itemEditTimetableShowDefaultSet.setChecked(timetableViewModel.isTimetableDefault(timetable));
        binding.itemEditTimetableShowDefaultSet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                timetableViewModel.setTimetableDefault(timetable);
            }
        });
    }

}
