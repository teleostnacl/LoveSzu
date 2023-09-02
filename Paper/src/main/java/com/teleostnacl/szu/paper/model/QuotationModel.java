package com.teleostnacl.szu.paper.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;

import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.view.edittext.RemoveEnterTextWatcher;
import com.teleostnacl.szu.paper.BR;
import com.teleostnacl.szu.paper.R;
import com.teleostnacl.szu.paper.databinding.LayoutEditPaperContentEditQuotationBinding;
import com.teleostnacl.szu.paper.view.edittext.spannable.QuotationClickableSpan;
import com.teleostnacl.szu.paper.view.edittext.spannable.QuotationSpannable;

import java.util.Objects;

/**
 * 引用的内容
 */
public class QuotationModel extends BaseContentModel {

    // 参考文献的内容
    public String quotation = "";

    // 该参考文献所属的论文Model
    public PaperModel paperModel;

    /**
     * Spannable 在文段的TextView中显示为引用图标的标识符
     */
    public QuotationSpannable quotationSpannable;

    public QuotationClickableSpan quotationClickableSpan;

    public QuotationModel(PaperModel paperModel) {
        this.paperModel = paperModel;
        setType(QUOTATION);
    }

    public QuotationModel(String quotation) {
        this.quotation = quotation;
        setType(QUOTATION);
    }

    /**
     * 编辑引用的信息
     */
    public void showEditQuotationDialog(Context context) {
        // 弹出AlertDialog, 输入参考文献内容
        LayoutEditPaperContentEditQuotationBinding dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.layout_edit_paper_content_edit_quotation, null, false);

        dialogBinding.setModel(this);
        dialogBinding.itemEditPaperContentValue.addTextChangedListener(new RemoveEnterTextWatcher());

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.paper_edit_paper_content_edit_quotation)
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, null)
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .create();

        alertDialog.show();

        // 保存
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            CharSequence charSequence = dialogBinding.itemEditPaperContentValue.getText();
            // 判空
            if (charSequence == null || TextUtils.isEmpty(charSequence)) {
                ToastUtils.makeToast(R.string.paper_edit_paper_content_edit_quotation_empty_error);
                return;
            }

            String string = charSequence.toString();

            // 检查重复
            if (paperModel.quotations.contains(new QuotationModel(string)) && !string.equals(quotation)) {
                ToastUtils.makeToast(R.string.paper_edit_paper_content_edit_quotation_repeat_error);
                return;
            }

            quotation = string;
            alertDialog.dismiss();
        });

        // 取消
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
            if (TextUtils.isEmpty(quotation)) {
                ToastUtils.makeToast(R.string.paper_edit_paper_content_edit_quotation_empty_error);
                return;
            }

            alertDialog.dismiss();
        });
    }

    /**
     * 创建Spannable的方法
     */
    public void createSpannable() {
        quotationSpannable = new QuotationSpannable(this);
        quotationClickableSpan = new QuotationClickableSpan();
    }

    @NonNull
    @Override
    public QuotationModel clone(PaperModel paperModel) {
        QuotationModel quotationModel = (QuotationModel) super.clone(paperModel);
        quotationModel.paperModel = paperModel;
        quotationModel.createSpannable();
        return quotationModel;
    }

    /**
     * 覆写equals, 当quotation相同时, 则为相同内容
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuotationModel)) return false;
        QuotationModel that = (QuotationModel) o;
        return Objects.equals(quotation, that.quotation);
    }

    @Bindable
    public String getQuotation() {
        return quotation;
    }

    public void setQuotation(String quotation) {
        this.quotation = quotation;
        notifyPropertyChanged(BR.quotation);
    }
}
