package com.teleostnacl.szu.grade.fragment;

import static com.teleostnacl.szu.grade.viewmodel.GradeViewModel.GRADE_AVERAGE_VIEW_TYPE;
import static com.teleostnacl.szu.grade.viewmodel.GradeViewModel.GRADE_VIEW_TYPE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.teleostnacl.common.android.fragment.BaseLogFragment;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.grade.R;
import com.teleostnacl.szu.grade.databinding.LayoutItemGradeAverageBinding;
import com.teleostnacl.szu.grade.databinding.LayoutItemGradeBinding;
import com.teleostnacl.szu.grade.model.GradeModel;
import com.teleostnacl.szu.grade.model.SemesterGradesModel;
import com.teleostnacl.szu.grade.viewmodel.GradeViewModel;

/**
 * 展示学期的成绩
 */
public class SemesterGradeFragment extends BaseLogFragment {

    public static final String ARG_SEMESTER = "1";

    private GradeViewModel gradeViewModel;

    // 记录当前显示的学期
    private SemesterGradesModel model;

    private final ListAdapter<GradeModel, DataBindingVH<?>> listAdapter = new ListAdapter<>(new DefaultItemCallback<>()) {
        @NonNull
        @Override
        public DataBindingVH<?> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == GRADE_AVERAGE_VIEW_TYPE) {
                return new DataBindingVH<>(parent, R.layout.layout_item_grade_average);
            }

            return new DataBindingVH<>(parent, R.layout.layout_item_grade);
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<?> holder, int position) {
            if (holder.binding instanceof LayoutItemGradeAverageBinding) {
                ((LayoutItemGradeAverageBinding) holder.binding).setGradeModel(model);
            } else if (holder.binding instanceof LayoutItemGradeBinding) {
                ((LayoutItemGradeBinding) holder.binding).setGradeModel(getItem(position - 1));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return GRADE_AVERAGE_VIEW_TYPE;
            }
            return GRADE_VIEW_TYPE;
        }

        @Override
        public int getItemCount() {
            return super.getItemCount() + 1;
        }
    };

    private SemesterGradeFragment() {
    }

    @NonNull
    public static SemesterGradeFragment getInstance(String semester) {
        SemesterGradeFragment semesterGradeFragment = new SemesterGradeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SEMESTER, semester);
        semesterGradeFragment.setArguments(bundle);

        return semesterGradeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gradeViewModel = new ViewModelProvider(requireActivity()).get(GradeViewModel.class);
        model = gradeViewModel.getGradesModel().grades.get(requireArguments().getString(ARG_SEMESTER));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return new RecyclerView(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view;

        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setRecycledViewPool(gradeViewModel.recyclerViewPool);
        recyclerView.setAdapter(listAdapter);

        // 显示该学期的成绩
        listAdapter.submitList(model.gradeModelList);
    }
}
