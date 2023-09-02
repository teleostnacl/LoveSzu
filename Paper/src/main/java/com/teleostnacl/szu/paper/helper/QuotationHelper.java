package com.teleostnacl.szu.paper.helper;

import static com.teleostnacl.szu.paper.util.XWPFDocumentUtil.FONT_KAI_TI_GB2312;
import static com.teleostnacl.szu.paper.util.XWPFDocumentUtil.FONT_TIMES_NEW_ROMAN;

import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.model.QuotationModel;
import com.teleostnacl.szu.paper.util.StyleUtil;
import com.teleostnacl.szu.paper.util.XWPFDocumentUtil;

import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;

import java.math.BigInteger;

/**
 * 生成参考文献类
 */
public class QuotationHelper {
    /**
     * 论文信息
     */
    private final PaperModel mPaperModel;

    /**
     * 论文文本对象
     */
    private final XWPFDocument mPaperDocument;

    /**
     * 参考文献使用的Style
     */
    private final static String QUOTATION_STYLE = "QUOTATION";

    private QuotationHelper(PaperModel mPaperModel, XWPFDocument mPaperDocument) {
        this.mPaperModel = mPaperModel;
        this.mPaperDocument = mPaperDocument;
    }

    public static void createQuotation(XWPFDocument paperDocument, PaperModel paperModel) {
        QuotationHelper quotationHelper = new QuotationHelper(paperModel, paperDocument);
        quotationHelper.createQuotationStyle();
        quotationHelper.createQuotationsTitle();
        quotationHelper.createQuotations();
        // 创建新的一页
        paperDocument.createParagraph().createRun().addBreak(BreakType.PAGE);
    }

    /**
     * 创建主题
     */
    private void createQuotationStyle() {
        XWPFStyles styles = mPaperDocument.createStyles();
        if (styles.styleExist(QUOTATION_STYLE)) {
            return;
        }

        XWPFStyle style = StyleUtil.createStyle(QUOTATION_STYLE);
        // 小五号中文楷体_GB2312, 英文Times New Roman
        StyleUtil.setFontName(style, XWPFRun.FontCharRange.ascii, FONT_TIMES_NEW_ROMAN);
        StyleUtil.setFontName(style, XWPFRun.FontCharRange.eastAsia, FONT_KAI_TI_GB2312);
        StyleUtil.setFontSize(style, 9);
        // 段前段后05行
        StyleUtil.setSpacingLines(style, 0.5);
        // 单倍行距
        StyleUtil.setSpacingBetween(style, 1);
        // 两端对齐
        StyleUtil.setAlignment(style, ParagraphAlignment.BOTH);

        StyleUtil.addStyle(styles, style);
    }

    /**
     * 创建参考文献标题 【参考文献】
     */
    private void createQuotationsTitle() {
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        // 段前段后 0.5行
        paragraph.setSpacingAfterLines(50);
        paragraph.setSpacingBeforeLines(50);

        // 设置大纲级别
        XWPFDocumentUtil.setOutlineLvl(paragraph, 1);

        XWPFRun xwpfRun = paragraph.createRun();
        // 楷体五号加粗
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_KAI_TI_GB2312, 10.5);
        xwpfRun.setBold(true);
        xwpfRun.setText("【参考文献】");
    }

    private void createQuotations() {
        int i = 1;
        for (QuotationModel model : mPaperModel.quotations) {
            XWPFParagraph paragraph = mPaperDocument.createParagraph();
            paragraph.setStyle(QUOTATION_STYLE);
            // 创建书签
            CTBookmark ctBookmark = paragraph.getCTP().addNewBookmarkStart();
            ctBookmark.setName("Quotation" + "-" + i);
            ctBookmark.setId(BigInteger.valueOf(i));
            // 设置内容
            XWPFRun xwpfRun = paragraph.createRun();
            xwpfRun.setText("[" + (i) + "] " + model.quotation);
            // 书签结束
            paragraph.getCTP().addNewBookmarkEnd().setId(BigInteger.valueOf(i));
            i++;
        }
    }
}
