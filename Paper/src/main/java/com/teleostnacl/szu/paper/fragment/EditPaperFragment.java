package com.teleostnacl.szu.paper.fragment;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseLogFragment;
import com.teleostnacl.common.java.util.ExecutorServiceUtils;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.paper.R;
import com.teleostnacl.szu.paper.databinding.FragmentEditPaperBinding;
import com.teleostnacl.szu.paper.helper.PaperHelper;
import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.viewmodel.PaperViewModel;

import java.io.File;

/**
 * 编辑论文的Fragment
 */
public class EditPaperFragment extends BaseLogFragment {

    private static final String EXPORT_FOLDER = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + File.separator + "SzuPaper";

    private FragmentEditPaperBinding binding;

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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_paper, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        paperModel = paperViewModel.getPaperModel();

        NavigationUtils.navPopBackForToolbar(binding.fragmentEditPaperToolbar);
        binding.fragmentEditPaperToolbar.setTitle(getString(R.string.paper_edit_paper_title, paperModel.getTitleOrUnnamed()));

        binding.setModel(paperModel);

        binding.fragmentEditPaperEditPaperCover.setOnClickListener(v ->
                NavigationUtils.navigate(v, R.id.action_editPaperFragment_to_editPaperCoverFragment));
        binding.fragmentEditPaperEditPaperBasicInformation.setOnClickListener(v ->
                NavigationUtils.navigate(v, R.id.action_editPaperFragment_to_editPaperBasicInformation));
        binding.fragmentEditPaperEditContent.setOnClickListener(v ->
                NavigationUtils.navigate(v, R.id.action_editPaperFragment_to_editPaperContentFragment));
        binding.fragmentEditPaperEditQuotation.setOnClickListener(v ->
                NavigationUtils.navigate(v, R.id.action_editPaperFragment_to_editPaperQuotationFragment));

        // 设置切换自动生成图片图例的编号
        binding.fragmentEditPaperAutoGeneratePictureNo.setOnClickListener(v ->
                paperModel.setAutoGeneratePictureNo(!paperModel.isAutoGeneratePictureNo()));

        // 导出论文
        binding.fragmentEditPaperExport.setOnClickListener(v -> {
            paperModel.setCreateSuccess(false);

            paperModel.file = new File(EXPORT_FOLDER, paperModel.getTitleOrUnnamed() + ".docx");

            ToastUtils.makeLongToast(R.string.paper_edit_paper_export_path, paperModel.file.getAbsolutePath());
            ExecutorServiceUtils.getCachedExecutorService().submit(() -> {
                        boolean success = PaperHelper.createPaper(paperModel);
                        paperModel.setCreateSuccess(success);
                        ToastUtils.makeToast(success ? R.string.paper_edit_paper_export_success : R.string.paper_edit_paper_export_failed);
                    }
            );
        });

        // 分享论文
        binding.fragmentEditPaperShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            Uri contentUri = FileProvider.getUriForFile(requireContext(),
                    "com.teleostnacl.szu.paper.fileprovider", paperModel.file);
            share.putExtra(Intent.EXTRA_STREAM, contentUri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            ContextUtils.startActivity(Intent.createChooser(share, getString(R.string.paper_edit_paper_share_title, paperModel.getTitleOrUnnamed())));
        });
    }
}
