package com.teleostnacl.szu.paper.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ListAdapter;

import com.teleostnacl.common.android.fragment.BaseLogFragment;
import com.teleostnacl.common.android.view.edittext.RemoveEnterTextWatcher;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.paper.R;
import com.teleostnacl.szu.paper.databinding.FragmentEditPaperQuotationBinding;
import com.teleostnacl.szu.paper.databinding.LayoutEditPaperQuotationFragmentQuotationBinding;
import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.model.QuotationModel;
import com.teleostnacl.szu.paper.viewmodel.PaperViewModel;

public class EditPaperQuotationFragment extends BaseLogFragment {

    private PaperViewModel paperViewModel;

    private PaperModel paperModel;

    private final ListAdapter<QuotationModel, DataBindingVH<LayoutEditPaperQuotationFragmentQuotationBinding>> listAdapter = new ListAdapter<>(new DefaultItemCallback<>()) {

        @NonNull
        @Override
        public DataBindingVH<LayoutEditPaperQuotationFragmentQuotationBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DataBindingVH<LayoutEditPaperQuotationFragmentQuotationBinding> VH =
                    new DataBindingVH<>(parent, R.layout.layout_edit_paper_quotation_fragment_quotation);

            // 禁止内容中含有换行符号(换行符自动转为新的段落)
            VH.binding.itemEditPaperContentValue.addTextChangedListener(new RemoveEnterTextWatcher());

            return VH;
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<LayoutEditPaperQuotationFragmentQuotationBinding> holder, int position) {
            QuotationModel model = getItem(position);
            holder.binding.setModel(model);
        }
    };

    private FragmentEditPaperQuotationBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paperViewModel = new ViewModelProvider(requireActivity()).get(PaperViewModel.class);
        paperModel = new PaperModel(paperViewModel.getPaperModel());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_paper_quotation, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.fragmentEditPaperQuotationToolbar.setTitle(getString(R.string.paper_edit_paper_content_edit_quotation_title, paperModel.getTitleOrUnnamed()));
        binding.fragmentEditPaperQuotationRecyclerView.setAdapter(listAdapter);

        listAdapter.submitList(paperModel.quotations);
    }
}
