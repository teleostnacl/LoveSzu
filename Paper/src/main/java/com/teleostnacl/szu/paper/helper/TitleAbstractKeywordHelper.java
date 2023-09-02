package com.teleostnacl.szu.paper.helper;

import androidx.annotation.NonNull;

import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.util.StyleUtil;
import com.teleostnacl.szu.paper.util.XWPFDocumentUtil;

import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;

/**
 * 生成论文标题, 摘要, 关键字的Helper类
 */
public class TitleAbstractKeywordHelper {

    /**
     * 论文信息
     */
    private final PaperModel mPaperModel;

    /**
     * 论文文本对象
     */
    private final XWPFDocument mPaperDocument;

    private final static String TITLE_STYLE = "Main Title";

    /**
     * 生成论文标题, 摘要, 关键字
     */
    public static void createTitleAbstractAndKeyWord(@NonNull XWPFDocument paperDocument, @NonNull PaperModel paperModel) {
        TitleAbstractKeywordHelper helper = new TitleAbstractKeywordHelper(paperDocument, paperModel);
        helper.createTitle();
        helper.createInformation();
        helper.createAbstract();
        helper.createKeywords();

        // 换页
        paperDocument.createParagraph().createRun().addBreak(BreakType.PAGE);
    }

    /**
     * 生成英文论文标题, 摘要, 关键字
     */
    public static void createTitleAbstractAndKeyWordEn(@NonNull XWPFDocument paperDocument, @NonNull PaperModel paperModel) {
        TitleAbstractKeywordHelper helper = new TitleAbstractKeywordHelper(paperDocument, paperModel);
        helper.createTitleEn();
        helper.createAbstractEn();
        helper.createKeywordsEn();
    }

    private TitleAbstractKeywordHelper(@NonNull XWPFDocument paperDocument, @NonNull PaperModel paperModel) {
        this.mPaperDocument = paperDocument;
        this.mPaperModel = paperModel;
    }

    /**
     * 生成标题
     */
    private void createTitle() {
        XWPFStyles styles = mPaperDocument.createStyles();
        if (!styles.styleExist(TITLE_STYLE)) {
            // 创建Style
            XWPFStyle style = StyleUtil.createStyle(TITLE_STYLE);
            // 设置字体为华文中宋
            StyleUtil.setFontName(style, XWPFDocumentUtil.FONT_HUA_WEN_ZHONG_SONG);
            // 字号为18号
            StyleUtil.setFontSize(style, 18);
            // 字体加粗
            StyleUtil.setFontBold(style, true);
            // 设置大纲级别为1
            StyleUtil.setOutlineLvl(style, 0);
            // 设置居中
            StyleUtil.setAlignment(style, ParagraphAlignment.CENTER);
            // 设置1.5倍行距
            StyleUtil.setSpacingBetween(style, 1.5);
            // 添加主题
            StyleUtil.addStyle(styles, style);
        }

        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        paragraph.setStyle(TITLE_STYLE);

        XWPFRun run = paragraph.createRun();
        run.setText(mPaperModel.title);
    }

    /**
     * 生成作者信息
     */
    private void createInformation() {
        // 学院（专业）  作者
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        // 1.5倍行距
        paragraph.setSpacingBetween(1.5);
        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_KAI_TI_GB2312, 10.5);
        xwpfRun.setText(mPaperModel.college + "（" + mPaperModel.major + "）" + "  " + mPaperModel.author);

        paragraph = mPaperDocument.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        // 1.5倍行距
        paragraph.setSpacingBetween(1.5);
        xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_KAI_TI_GB2312, 9);
        xwpfRun.setText("学号：" + mPaperModel.no);
    }

    /**
     * 生成摘要
     */
    private void createAbstract() {
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        // 段前段后 0.5行
        paragraph.setSpacingAfterLines(50);
        paragraph.setSpacingBeforeLines(50);
        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_KAI_TI_GB2312, 12);
        xwpfRun.setBold(true);
        xwpfRun.setText("   【摘要】");
        for (String s : PaperModel.spiltLines(mPaperModel.abstractString)) {
            xwpfRun = paragraph.createRun();
            XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_KAI_TI_GB2312, 10.5);
            xwpfRun.setText(s);

            paragraph = mPaperDocument.createParagraph();
            // 段前段后 0.5行
            paragraph.setSpacingBeforeLines(50);
            paragraph.setSpacingAfterLines(50);
            // 首行缩进两个字符
            paragraph.setFirstLineIndent(XWPFDocumentUtil.pt2twip(10.5 * 2));
        }
        // 移除最后一次循环生成的多余的段落
        mPaperDocument.removeBodyElement(mPaperDocument.getBodyElements().size() - 1);
    }

    /**
     * 生成关键词
     */
    private void createKeywords() {
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        // 段前段后 0.5行
        paragraph.setSpacingBeforeLines(50);
        paragraph.setSpacingAfterLines(50);
        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_KAI_TI_GB2312, 12);
        xwpfRun.setBold(true);
        xwpfRun.setText("   【关键词】");
        xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_KAI_TI_GB2312, 10.5);
        xwpfRun.setText(mPaperModel.getKeywords());
    }

    /**
     * 生成英文标题
     */
    private void createTitleEn() {
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        // 设置大纲级别
        XWPFDocumentUtil.setOutlineLvl(paragraph, 1);
        // 居中
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = paragraph.createRun();
        // 设置字体为Times New Roman 18号(小二号)
        XWPFDocumentUtil.setFont(run, XWPFDocumentUtil.FONT_TIMES_NEW_ROMAN, 18);
        run.setBold(true);
        run.setText(mPaperModel.titleEn);
    }

    /**
     * 生成摘要
     */
    private void createAbstractEn() {
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        // 段前段后 0.5行
        paragraph.setSpacingAfterLines(50);
        paragraph.setSpacingBeforeLines(50);
        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_SONG_TI, 12);
        XWPFDocumentUtil.setFontTimesNewRoman(xwpfRun);
        xwpfRun.setBold(true);
        xwpfRun.setText("【Abstract】");
        for (String s : PaperModel.spiltLines(mPaperModel.abstractStringEn)) {
            xwpfRun = paragraph.createRun();
            XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_TIMES_NEW_ROMAN, 10.5);
            xwpfRun.setText(s);

            paragraph = mPaperDocument.createParagraph();
            // 段前段后 0.5行
            paragraph.setSpacingBeforeLines(50);
            paragraph.setSpacingAfterLines(50);
            // 首行缩进两个字符
            paragraph.setFirstLineIndent(XWPFDocumentUtil.pt2twip(10.5 * 2));
        }
        // 移除最后一次循环生成的多余的段落
        mPaperDocument.removeBodyElement(mPaperDocument.getBodyElements().size() - 1);
    }

    /**
     * 生成关键词
     */
    private void createKeywordsEn() {
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        // 段前段后 0.5行
        paragraph.setSpacingBeforeLines(50);
        paragraph.setSpacingAfterLines(50);
        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_SONG_TI, 12);
        XWPFDocumentUtil.setFontTimesNewRoman(xwpfRun);
        xwpfRun.setBold(true);
        xwpfRun.setText("【Key words】");
        xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_TIMES_NEW_ROMAN, 10.5);
        xwpfRun.setText(mPaperModel.getKeywordsEn());
    }
}
