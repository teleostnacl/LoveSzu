package com.teleostnacl.szu.paper.view.edittext.spannable;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.SuperscriptSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.teleostnacl.szu.paper.model.QuotationModel;

public class QuotationSpannable extends SuperscriptSpan {

    public final QuotationModel quotationModel;

    public QuotationSpannable(QuotationModel quotationModel) {
        this.quotationModel = quotationModel;
    }

    public void onClick(@NonNull View widget) {
        quotationModel.showEditQuotationDialog(widget.getContext());
    }

    /**
     * 在指定SpannableStringBuilder的指定位置添加QuotationSpannable
     */
    public void addQuotation(@NonNull SpannableStringBuilder spannableStringBuilder, int start, int end) {
        spannableStringBuilder.setSpan(quotationModel.quotationSpannable,
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(quotationModel.quotationClickableSpan,
                start + 1, end - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * 从指定的spannableStringBuilder删除此QuotationSpannable
     */
    public void removeQuotation(@NonNull SpannableStringBuilder spannableStringBuilder) {
        spannableStringBuilder.removeSpan(this);
        spannableStringBuilder.removeSpan(quotationModel.quotationClickableSpan);
    }
}
