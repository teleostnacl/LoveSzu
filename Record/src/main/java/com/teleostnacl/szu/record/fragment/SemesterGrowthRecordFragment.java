package com.teleostnacl.szu.record.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingDataAdapter;

import com.teleostnacl.common.android.paging.PagingRecyclerView;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.record.R;
import com.teleostnacl.szu.record.databinding.LayoutSemesterGrowthRecordBinding;
import com.teleostnacl.szu.record.model.SemesterModel;
import com.teleostnacl.szu.record.viewmodel.GrowthRecordViewModel;

public class SemesterGrowthRecordFragment extends BaseLoadingFragment {

    private static final String ARG_INDEX = "1";

    private SemesterModel semesterModel;

    private GrowthRecordViewModel growthRecordViewModel;

    private final SemesterGrowthRecordAdapter adapter = new SemesterGrowthRecordAdapter();

    private SemesterGrowthRecordFragment() {
    }

    @NonNull
    public static SemesterGrowthRecordFragment getInstance(int index) {
        SemesterGrowthRecordFragment fragment = new SemesterGrowthRecordFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_INDEX, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        growthRecordViewModel = new ViewModelProvider(requireActivity()).get(GrowthRecordViewModel.class);

        semesterModel = growthRecordViewModel.getGrowthRecordModel().semesterModelList.get(
                requireArguments().getInt(ARG_INDEX));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return new PagingRecyclerView(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PagingRecyclerView recyclerView = (PagingRecyclerView) view;

        recyclerView.setAdapter(adapter);
        recyclerView.setSwipeRefreshEnabled(false);
        disposable.add(growthRecordViewModel.getSemesterGrowthRecord(semesterModel).subscribe(
                pagingData -> adapter.submitData(this.getLifecycle(), pagingData)));
    }

    /**
     * 展示成长记录的Adapter
     */
    private class SemesterGrowthRecordAdapter extends PagingDataAdapter<SemesterModel, DataBindingVH<LayoutSemesterGrowthRecordBinding>> {

        public SemesterGrowthRecordAdapter() {
            super(new DefaultItemCallback<>());
        }

        @NonNull
        @Override
        public DataBindingVH<LayoutSemesterGrowthRecordBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DataBindingVH<>(parent, R.layout.layout_semester_growth_record);
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<LayoutSemesterGrowthRecordBinding> holder, int position) {
            holder.binding.setModel(getItem(position));
        }
    }
}
