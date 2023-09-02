package com.teleostnacl.szu.paper.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.edittext.RemoveEnterTextWatcher;
import com.teleostnacl.szu.paper.R;
import com.teleostnacl.szu.paper.databinding.FragmentEditPaperCoverBinding;
import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.viewmodel.PaperViewModel;

/**
 * 编辑论文封面的Fragment
 */
public class EditPaperCoverFragment extends BaseBackFragment {
    private FragmentEditPaperCoverBinding binding;

    private PaperViewModel paperViewModel;

    private PaperModel paperModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paperViewModel = new ViewModelProvider(requireActivity()).get(PaperViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_paper_cover, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavigationUtils.navigationForToolbar(binding.fragmentEditPaperCoverToolbar, v -> discard());

        paperModel = new PaperModel(paperViewModel.getPaperModel());
        binding.setPaper(paperModel);
        binding.fragmentEditPaperCoverFinishButton.setOnClickListener(v -> {
            // 更新
            paperViewModel.setPaperModel(paperModel);
            NavigationUtils.popBackStack(v);
        });

        // 限制输入回车符
        binding.fragmentEditPaperCoverTitle.addTextChangedListener(new RemoveEnterTextWatcher());
        binding.fragmentEditPaperCoverAuthor.addTextChangedListener(new RemoveEnterTextWatcher());
        binding.fragmentEditPaperCoverMajor.addTextChangedListener(new RemoveEnterTextWatcher());
        binding.fragmentEditPaperCoverCollege.addTextChangedListener(new RemoveEnterTextWatcher());
        binding.fragmentEditPaperCoverNo.addTextChangedListener(new RemoveEnterTextWatcher());
        binding.fragmentEditPaperCoverTeacher.addTextChangedListener(new RemoveEnterTextWatcher());
        binding.fragmentEditPaperCoverProfessionQualification.addTextChangedListener(new RemoveEnterTextWatcher());
    }

    /**
     * 覆写返回, 询问是否不保存
     */
    @Override
    public boolean onBackPressed() {
        discard();
        return true;
    }

    /**
     * 丢弃修改
     */
    private void discard() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.paper_edit_paper_discard)
                .setPositiveButton(R.string.paper_edit_paper_discard_yes,
                        (dialog, which) -> NavigationUtils.popBackStack(requireView()))
                .setNegativeButton(R.string.paper_edit_paper_discard_no, null)
                .show();
    }
}
