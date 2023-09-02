package com.teleostnacl.szu.paper.fragment;

import static com.teleostnacl.szu.paper.model.BaseContentModel.CONTENT;
import static com.teleostnacl.szu.paper.model.BaseContentModel.FIRST_TITLE;
import static com.teleostnacl.szu.paper.model.BaseContentModel.PICTURE;
import static com.teleostnacl.szu.paper.model.BaseContentModel.SECOND_TITLE;
import static com.teleostnacl.szu.paper.model.BaseContentModel.THIRD_TITLE;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseBackFragment;
import com.teleostnacl.common.java.util.ExecutorServiceUtils;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.edittext.RemoveEnterTextWatcher;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.paper.R;
import com.teleostnacl.szu.paper.databinding.FragmentEditPaperContentBinding;
import com.teleostnacl.szu.paper.databinding.LayoutEditPaperContentPictureBinding;
import com.teleostnacl.szu.paper.databinding.LayoutEditPaperContentStringBinding;
import com.teleostnacl.szu.paper.model.BaseContentModel;
import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.model.PictureModel;
import com.teleostnacl.szu.paper.model.QuotationModel;
import com.teleostnacl.szu.paper.model.StringModel;
import com.teleostnacl.szu.paper.view.edittext.EditContentSimpleTextEditText;
import com.teleostnacl.szu.paper.view.edittext.spannable.QuotationSpannable;
import com.teleostnacl.szu.paper.viewmodel.PaperViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 编辑论文内容的Fragment
 */
public class EditPaperContentFragment extends BaseBackFragment {
    private FragmentEditPaperContentBinding binding;

    private PaperViewModel paperViewModel;

    private PaperModel paperModel;

    // 正在调用照片选择器设置的PictureModel
    public PictureModel pictureModel;

