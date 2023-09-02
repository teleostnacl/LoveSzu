package com.teleostnacl.szu.paper.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ListAdapter;

import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.edittext.RemoveEnterTextWatcher;
import com.teleostnacl.common.android.view.recyclerview.ClickedOnceListAdapter;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.paper.R;
import com.teleostnacl.szu.paper.databinding.FragmentEditPaperBasicInformationBinding;
import com.teleostnacl.szu.paper.databinding.ItemEditPaperBasicInformationKeywordBinding;
import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.viewmodel.PaperViewModel;

/**
 * 编辑论文基本信息(摘要 关键词 英文摘要 英文关键词)的Fragment
 */
public class EditPaperBasicInformationFragment extends BaseBackFragment {
    private FragmentEditPaperBasicInformationBinding binding;

    private PaperViewModel paperViewModel;

    private PaperModel paperModel;

    /**
     * 显示关键词用的Keywords
     */
    private final ListAdapter<String, DataBindingVH<ItemEditPaperBasicInformationKeywordBinding>> listAdapter =
            new ClickedOnceListAdapter<>(new DefaultItemCallback<>() {
                @Override
                public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                    // 当字符串为空时 快速返回非相同项
                    if (TextUtils.isEmpty(oldItem) || TextUtils.isEmpty(newItem)) {
                        return false;
                    }
                    return super.areContentsTheSame(oldItem, newItem);
                }
            }) {
                @Override
                public void onClick(@NonNull View v) {
                    // 获取是否为英文关键字
                    boolean isEN = (boolean) ((View) v.getParent()).findViewById(R.id.item_edit_paper_basic_information_keyword).getTag();
                    // 获取在关键字List的位置
                    int position = (int) ((View) v.getParent()).findViewById(R.id.item_edit_paper_basic_information_keyword_edit_text).getTag();

                    String keyword = isEN ? paperModel.keywordsEn.get(position) : paperModel.keywords.get(position);

                    // 关键词为空 则直接删除
                    if (TextUtils.isEmpty(keyword)) {
                        deleteKeywords(isEN, position);
                    } else {
                        new AlertDialog.Builder(requireContext())
                                .setTitle(getString(isEN ? R.string.paper_edit_paper_basic_information_delete_keyword_en : R.string.paper_edit_paper_basic_information_delete_keyword, position + 1))
                                .setPositiveButton(com.teleostnacl.common.android.R.string.delete, (dialog, which) -> {
                                    deleteKeywords(isEN, position);
                                })
                                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                                .show();
                    }
                }

                /**
                 * 删除指定位置的关键词
                 */
                private void deleteKeywords(boolean isEN, int position) {
                    if (isEN) {
                        paperModel.keywordsEn.remove(position);
                    } else {
                        paperModel.keywords.remove(position);
                    }
                    submitList(paperModel.getKeywordList());
                }

                /**
                 * 设置两次点击间隔时间为300ms
                 */
                @Override
                protected int getClickGap() {
                    return 300;
                }

                @NonNull
                @Override
                public DataBindingVH<ItemEditPaperBasicInformationKeywordBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    DataBindingVH<ItemEditPaperBasicInformationKeywordBinding> VH = new DataBindingVH<>(parent, R.layout.item_edit_paper_basic_information_keyword);

                    // 修改关键词
                    VH.binding.itemEditPaperBasicInformationKeywordEditText.addTextChangedListener(new RemoveEnterTextWatcher() {
                        @Override
                        public void afterTextChanged(Editable s) {
                            super.afterTextChanged(s);

                            // 获取是否为英文关键字
                            boolean isEN = (boolean) VH.binding.itemEditPaperBasicInformationKeyword.getTag();
                            // 获取在关键字List的位置
                            int position = (int) VH.binding.itemEditPaperBasicInformationKeywordEditText.getTag();

                            if (isEN) {
                                paperModel.keywordsEn.set(position, s.toString());
                            } else {
                                paperModel.keywords.set(position, s.toString());
                            }
                        }
                    });

                    setOnClickListener(VH.binding.itemEditPaperBasicInformationKeywordDelete);

                    return VH;
                }

                @Override
                public void onBindViewHolder(@NonNull DataBindingVH<ItemEditPaperBasicInformationKeywordBinding> holder,
                                             int position) {
                    // position 减去中文关键词的大小之后 若pos大于0 则表示此关键词是英文关键词
                    int pos = position - paperModel.keywords.size();
                    boolean isEN = pos >= 0;
                    pos = isEN ? pos : position;
                    holder.binding.itemEditPaperBasicInformationKeyword.setTag(isEN);
                    holder.binding.itemEditPaperBasicInformationKeywordEditText.setTag(pos);

                    holder.binding.itemEditPaperBasicInformationKeyword.setHint(getString(isEN ? R.string.paper_edit_paper_basic_information_keywords_en : R.string.paper_edit_paper_basic_information_keywords, pos + 1));
                    holder.binding.itemEditPaperBasicInformationKeywordEditText.setText(isEN ? paperModel.keywordsEn.get(pos) : paperModel.keywords.get(pos));
                }
            };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paperViewModel = new ViewModelProvider(requireActivity()).get(PaperViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_paper_basic_information, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavigationUtils.navigationForToolbar(binding.fragmentEditPaperBasicInformationToolbar, v -> discard());

        paperModel = new PaperModel(paperViewModel.getPaperModel());
        binding.setPaper(paperModel);
        binding.fragmentEditPaperBasicInformationFinishButton.setOnClickListener(v -> {
            // 更新
            paperViewModel.setPaperModel(paperModel);
            NavigationUtils.popBackStack(v);
        });

        binding.fragmentEditPaperBasicInformationTitleEn.addTextChangedListener(new RemoveEnterTextWatcher());

        initKeywordsView();
    }

    /**
     * 初始化显示关键词的View
     */
    private void initKeywordsView() {
        binding.fragmentEditPaperBasicInformationKeywords.setAdapter(listAdapter);
        binding.fragmentEditPaperBasicInformationKeywords.setItemAnimator(null);
        listAdapter.submitList(paperModel.getKeywordList());

        // 增加关键词
        binding.fragmentEditPaperBasicInformationAddKeywordsButton.setOnClickListener(v -> {
            paperModel.keywords.add("");
            paperModel.keywordsEn.add("");
            listAdapter.submitList(paperModel.getKeywordList());
        });
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
