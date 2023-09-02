package com.teleostnacl.szu.paper.model;

import android.text.SpannableStringBuilder;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.paper.R;
import com.teleostnacl.szu.paper.view.edittext.spannable.QuotationClickableSpan;
import com.teleostnacl.szu.paper.view.edittext.spannable.QuotationSpannable;

/**
 * 字符串内容
 */
public class StringModel extends BaseContentModel {

    private static final String QUOTATION_PLACE_HOLDER = " [引用*] ";
    public static final int QUOTATION_PLACE_HOLDER_LENGTH = QUOTATION_PLACE_HOLDER.length();

    //region 各个级别文本的正则表达式
    // 1. 一级标题
    public static final String FIRST_TITLE_REGEX = "\\d+\\. .*";
    // 1.1 二级标题
    public static final String SECOND_TITLE_REGEX = "\\d+\\.\\d+ .*";
    // 1.1.1 三级标题
    public static final String THIRD_TITLE_REGEX = "\\d+\\.\\d+\\.\\d+ .*";
    // endregion

    /**
     * 内容
     */
    private SpannableStringBuilder contentSpannable;

    private String content = "";

    public StringModel() {
        setType(FIRST_TITLE);
    }

    public StringModel(int type) {
        setType(type);
    }

    public StringModel(int type, String content) {
        setType(type);
        this.contentSpannable = new SpannableStringBuilder(content);
    }

    /**
     * 增加该文段的级别
     */
    public void addLevel() {
        if (getType() < CONTENT) {
            setType(getType() + 1);
        }
    }

    /**
     * 减少该文段的级别
     */
    public void reduceLevel() {
        if (getType() > FIRST_TITLE) {
            setType(getType() - 1);
        }
    }

    /**
     * 在指定位置添加引用
     */
    public void addQuotation(int index, QuotationModel quotationModel) {
        if (index == 0) {
            return;
        }

        contentSpannable.insert(index, QUOTATION_PLACE_HOLDER);

        quotationModel.createSpannable();

        quotationModel.quotationSpannable.addQuotation(contentSpannable, index, index + QUOTATION_PLACE_HOLDER_LENGTH);
    }

    @NonNull
    public String getContent() {
        return content;
    }

    @NonNull
    public SpannableStringBuilder getContentSpannable() {
        if (contentSpannable == null) {
            return new SpannableStringBuilder();
        }
        return contentSpannable;
    }

    public void setContentSpannable(@NonNull SpannableStringBuilder content) {
        if (this.contentSpannable == content) {
            return;
        }

        this.contentSpannable = content;
    }

    /**
     * 更新Content
     */
    public void updateContent() {
        StringBuilder content = new StringBuilder(contentSpannable);

        int limit = content.length();
        int index = -1;

        int i = 0;

        // 遍历每一个
        while (true) {
            index = contentSpannable.nextSpanTransition(index, limit, QuotationClickableSpan.class);
            if (index >= limit) {
                break;
            }

            QuotationSpannable[] quotationSpannable = contentSpannable.getSpans(index, index + 1, QuotationSpannable.class);
            if (quotationSpannable != null && quotationSpannable.length > 0 && quotationSpannable[0] != null) {
                quotationSpannable[0].quotationModel.paperModel.quotations.add(quotationSpannable[0].quotationModel);
            }

            int start = index - 1 - i * QUOTATION_PLACE_HOLDER_LENGTH;
            int end = start + QUOTATION_PLACE_HOLDER_LENGTH;

            content.delete(start, end);
            i++;
            index += QUOTATION_PLACE_HOLDER_LENGTH - 1;
        }

        this.content = content.toString();
    }

    @NonNull
    @Override
    public StringModel clone(PaperModel paperModel) {
        StringModel stringModel = (StringModel) super.clone(paperModel);
        if (contentSpannable != null) {
            stringModel.contentSpannable = (SpannableStringBuilder) contentSpannable.subSequence(0, contentSpannable.length());
            // 获取原QuotationSpannable所在的位置
            QuotationSpannable[] quotationSpannables = stringModel.contentSpannable.getSpans(
                    0, stringModel.contentSpannable.length(), QuotationSpannable.class);
            if (quotationSpannables != null) {
                for (QuotationSpannable quotationSpannable : quotationSpannables) {
                    // 清除该Spannable
                    quotationSpannable.removeQuotation(stringModel.contentSpannable);

                    // 克隆Quotation
                    QuotationModel quotationModel = quotationSpannable.quotationModel.clone(paperModel);
                    paperModel.quotations.add(quotationModel);

                    // 设置Spannable
                    quotationModel.quotationSpannable.addQuotation(stringModel.contentSpannable,
                            contentSpannable.getSpanStart(quotationSpannable), contentSpannable.getSpanEnd(quotationSpannable));
                }
            }
        }
        return stringModel;
    }

    // region DataBinding

    /**
     * 根据该内容的级别 设置文本输入框的缩进
     */
    @Bindable
    public int getStartPaddingByType() {
        return ResourcesUtils.getDensityPx(8 * (getType() - 1));
    }

    @Bindable
    public String getHint() {
        switch (getType()) {
            case FIRST_TITLE:
                return ResourcesUtils.getString(R.string.paper_edit_paper_content_first_title);
            case SECOND_TITLE:
                return ResourcesUtils.getString(R.string.paper_edit_paper_content_second_title);
            case THIRD_TITLE:
                return ResourcesUtils.getString(R.string.paper_edit_paper_content_third_title);
            case CONTENT:
                return ResourcesUtils.getString(R.string.paper_edit_paper_content_content);
        }

        return "";
    }
    // endregion
}
