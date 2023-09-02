package com.teleostnacl.szu.paper.helper;

import static com.teleostnacl.szu.paper.util.XWPFDocumentUtil.FONT_HEI_TI;
import static com.teleostnacl.szu.paper.util.XWPFDocumentUtil.FONT_SONG_TI;

import androidx.annotation.NonNull;

import com.teleostnacl.common.java.util.IOUtils;
import com.teleostnacl.szu.paper.model.BaseContentModel;
import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.model.PictureModel;
import com.teleostnacl.szu.paper.model.StringModel;
import com.teleostnacl.szu.paper.util.StyleUtil;

import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 生成论文内容的Helper类
 */
public class ContentHelper {
    /**
     * 一级标题
     */
    private final static String FIRST_STYLE = "TITLE 1";

    /**
     * 二级标题
     */
    private final static String SECOND_STYLE = "TITLE 2";

    /**
     * 三级标题
     */
    private final static String THIRD_STYLE = "TITLE 3";

    /**
     * 正文
     */
    private static final String CONTENT_STYLE = "CONTENT";

    /**
     * 图例
     */
    private static final String LEGEND_STYLE = "LEGEND";

    /**
     * 图片
     */
    private static final String PICTURE_STYLE = "PICTURE";

    /**
     * 论文信息
     */
    private final PaperModel mPaperModel;

    /**
     * 论文文本对象
     */
    private final XWPFDocument mPaperDocument;

    private XWPFParagraph paragraph;

    // 是否为第一个一级标题
    private boolean first = true;

    private int pictureNo = 0;

    public static void createContent(XWPFDocument paperDocument, PaperModel paperModel) throws Exception {
        ContentHelper contentHelper = new ContentHelper(paperDocument, paperModel);
        contentHelper.createStyle();
        contentHelper.createContents();
        // 创建新的一页
        paperDocument.createParagraph().createRun().addBreak(BreakType.PAGE);
    }

    private ContentHelper(XWPFDocument paperDocument, PaperModel paperModel) {
        this.mPaperModel = paperModel;
        this.mPaperDocument = paperDocument;
    }

    // region 创建主题

    /**
     * 创建主题
     */
    private void createStyle() {
        createFirstStyle();
        createSecondStyle();
        createThirdStyle();
        createContentStyle();
        createLegendStyle();
        createPictureStyle();
    }

    /**
     * 创建一级主题
     */
    private void createFirstStyle() {
        XWPFStyles styles = mPaperDocument.createStyles();
        if (styles.styleExist(FIRST_STYLE)) {
            return;
        }

        XWPFStyle style = StyleUtil.createStyle(FIRST_STYLE);
        // 三号黑体加粗 单倍行距 段前段后0.5行
        StyleUtil.setFontName(style, FONT_HEI_TI);
        StyleUtil.setFontSize(style, 16);
        StyleUtil.setFontBold(style, true);
        StyleUtil.setSpacingBetween(style, 1);
        StyleUtil.setSpacingLines(style, 0.5);
        // 大纲级别1级
        StyleUtil.setOutlineLvl(style, 1);
        // 两端对齐
        StyleUtil.setAlignment(style, ParagraphAlignment.BOTH);

        StyleUtil.addStyle(styles, style);
    }

    /**
     * 创建二级主题
     */
    private void createSecondStyle() {
        XWPFStyles styles = mPaperDocument.createStyles();
        if (styles.styleExist(SECOND_STYLE)) {
            return;
        }

        XWPFStyle style = StyleUtil.createStyle(SECOND_STYLE);
        // 小三号黑体加粗 单倍行距 段前段后0.5行
        StyleUtil.setFontName(style, FONT_HEI_TI);
        StyleUtil.setFontSize(style, 15);
        StyleUtil.setFontBold(style, true);
        StyleUtil.setSpacingBetween(style, 1);
        StyleUtil.setSpacingLines(style, 0.5);
        // 大纲级别2级
        StyleUtil.setOutlineLvl(style, 2);
        // 两端对齐
        StyleUtil.setAlignment(style, ParagraphAlignment.BOTH);

        StyleUtil.addStyle(styles, style);
    }

    /**
     * 创建三级主题
     */
    private void createThirdStyle() {
        XWPFStyles styles = mPaperDocument.createStyles();
        if (styles.styleExist(THIRD_STYLE)) {
            return;
        }

        XWPFStyle style = StyleUtil.createStyle(THIRD_STYLE);
        // 四号黑体加粗 单倍行距 段前段后0.5行
        StyleUtil.setFontName(style, FONT_HEI_TI);
        StyleUtil.setFontSize(style, 14);
        StyleUtil.setFontBold(style, true);
        StyleUtil.setSpacingBetween(style, 1);
        StyleUtil.setSpacingLines(style, 0.5);
        // 大纲级别2级
        StyleUtil.setOutlineLvl(style, 3);
        // 两端对齐
        StyleUtil.setAlignment(style, ParagraphAlignment.BOTH);

        StyleUtil.addStyle(styles, style);
    }