    private final ListAdapter<BaseContentModel, DataBindingVH<?>> listAdapter = new ListAdapter<>(new DefaultItemCallback<>()) {

        // 记录所需获取焦点的位置(用于新增 移除正文内容时 将焦点给予正确的视图)
        private int currentFocusIndex = -1;

        @NonNull
        @Override
        public DataBindingVH<?> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                // 一级 二级 三级标题
                case FIRST_TITLE:
                case SECOND_TITLE:
                case THIRD_TITLE:
                case CONTENT: {
                    DataBindingVH<LayoutEditPaperContentStringBinding> VH =
                            new DataBindingVH<>(parent, R.layout.layout_edit_paper_content_string);

                    // 设置关联View
                    VH.binding.itemEditPaperContentValue.setRelationViews(
                            binding.fragmentEditPaperContentBottomTool,
                            binding.fragmentEditPaperContentLeftIndentation,
                            binding.fragmentEditPaperContentRightIndentation,
                            binding.fragmentEditPaperContentPicture,
                            binding.fragmentEditPaperContentQuote);

                    // 禁止内容中含有换行符号(换行符自动转为新的段落)
                    VH.binding.itemEditPaperContentValue.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String s1 = s.toString();

                            // 仅有回车符 直接删除
                            if (s1.equals("\r\n") || s1.equals("\r") || s1.equals("\n")) {
                                s.clear();
                                return;
                            }

                            // 分割回车符
                            if (s1.contains("\r\n") || s1.contains("\r") || s1.contains("\n")) {

                                // 在回车符处进行分割
                                String[] strings = s1.split("\r\n|\r|\n");

                                if (strings.length > 0) {
                                    // 首个回车符之后的内容去掉
                                    s.replace(strings[0].length(), s.length(), "");

                                    // 获取当前的索引值
                                    StringModel stringModel = VH.binding.getModel();
                                    int index = getCurrentList().indexOf(stringModel);

                                    // 将剩下的另起新段落
                                    for (int i = 1; i < strings.length; i++) {
                                        StringModel newStringModel;
                                        // 根据正则表达式 划分级别
                                        if (strings[i].matches(StringModel.FIRST_TITLE_REGEX)) {
                                            newStringModel = new StringModel(FIRST_TITLE, strings[i]);
                                        } else if (strings[i].matches(StringModel.SECOND_TITLE_REGEX)) {
                                            newStringModel = new StringModel(SECOND_TITLE, strings[i]);
                                        } else if (strings[i].matches(StringModel.THIRD_TITLE_REGEX)) {
                                            newStringModel = new StringModel(THIRD_TITLE, strings[i]);
                                        } else {
                                            newStringModel = new StringModel(CONTENT, strings[i]);
                                        }

                                        paperModel.addContent(index + i, newStringModel);
                                    }

                                    // 更新
                                    listAdapter.submitList(paperModel.getContents());
                                }

                                return;
                            }

                            // 删除时遇到引用的Span, 需整体删除
                            if (!(s instanceof SpannableStringBuilder)) {
                                return;
                            }
                            deleteQuotation((SpannableStringBuilder) s);
                        }

                        private void deleteQuotation(SpannableStringBuilder spannableStringBuilder) {
                            // 获取当前光标开始和结束的位置
                            int selectionStartIndex = Selection.getSelectionStart(spannableStringBuilder);
                            int selectionEndIndex = Selection.getSelectionEnd(spannableStringBuilder);
                            // 获取在光标处前是否有span
                            QuotationSpannable[] spans = spannableStringBuilder.getSpans(selectionStartIndex, selectionEndIndex, QuotationSpannable.class);
                            // 无span 不进行处理
                            if (spans == null || spans.length == 0 || spans[0] == null) {
                                return;
                            }

                            // span的尺寸不等于QUOTATION_PLACE_HOLDER_LENGTH, 则表示该Span不完整 移出
                            int startIndex = spannableStringBuilder.getSpanStart(spans[0]);
                            int endIndex = spannableStringBuilder.getSpanEnd(spans[0]);
                            if (endIndex - startIndex != StringModel.QUOTATION_PLACE_HOLDER_LENGTH) {
                                spans[0].removeQuotation(spannableStringBuilder);
                                spannableStringBuilder.replace(startIndex, endIndex, "");
                                paperModel.quotations.remove(spans[0].quotationModel);
                            }
                        }
                    });
                    // 监听按键
                    VH.binding.itemEditPaperContentValue.setOnKeyListener((v, keyCode, event) -> {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            switch (keyCode) {
                                // 回车键 新增新的Content
                                case KeyEvent.KEYCODE_ENTER: {
                                    // 清除焦点
                                    v.clearFocus();

                                    StringModel stringModel = VH.binding.getModel();
                                    int index = getCurrentList().indexOf(stringModel);
                                    // 设置获取焦点的位置
                                    currentFocusIndex = index + 1;

                                    // 在指定位置新增新的Model
                                    paperModel.addContent(index + 1, new StringModel(stringModel.getType()));
                                    listAdapter.submitList(paperModel.getContents(),
                                            // 更新Model之后 在指定位置获取焦点
                                            () -> {
                                                if (currentFocusIndex != -1) {
                                                    binding.fragmentEditPaperContentRecyclerView.scrollToPosition(currentFocusIndex);
                                                    notifyItemChanged(currentFocusIndex);
                                                }
                                            });
                                    return true;
                                }

                                // backspace键, 当文本为空时 删除整个文本框
                                case KeyEvent.KEYCODE_DEL:
                                case KeyEvent.KEYCODE_FORWARD_DEL: {
                                    StringModel stringModel = VH.binding.getModel();
                                    // 当前内容已经为空 则删除该Model
                                    if ("".equals(stringModel.getContentSpannable().toString())) {
                                        // 清除焦点
                                        v.clearFocus();

                                        int index = getCurrentList().indexOf(stringModel);
                                        // 设置获取焦点的位置
                                        currentFocusIndex = index - 1;
                                        if (currentFocusIndex < 0) {
                                            currentFocusIndex = 0;
                                        }

                                        paperModel.removeContent(stringModel);
                                        listAdapter.submitList(paperModel.getContents(),
                                                // 更新Model之后 在指定位置获取焦点
                                                () -> {
                                                    if (currentFocusIndex != -1) {
                                                        notifyItemChanged(currentFocusIndex);
                                                        binding.fragmentEditPaperContentRecyclerView.scrollToPosition(currentFocusIndex);
                                                    }
                                                });
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
                    });

                    // 监听焦点变化
                    VH.binding.itemEditPaperContentValue.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            // 使用post可以保证已经onBindViewHolder了, VH.binding.getModel()不为空
                            v.post(() -> {
                                StringModel stringModel = VH.binding.getModel();
                                binding.fragmentEditPaperContentBottomTool.setTag(stringModel);
                                binding.fragmentEditPaperContentBottomTool.setVisibility(View.VISIBLE);
                            });
                        }
                    });

                    return VH;
                }


                case PICTURE: {
                    DataBindingVH<LayoutEditPaperContentPictureBinding> VH =
                            new DataBindingVH<>(parent, R.layout.layout_edit_paper_content_picture);

                    // 禁止内容中含有换行符号(换行符自动转为新的段落)
                    VH.binding.layoutEditPaperContentPictureEditText.addTextChangedListener(new RemoveEnterTextWatcher());
                    // 监听按键
                    VH.binding.layoutEditPaperContentPictureEditText.setOnKeyListener((v, keyCode, event) -> {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            switch (keyCode) {
                                // 回车键 新增新的Content
                                case KeyEvent.KEYCODE_ENTER: {
                                    // 清除焦点
                                    v.clearFocus();

                                    PictureModel model = VH.binding.getModel();
                                    int index = getCurrentList().indexOf(model);
                                    // 设置获取焦点的位置
                                    currentFocusIndex = index + 1;

                                    // 在指定位置新增新的Model
                                    paperModel.addContent(index + 1, new StringModel(FIRST_TITLE));
                                    listAdapter.submitList(paperModel.getContents(),
                                            // 更新Model之后 在指定位置获取焦点
                                            () -> {
                                                if (currentFocusIndex != -1) {
                                                    binding.fragmentEditPaperContentRecyclerView.scrollToPosition(currentFocusIndex);
                                                    notifyItemChanged(currentFocusIndex);
                                                }
                                            });
                                    return true;
                                }

                                // backspace键, 当文本为空时 删除整个文本框
                                case KeyEvent.KEYCODE_DEL: {
                                    PictureModel model = VH.binding.getModel();
                                    // 当前内容已经为空 则删除该Model
                                    if ("".equals(model.pictureName)) {
                                        // 清除焦点
                                        v.clearFocus();

                                        int index = getCurrentList().indexOf(model);
                                        // 设置获取焦点的位置
                                        currentFocusIndex = index - 1;
                                        if (currentFocusIndex < 0) {
                                            currentFocusIndex = 0;
                                        }

                                        paperModel.removeContent(model);
                                        listAdapter.submitList(paperModel.getContents(),
                                                // 更新Model之后 在指定位置获取焦点
                                                () -> {
                                                    if (currentFocusIndex != -1) {
                                                        binding.fragmentEditPaperContentRecyclerView.scrollToPosition(currentFocusIndex);
                                                        notifyItemChanged(currentFocusIndex);
                                                    }
                                                });
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
                    });

                    // 宽度设置为屏幕的一半
                    VH.binding.layoutEditPaperContentPictureImageView.getLayoutParams().width =
                            (int) (ResourcesUtils.getWidthPixels() / 2);

                    VH.binding.layoutEditPaperContentPictureImageView.setOnClickListener(v -> {
                        // 启动图片选择器
                        pictureModel = VH.binding.getModel();
                        pickMedia.launch("image/*");
                        ToastUtils.makeToast(R.string.paper_edit_paper_content_choose_picture);
                    });

                    return VH;
                }
            }

            throw new IllegalArgumentException();
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<?> holder, int position) {
            // 获取焦点
            if (position == currentFocusIndex) {
                holder.itemView.requestFocus();
                holder.itemView.requestFocusFromTouch();
                currentFocusIndex = -1;
            }

            if (position == super.getItemCount()) {
                return;
            }

            BaseContentModel model = getItem(position);

            // 文本段
            if (holder.binding instanceof LayoutEditPaperContentStringBinding && model instanceof StringModel) {
                LayoutEditPaperContentStringBinding holderBinding = (LayoutEditPaperContentStringBinding) holder.binding;
                holderBinding.setModel((StringModel) model);

                // 给EditText 设置SpannableStringBuilder
                holderBinding.itemEditPaperContentValue.setText(((StringModel) model).getContentSpannable());
                Editable editable = holderBinding.itemEditPaperContentValue.getText();
                if (editable instanceof SpannableStringBuilder) {
                    ((StringModel) model).setContentSpannable((SpannableStringBuilder) editable);
                }

            } else if (holder.binding instanceof LayoutEditPaperContentPictureBinding && model instanceof PictureModel) {
                LayoutEditPaperContentPictureBinding holderBinding = (LayoutEditPaperContentPictureBinding) holder.binding;
                holderBinding.setModel((PictureModel) model);
                Glide.with(EditPaperContentFragment.this)
                        .load(((PictureModel) model).filePath)
                        // 使用原尺寸进行加载
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .placeholder(R.drawable.drawable_edit_paper_content_picture_placeholder)
                        .into(holderBinding.layoutEditPaperContentPictureImageView);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getType();
        }
    };

    private ActivityResultLauncher<String> pickMedia;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paperViewModel = new ViewModelProvider(requireActivity()).get(PaperViewModel.class);

        pickMedia = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                if (pictureModel == null) {
                    return;
                }

                ExecutorServiceUtils.executeByCached(() -> {
                    String[] tmp = uri.getLastPathSegment().split(File.separator);
                    if (tmp.length == 0) {
                        ToastUtils.makeToast(R.string.paper_edit_paper_content_choose_picture_empty);
                        return;
                    }
                    // 设置文件路径
                    String filePath = paperModel.getPicturePath(tmp[tmp.length - 1]);

                    // 将图片复制到论文的临时文件夹中
                    try (AssetFileDescriptor afd = ContextUtils.getContext().getContentResolver().openAssetFileDescriptor(uri, "r");
                         FileInputStream fis = afd.createInputStream();
                         FileOutputStream fos = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[16 * 1024];
                        int size;
                        while ((size = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, size);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.makeToast(R.string.paper_edit_paper_content_choose_picture_empty);
                        return;
                    }

                    pictureModel.setFilePath(filePath);
                    binding.fragmentEditPaperContentRecyclerView.post(() -> {
                        listAdapter.notifyItemChanged(listAdapter.getCurrentList().indexOf(pictureModel));
                        pictureModel = null;
                    });
                });
            } else {
                ToastUtils.makeToast(R.string.paper_edit_paper_content_choose_picture_empty);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_paper_content, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        paperModel = new PaperModel(paperViewModel.getPaperModel());

        binding.fragmentEditPaperContentToolbar.setTitle(getString(R.string.paper_edit_paper_content_fragment_title, paperModel.getTitleOrUnnamed()));
        NavigationUtils.navigationForToolbar(binding.fragmentEditPaperContentToolbar, v -> discard());

        binding.fragmentEditPaperContentToolbar.inflateMenu(R.menu.menu_paper_edit_conetent);
        binding.fragmentEditPaperContentToolbar.setOnMenuItemClickListener(item -> {
            // 帮助
            if (item.getItemId() == R.id.paper_edit_content_menu_help) {
                ToastUtils.makeLongToast(R.string.paper_edit_paper_content_tips);
                return true;
            }

            // 保存
            if (item.getItemId() == R.id.paper_edit_content_menu_save) {
                // 更新
                paperModel.updateContents();
                paperViewModel.setPaperModel(paperModel);
                NavigationUtils.popBackStack(binding.fragmentEditPaperContentToolbar);
                return true;
            }

            return false;
        });

        initRecyclerView();

        initBottomTool();
    }

    private void initRecyclerView() {
        binding.fragmentEditPaperContentRecyclerView.setAdapter(listAdapter);
        listAdapter.submitList(paperModel.getContents());
        // 修改默认动画的持续时间(修改为原来的五分之一)
        RecyclerView.ItemAnimator animator = binding.fragmentEditPaperContentRecyclerView.getItemAnimator();
        if (animator != null) {
            int times = 5;
            animator.setAddDuration(animator.getAddDuration() / times);
            animator.setChangeDuration(animator.getChangeDuration() / times);
            animator.setMoveDuration(animator.getMoveDuration() / times);
            animator.setRemoveDuration(animator.getRemoveDuration() / times);
        }
    }

    private void initBottomTool() {
        // 监听焦点变化 控制显示底部工具栏
        binding.fragmentEditPaperContentRecyclerView.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            private int i = 0;

            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                // 记录当前的标记值, 焦点改变一次+1
                int finalI = ++i;
                binding.fragmentEditPaperContentRecyclerView.postDelayed(() -> {
                    // 延迟执行, 如果在此期间焦点有所改变 则不再进行操作
                    if (finalI == i) {
                        if (binding.fragmentEditPaperContentRecyclerView.findFocus() instanceof EditContentSimpleTextEditText) {
                            binding.fragmentEditPaperContentBottomTool.setVisibility(View.VISIBLE);
                        } else {
                            binding.fragmentEditPaperContentBottomTool.setVisibility(View.GONE);
                        }
                    }
                }, 100);
            }
        });

        // 减少级别
        binding.fragmentEditPaperContentLeftIndentation.setOnClickListener(v -> {
            BaseContentModel baseContentModel = (BaseContentModel) binding.fragmentEditPaperContentBottomTool.getTag();

            // 非文本段类型 不执行
            if (!(baseContentModel instanceof StringModel)) {
                return;
            }

            // 减少级别
            ((StringModel) baseContentModel).reduceLevel();
            listAdapter.notifyItemChanged(listAdapter.getCurrentList().indexOf(baseContentModel));
        });

        // 增加级别
        binding.fragmentEditPaperContentRightIndentation.setOnClickListener(v -> {
            BaseContentModel baseContentModel = (BaseContentModel) binding.fragmentEditPaperContentBottomTool.getTag();
            // 非文本段类型 不执行
            if (!(baseContentModel instanceof StringModel)) {
                return;
            }

            // 增加级别
            ((StringModel) baseContentModel).addLevel();
            listAdapter.notifyItemChanged(listAdapter.getCurrentList().indexOf(baseContentModel));
        });

        // 文本转图片
        binding.fragmentEditPaperContentPicture.setOnClickListener(v -> {
            BaseContentModel baseContentModel = (BaseContentModel) binding.fragmentEditPaperContentBottomTool.getTag();

            // 非文本段类型 不执行
            if (!(baseContentModel instanceof StringModel)) {
                return;
            }

            // 为空文本 则直接转换
            if (TextUtils.isEmpty(((StringModel) baseContentModel).getContentSpannable())) {
                insertPicture(listAdapter.getCurrentList().indexOf(baseContentModel));
                return;
            }

            // 非空文本 弹Dialog询问是否转图片
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.paper_edit_paper_content_covert_to_picture)
                    .setPositiveButton(com.teleostnacl.common.android.R.string.yes, (dialog, which) ->
                            insertPicture(listAdapter.getCurrentList().indexOf(baseContentModel)))
                    .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                    .show();
        });

        // 添加引用
        binding.fragmentEditPaperContentQuote.setOnClickListener(v -> {
            BaseContentModel baseContentModel = (BaseContentModel) binding.fragmentEditPaperContentBottomTool.getTag();

            // 非文本段类型 不执行
            if (!(baseContentModel instanceof StringModel)) {
                return;
            }

            // 查找当前获焦的EditText
            View focusView = binding.fragmentEditPaperContentRecyclerView.findFocus();
            if (!(focusView instanceof EditText)) {
                return;
            }

            EditText editText = (EditText) focusView;
            // 获取光标位置
            int index = editText.getSelectionEnd();

            // 索引在0, 不进行添加引用
            if (index == 0) {
                return;
            }

            // 添加参考文献
            QuotationModel quotationModel = new QuotationModel(paperModel);
            ((StringModel) baseContentModel).addQuotation(index, quotationModel);
            paperModel.quotations.add(quotationModel);

            listAdapter.notifyItemChanged(listAdapter.getCurrentList().indexOf(baseContentModel));

            quotationModel.showEditQuotationDialog(requireContext());
        });
    }

    /**
     * 插入一张新图片
     */
    private void insertPicture(int index) {
        // 隐藏输入法
        ContextUtils.hideSoftInputFromWindow(binding.getRoot());
        paperModel.updateContent(index, pictureModel = new PictureModel());
        listAdapter.submitList(paperModel.getContents());

        pickMedia.launch("image/*");
        ToastUtils.makeToast(R.string.paper_edit_paper_content_choose_picture);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 移除未被使用的资源
        paperModel.deleteUnusedResources();
    }
}
