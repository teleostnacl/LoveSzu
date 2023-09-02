package com.teleostnacl.szu.paper.helper;

import static com.teleostnacl.szu.paper.util.XWPFDocumentUtil.FONT_HEI_TI;
import static com.teleostnacl.szu.paper.util.XWPFDocumentUtil.FONT_SONG_TI;
import static com.teleostnacl.szu.paper.util.XWPFDocumentUtil.FONT_TIMES_NEW_ROMAN;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.IOUtils;
import com.teleostnacl.szu.paper.R;
import com.teleostnacl.szu.paper.model.BaseContentModel;
import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.model.PictureModel;
import com.teleostnacl.szu.paper.util.StyleUtil;
import com.teleostnacl.szu.paper.util.XWPFDocumentUtil;

import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;

/**
 * 生成论文用的Helper类
 */
public class PaperHelper {

    /**
     * 文本对象
     */
    private final XWPFDocument mPaperDocument;

    /**
     * 论文信息
     */
    private final PaperModel mPaperModel;

    /**
     * 页眉使用的样式
     */
    private static final String HEADER_STYLE = "HEADER";

    /**
     * 页尾使用的样式
     */
    private static final String FOOTER_STYLE = "FOOTER";


    /**
     * 生产论文的方法
     *
     * @return 是否成功
     */
    public static boolean createPaper(PaperModel paperModel) {
        // 判空
        if (checkNull(paperModel)) {
            return false;
        }

        try {
            // 创建文档
            PaperHelper paper = new PaperHelper(paperModel);

            // 创建封面
            paper.createCover();

            // 创建目录标题
            paper.createCatalogueTitle();

            // 创建正文的页码
            paper.createContentPage();
            // 创建正文的页眉
            paper.createContentHeader();

            // 创建标题摘要和关键字
            paper.createTitleAbstractAndKeyWord();

            // 创建正文
            paper.createContent();

            // 创建参考文献
            paper.createQuotations();

            // 创建致谢
            paper.createThanks();

            // 创建英文标题摘要和关键字
            paper.createTitleAbstractAndKeyWordEn();

            // 写入文件
            paper.writeFile();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean checkNull(@Nullable PaperModel paperModel) {
        if (paperModel == null) {
            return true;
        }

        if (paperModel.file == null) {
            return true;
        }

        // 防空
        if (paperModel.title == null) {
            paperModel.title = "";
        }
        if (paperModel.titleEn == null) {
            paperModel.titleEn = "";
        }
        if (paperModel.author == null) {
            paperModel.author = "";
        }
        if (paperModel.major == null) {
            paperModel.major = "";
        }
        if (paperModel.college == null) {
            paperModel.college = "";
        }
        if (paperModel.no == null) {
            paperModel.no = "";
        }
        if (paperModel.teacher == null) {
            paperModel.teacher = "";
        }
        if (paperModel.professionalQualification == null) {
            paperModel.professionalQualification = "";
        }
        if (paperModel.abstractString == null) {
            paperModel.abstractString = "";
        }
        if (paperModel.abstractStringEn == null) {
            paperModel.abstractStringEn = "";
        }
        if (paperModel.thanks == null) {
            paperModel.thanks = "";
        }
        for (BaseContentModel baseContentModel : paperModel.getContents()) {
            if (baseContentModel instanceof PictureModel) {
                if (((PictureModel) baseContentModel).getPictureName() == null) {
                    ((PictureModel) baseContentModel).setPictureName("");
                }
            }
        }


        return false;
    }

    private PaperHelper(@NonNull PaperModel paperModel) throws Exception {
        InputStream inputStream = null;

        try {
            //获取模板文件中的内容
            mPaperDocument = new XWPFDocument(inputStream = ResourcesUtils.getResources().openRawResource(R.raw.template));
        } finally {
            IOUtils.close(inputStream);
        }

        // 清空模板中的全部内容
        for (int i = mPaperDocument.getBodyElements().size() - 1; i >= 0; i--) {
            mPaperDocument.removeBodyElement(i);
        }

        this.mPaperModel = paperModel;
    }

    /**
     * 创建论文封面
     */
    private void createCover() {
        CoverHelper.createCover(mPaperDocument, mPaperModel);
    }

    /**
     * 创建目录的标题
     */
    private void createCatalogueTitle() {
        CatalogueHelper.createCatalogueTitle(mPaperDocument);
    }

    /**
     * 创建正文页码
     */
    private void createContentPage() {
        // 创建页码的样式
        XWPFStyles styles = mPaperDocument.createStyles();
        if (!styles.styleExist(FOOTER_STYLE)) {
            XWPFStyle style = StyleUtil.createStyle(FOOTER_STYLE);
            // 宋体小五 居中
            StyleUtil.setFontName(style, FONT_SONG_TI);
            StyleUtil.setFontName(style, XWPFRun.FontCharRange.ascii, FONT_TIMES_NEW_ROMAN);
            StyleUtil.setFontSize(style, 9);
            StyleUtil.setAlignment(style, ParagraphAlignment.CENTER);
            StyleUtil.addStyle(mPaperDocument.createStyles(), style);
        }

        CTBody body = mPaperDocument.getDocument().getBody();
        CTSectPr sectPr = body.isSetSectPr() ? body.getSectPr() : body.addNewSectPr();
        CTPageNumber pgNum = sectPr.isSetPgNumType() ? sectPr.getPgNumType() : sectPr.addNewPgNumType();
        pgNum.setStart(BigInteger.valueOf(1));

        XWPFFooter footer = mPaperDocument.createFooter(HeaderFooterType.DEFAULT);

        XWPFParagraph paragraph = footer.getParagraphArray(0);
        if (paragraph == null) {
            paragraph = footer.createParagraph();
        }

        paragraph.setStyle(FOOTER_STYLE);

        CTP ctp = paragraph.getCTP();

        CTText txt = ctp.addNewR().addNewT();
        txt.setStringValue("第 ");
        txt.setSpace(SpaceAttribute.Space.PRESERVE);

        CTR run = ctp.addNewR();
        run.addNewFldChar().setFldCharType(STFldCharType.BEGIN);
        run.addNewInstrText().setStringValue(" PAGE ");
        run.addNewFldChar().setFldCharType(STFldCharType.END);

        txt = ctp.addNewR().addNewT();
        txt.setStringValue(" 页");
        txt.setSpace(SpaceAttribute.Space.PRESERVE);
    }

    /**
     * 创建论文的页眉
     */
    private void createContentHeader() {
        // 创建页眉的主题
        XWPFStyles styles = mPaperDocument.createStyles();
        if (!styles.styleExist(HEADER_STYLE)) {
            XWPFStyle style = StyleUtil.createStyle(HEADER_STYLE);
            // 宋体小五 居中
            StyleUtil.setFontName(style, FONT_SONG_TI);
            StyleUtil.setFontName(style, XWPFRun.FontCharRange.ascii, FONT_TIMES_NEW_ROMAN);
            StyleUtil.setFontSize(style, 9);
            StyleUtil.setAlignment(style, ParagraphAlignment.CENTER);
            // 设置下边框
            StyleUtil.setBorder(style, StyleUtil.BOTTOM, Borders.BASIC_BLACK_DASHES);
            StyleUtil.addStyle(mPaperDocument.createStyles(), style);
        }

        CTP ctp = CTP.Factory.newInstance();

        CTText txt = ctp.addNewR().addNewT();
        txt.setStringValue("深圳大学本科毕业论文—" + mPaperModel.title);
        txt.setSpace(SpaceAttribute.Space.PRESERVE);

        XWPFParagraph par = new XWPFParagraph(ctp, mPaperDocument);
        par.setStyle(HEADER_STYLE);

        XWPFHeaderFooterPolicy policy = mPaperDocument.getHeaderFooterPolicy();
        if (policy == null) {
            policy = mPaperDocument.createHeaderFooterPolicy();
        }
        policy.createHeader(XWPFHeaderFooterPolicy.DEFAULT, new XWPFParagraph[]{par});
    }

    /**
     * 创建论文标题 摘要 和关键字
     */
    private void createTitleAbstractAndKeyWord() {
        TitleAbstractKeywordHelper.createTitleAbstractAndKeyWord(mPaperDocument, mPaperModel);
    }

    /**
     * 创建论文正文
     */
    private void createContent() throws Exception {
        ContentHelper.createContent(mPaperDocument, mPaperModel);
    }

    /**
     * 创建参考文献
     */
    private void createQuotations() {
        QuotationHelper.createQuotation(mPaperDocument, mPaperModel);
    }

    /**
     * 生成致谢
     */
    private void createThanks() {
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setSpacingBetween(18, LineSpacingRule.AT_LEAST);
        // 设置大纲级别
        XWPFDocumentUtil.setOutlineLvl(paragraph, 1);

        XWPFRun xwpfRun = paragraph.createRun();
        xwpfRun.setBold(true);
        XWPFDocumentUtil.setFont(xwpfRun, FONT_HEI_TI, 12);
        xwpfRun.setText("致谢");

        // 致谢内容
        for (String s : PaperModel.spiltLines(mPaperModel.thanks)) {
            paragraph = mPaperDocument.createParagraph();
            paragraph.setSpacingAfterLines(50);
            paragraph.setSpacingBeforeLines(50);
            // 首行缩进两个字符
            paragraph.setIndentationFirstLine(XWPFDocumentUtil.pt2twip(10.5 * 2));

            xwpfRun = paragraph.createRun();
            XWPFDocumentUtil.setFont(xwpfRun, FONT_SONG_TI, 10.5);
            xwpfRun.setText(s);
        }

        // 换页
        mPaperDocument.createParagraph().createRun().addBreak(BreakType.PAGE);
    }

    /**
     * 生成论文的英文标题 摘要 和关键字
     */
    private void createTitleAbstractAndKeyWordEn() {
        TitleAbstractKeywordHelper.createTitleAbstractAndKeyWordEn(mPaperDocument, mPaperModel);
    }

    /**
     * 写入文件
     */
    private void writeFile() throws Exception {
        if (!recreateFile(mPaperModel.file)) {
            throw new Exception("File recreate failed");
        }

        OutputStream outputStream = null;
        try {
            mPaperDocument.write(outputStream = Files.newOutputStream(mPaperModel.file.toPath()));
        } finally {
            IOUtils.close(outputStream);
        }
    }

    /**
     * 重新创建论文文件
     *
     * @param file 论文的文件
     * @return 是否成功
     */
    private boolean recreateFile(@NonNull File file) throws Exception {
        File parentFile = file.getParentFile();
        // 父文件夹不存在时 尝试创建文件夹
        if (parentFile == null || (!parentFile.exists() && !parentFile.mkdirs())) {
            // 创建失败 则返回false
            return false;
        }

        // 文件存在时 尝试删除文件 并返回结果
        if (file.exists() && !file.delete()) {
            return false;
        }

        return file.createNewFile();
    }
}