    /**
     * 创建正文主题
     */
    private void createContentStyle() {
        XWPFStyles styles = mPaperDocument.createStyles();
        if (styles.styleExist(CONTENT_STYLE)) {
            return;
        }

        XWPFStyle style = StyleUtil.createStyle(CONTENT_STYLE);
        // 五号宋体 单倍行距 段前段后0.5行
        StyleUtil.setFontName(style, FONT_SONG_TI);
        StyleUtil.setFontSize(style, 10.5);
        StyleUtil.setSpacingBetween(style, 1);
        StyleUtil.setSpacingLines(style, 0.5);
        // 首行缩进两个字符
        StyleUtil.setIndentationFirstLine(style, 10.5, 2);
        // 两端对齐
        StyleUtil.setAlignment(style, ParagraphAlignment.BOTH);

        StyleUtil.addStyle(styles, style);
    }

    /**
     * 创建图例表例的主题
     */
    private void createLegendStyle() {
        XWPFStyles styles = mPaperDocument.createStyles();
        if (styles.styleExist(LEGEND_STYLE)) {
            return;
        }

        XWPFStyle style = StyleUtil.createStyle(LEGEND_STYLE);
        // 小五号宋体 居中
        StyleUtil.setFontName(style, FONT_SONG_TI);
        StyleUtil.setFontSize(style, 9);
        StyleUtil.setAlignment(style, ParagraphAlignment.CENTER);

        StyleUtil.addStyle(styles, style);
    }

    /**
     * 创建图片使用的主题
     */
    private void createPictureStyle() {
        XWPFStyles styles = mPaperDocument.createStyles();
        if (styles.styleExist(PICTURE_STYLE)) {
            return;
        }

        XWPFStyle style = StyleUtil.createStyle(PICTURE_STYLE);
        // 居中
        StyleUtil.setAlignment(style, ParagraphAlignment.CENTER);
        StyleUtil.addStyle(styles, style);
    }
    // endregion

    /**
     * 生成正文
     */
    private void createContents() throws Exception {
        // 遍历每一个模型
        for (BaseContentModel model : mPaperModel.getContents()) {
            switch (model.getType()) {
                case BaseContentModel.PICTURE:
                    generatePicture((PictureModel) model);
                    break;
                default:
                    generateString((StringModel) model);
                    break;
            }
        }
    }

    /**
     * 生成字符串
     */
    private void generateString(@NonNull StringModel stringModel) {
        // 拆分成多个段落
        String[] strings = PaperModel.spiltLines(stringModel.getContent());
        XWPFRun run;

        for (int i = 0; i < strings.length; i++) {
            if (paragraph == null || stringModel.isParagraph || i > 0) {
                paragraph = mPaperDocument.createParagraph();
                // 一级标题需要另起一页
                if (stringModel.getType() == StringModel.FIRST_TITLE) {
                    // 跳过第一个一级标题
                    if (first) {
                        first = false;
                    } else {
                        paragraph.createRun().addBreak(BreakType.PAGE);
                        paragraph = mPaperDocument.createParagraph();
                    }
                }
                // 根据类型设置段路样式
                switch (stringModel.getType()) {
                    case StringModel.FIRST_TITLE:
                        paragraph.setStyle(FIRST_STYLE);
                        break;
                    case StringModel.SECOND_TITLE:
                        paragraph.setStyle(SECOND_STYLE);
                        break;
                    case StringModel.THIRD_TITLE:
                        paragraph.setStyle(THIRD_STYLE);
                        break;
                    case StringModel.CONTENT:
                        paragraph.setStyle(CONTENT_STYLE);
                        break;
                }
            }
            run = paragraph.createRun();
            run.setText(strings[i]);
        }
    }

    /**
     * 生成图片
     */
    private void generatePicture(PictureModel pictureModel) throws Exception {
        if (paragraph == null || pictureModel.isParagraph) {
            paragraph = mPaperDocument.createParagraph();
            paragraph.setStyle(PICTURE_STYLE);
        }

        XWPFRun xwpfRun = paragraph.createRun();

        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(Paths.get(pictureModel.filePath));
            xwpfRun.addPicture(inputStream, pictureModel.pictureType, pictureModel.pictureName, pictureModel.width, pictureModel.height);
        } finally {
            IOUtils.close(inputStream);
        }

        // 创建图例
        if (pictureModel.isParagraph) {
            paragraph = mPaperDocument.createParagraph();
            paragraph.setStyle(LEGEND_STYLE);
            xwpfRun = paragraph.createRun();
            if (mPaperModel.isAutoGeneratePictureNo()) {
                // 自动生成图片图例编号
                xwpfRun.setText("图" + (++pictureNo) + " " + pictureModel.pictureName);
            } else {
                xwpfRun.setText(pictureModel.pictureName);
            }
        }
        // 图片之后需要增加空行
        paragraph = mPaperDocument.createParagraph();
        paragraph.setStyle(LEGEND_STYLE);
    }
}
