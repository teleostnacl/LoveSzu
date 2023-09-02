package com.teleostnacl.szu.examination.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingDataAdapter;

import com.teleostnacl.common.android.paging.PagingRecyclerView;
import com.teleostnacl.common.android.fragment.BaseDisposableFragment;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.examination.R;
import com.teleostnacl.szu.examination.databinding.ItemExaminationBinding;
import com.teleostnacl.szu.examination.model.ExaminationDataModel;
import com.teleostnacl.szu.examination.model.XNXQModel;
import com.teleostnacl.szu.examination.viewmodel.ExaminationViewModel;

/**
 * 展示各学年学期期末考试安排的Fragment
 */
public class SemesterExaminationFragment extends BaseDisposableFragment {

    private static final String ARG_XNXQ = "1";

    private ExaminationViewModel examinationViewModel;

    private final PagingDataAdapter<ExaminationDataModel, DataBindingVH<ItemExaminationBinding>> pagingDataAdapter =
            new PagingDataAdapter<>(new DefaultItemCallback<>()) {
                @NonNull
                @Override
                public DataBindingVH<ItemExaminationBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    return new DataBindingVH<>(parent, R.layout.item_examination);
                }

                @Override
                public void onBindViewHolder(@NonNull DataBindingVH<ItemExaminationBinding> holder, int position) {
                    holder.binding.setModel(getItem(position));
                }
            };

    private SemesterExaminationFragment() {
    }

    @NonNull
    public static SemesterExaminationFragment getInstance(XNXQModel xnxqModel) {
        SemesterExaminationFragment semesterExaminationFragment = new SemesterExaminationFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_XNXQ, xnxqModel);
        semesterExaminationFragment.setArguments(bundle);
        return semesterExaminationFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        examinationViewModel = new ViewModelProvider(requireActivity()).get(ExaminationViewModel.class);
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

        ((PagingRecyclerView) view).setAdapter(pagingDataAdapter);
        ((PagingRecyclerView) view).getRecyclerView().setNestedScrollingEnabled(false);

        disposable.add(examinationViewModel.getExaminationDataModelsFlowable(
                ((XNXQModel) requireArguments().getParcelable(ARG_XNXQ)).dm).subscribe(
                pagingData -> pagingDataAdapter.submitData(getLifecycle(), pagingData)));
    }
}
