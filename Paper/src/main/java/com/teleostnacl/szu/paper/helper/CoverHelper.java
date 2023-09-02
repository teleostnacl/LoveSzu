package com.teleostnacl.szu.paper.helper;

import androidx.annotation.NonNull;

import com.teleostnacl.szu.paper.model.PaperModel;
import com.teleostnacl.szu.paper.util.StyleUtil;
import com.teleostnacl.szu.paper.util.XWPFDocumentUtil;

import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 生成封面的Helper类
 */
public class CoverHelper {
    /**
     * 封面上的KEY
     */
    private static final String KEY_TITLE = "题目：";
    private static final String KEY_AUTHOR = "姓名：";
    private static final String KEY_MAJOR = "专业：";
    private static final String KEY_COLLEGE = "学院：";
    private static final String KEY_NO = "学号：";
    private static final String KEY_TEACHER = "指导教师：";
    private static final String KEY_PROFESSIONAL_QUALIFICATION = "职称：";

    /**
     * 封面第一行空白所使用的style
     */
    private static final String FIRST_BLANK_LINE_STYLE = "SONG_TI_14";

    /**
     * 封面其余行空白所使用的style
     */
    private static final String COMMON_BLANK_LINE_STYLE = "SONG_TI_16";

    /**
     * 论文信息
     */
    private final PaperModel mPaperModel;

    /**
     * 论文文本对象
     */
    private final XWPFDocument mPaperDocument;

    public static void createCover(@NonNull XWPFDocument paperDocument, @NonNull PaperModel paperModel) {
        CoverHelper coverHelper = new CoverHelper(paperDocument, paperModel);

        // 创建封面
        coverHelper.createCover();
        // 添加换页赴
        paperDocument.createParagraph().createRun().addBreak(BreakType.PAGE);
        // 插入分节符
        XWPFDocumentUtil.addNewSectPr(paperDocument);
    }

    private CoverHelper(@NonNull XWPFDocument paperDocument, @NonNull PaperModel paperModel) {
        this.mPaperDocument = paperDocument;
        this.mPaperModel = paperModel;
    }

    /**
     * 创建封面
     */
    private void createCover() {
        // 插入标题: 深圳大学
        addSZU();
        // 插入副标题: 本 科 毕 业 论 文（设计）
        addSubTitle();
        // 插入题目 姓名 专业 学院 学号 指导教师 职称
        addElements();
        // 插入时间
        addDate();
    }

    /**
     * 插入深圳大学
     */
    private void addSZU() {
        // 创建第一行空白的Style
        XWPFStyles styles = mPaperDocument.createStyles();
        if (!styles.styleExist(FIRST_BLANK_LINE_STYLE)) {
            XWPFStyle style = StyleUtil.createStyle(FIRST_BLANK_LINE_STYLE);
            StyleUtil.setFontName(style, XWPFDocumentUtil.FONT_SONG_TI);
            StyleUtil.setFontSize(style, 14);
            styles.addStyle(style);
        }
        // 插入第一行空白
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        paragraph.setStyle(FIRST_BLANK_LINE_STYLE);

        // 插入深圳大学
        paragraph = mPaperDocument.createParagraph();
        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_HUA_WEN_XIN_KAI, 36);
        XWPFDocumentUtil.setFontTimesNewRoman(xwpfRun);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        xwpfRun.setText("深 圳 大 学");
    }

    /**
     * 插入副标题: 本 科 毕 业 论 文（设计）
     */
    private void addSubTitle() {
        // 本 科 毕 业 论 文（设计）
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_HEI_TI, 24);
        XWPFDocumentUtil.setFontTimesNewRoman(xwpfRun);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        xwpfRun.setText("本 科 毕 业 论 文（设计）");

        // 插入三行空白
        for (int i = 0; i < 3; i++) {
            XWPFStyles styles = mPaperDocument.createStyles();
            if (!styles.styleExist(COMMON_BLANK_LINE_STYLE)) {
                XWPFStyle style = StyleUtil.createStyle(COMMON_BLANK_LINE_STYLE);
                StyleUtil.setFontName(style, XWPFDocumentUtil.FONT_SONG_TI);
                StyleUtil.setFontSize(style, 16);
                StyleUtil.addStyle(styles, style);
            }
            paragraph = mPaperDocument.createParagraph();
            paragraph.setStyle(COMMON_BLANK_LINE_STYLE);
        }
    }

    /**
     * 插入题目 姓名 专业 学院 学号 指导教师 职称
     */
    private void addElements() {
        // 题目
        addElement(KEY_TITLE, mPaperModel.title);
        // 姓名
        addElement(KEY_AUTHOR, mPaperModel.author);
        // 专业
        addElement(KEY_MAJOR, mPaperModel.major);
        // 学院
        addElement(KEY_COLLEGE, mPaperModel.college);
        // 学院
        addElement(KEY_NO, mPaperModel.no);
        // 指导教师
        addElement(KEY_TEACHER, mPaperModel.teacher);
        // 职称
        addElement(KEY_PROFESSIONAL_QUALIFICATION, mPaperModel.professionalQualification);
    }

    /**
     * 插入日期
     */
    private void addDate() {
        XWPFParagraph paragraph;
        // 插入四行空白
        for (int i = 0; i < 4; i++) {
            XWPFStyles styles = mPaperDocument.createStyles();
            if (!styles.styleExist(COMMON_BLANK_LINE_STYLE)) {
                XWPFStyle style = StyleUtil.createStyle(COMMON_BLANK_LINE_STYLE);
                StyleUtil.setFontName(style, XWPFDocumentUtil.FONT_SONG_TI);
                StyleUtil.setFontSize(style, 16);
                StyleUtil.addStyle(styles, style);
            }
            paragraph = mPaperDocument.createParagraph();
            paragraph.setStyle(COMMON_BLANK_LINE_STYLE);
        }

        // 插入日期
        paragraph = mPaperDocument.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_SONG_TI, 16);
        XWPFDocumentUtil.setFontTimesNewRoman(xwpfRun);
        SimpleDateFormat format = new SimpleDateFormat("yyyy年 MM 月 dd 日", Locale.CHINA);
        String date = format.format(new Date());
        xwpfRun.setText(date);
    }


    /**
     * 在封面插入指定的元素(题目 姓名 专业 学院 学号 指导教师 职称)
     */
    private void addElement(@NonNull String key, @NonNull String value) {
        XWPFParagraph paragraph = mPaperDocument.createParagraph();
        paragraph.setIndentationLeft(1155);
        paragraph.setSpacingBetween(35, LineSpacingRule.EXACT);

        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_HEI_TI, 16);
        xwpfRun.setText(key);

        handleElement(paragraph, key, value);

    }

    /**
     * 处理封面(姓名 专业 学院 学号 指导教师 职称)元素, 使其在横线中间居中
     *
     * @param paragraph 所在的段落
     * @param key       元素的键值
     * @param value     元素
     */
    private void handleElement(XWPFParagraph paragraph, String key, @NonNull String value) {
        // 一行最大字符数(除指导教师为24 其他都为28)
        int max = KEY_TEACHER.equals(key) ? 26 : 30;

        char[] chars = value.toCharArray();
        // (楷体_GB2312字体为等宽字体 数字字母占一个字符, 中文占两个字符)
        int charNum = 0;
        for (char c : chars) {
            if (c < 127) {
                // ASCII字符全为半角字符
                charNum++;
            } else {
                // 非ASCII字符占两个字符
                charNum += 2;
            }
        }

        // 恰好显示 则无需进行处理
        if (charNum == max) {
            setElementText(paragraph, value);
        }

        // 构造处理后的字符串的stringBuilder
        StringBuilder stringBuilder = new StringBuilder();
        // 需要开始增加的空格的字符串
        String needSpaceString;
        // 需要空格的总数
        int spaceNum;

        // 一行能显示完整
        if (charNum < max) {
            // 计算需要增加空格数
            spaceNum = max - charNum;
            needSpaceString = value;
        }
        // 大于最大行数时 计算最后一行需要增加的空格数
        else {
            // 由于每行前面有key, 导致第二行之后所需要的空格占位符数量增多
            int more = KEY_TEACHER.equals(key) ? 10 : 6;

            // 处理每一行
            int index = 0;
            int i = 0;
            // 处理每一行
            do {
                // 遍历每一个字符
                int tmp = 0;
                for (; i < chars.length; i++) {
                    int tmp1 = tmp;
                    if (chars[i] < 127) {
                        // ASCII字符全为半角字符
                        tmp1++;
                    } else {
                        // 非ASCII字符占两个字符
                        tmp1 += 2;
                    }

                    // 加上当前字符已经超过一行显示了 停止循环
                    if (tmp1 > max) {
                        break;
                    }

                    tmp = tmp1;
                    // 将字符添加进已处理的字符中去
                    stringBuilder.append(chars[i]);
                }

                index += tmp;

                // 强制换行
                setElementText(paragraph, stringBuilder.toString()).addBreak();
                stringBuilder = new StringBuilder();

                // 还不能显示完全 增加空白 - 全角空格占位符(前面key的位置不显示字符)
                for (int j = 0; j < more; j++) {
                    stringBuilder.append(XWPFDocumentUtil.SPACE);
                }

                // 如果剩下的字符数不能占满一行 则停止循环
            } while (i < chars.length && charNum - index >= max);

            // 计算需要增加空格的总数
            spaceNum = max - (charNum - index);
            needSpaceString = String.valueOf(chars, i, chars.length - i);
        }

        // 计算空格数的一半
        int halfSpace = spaceNum / 2;
        // 需要使用空格占位符 前的个数
        int leftSpace = KEY_TEACHER.equals(key) ? halfSpace - 2 : halfSpace;
        // 如果所需空格数为奇数时, 将缺少的空格移到后面
        halfSpace = spaceNum % 2 == 0 ? halfSpace : halfSpace + 1;
        // 需要使用空格占位符 后的个数
        int rightSpace = KEY_TEACHER.equals(key) ? halfSpace + 2 : halfSpace;

        // 使用空格进行前后占位
        for (int i = 0; i < leftSpace; i++) {
            stringBuilder.append(XWPFDocumentUtil.SPACE);
        }
        stringBuilder.append(needSpaceString);
        for (int i = 0; i < rightSpace; i++) {
            stringBuilder.append(XWPFDocumentUtil.SPACE);
        }

        setElementText(paragraph, stringBuilder.toString());
    }

    /**
     * 设置封面(姓名 专业 学院 学号 指导教师 职称)元素的文字
     *
     * @param paragraph 所在段落
     * @param value     所需设的值
     * @return 新创建的XWPFRun
     */
    @NonNull
    private XWPFRun setElementText(@NonNull XWPFParagraph paragraph, String value) {
        XWPFRun xwpfRun = paragraph.createRun();
        XWPFDocumentUtil.setFont(xwpfRun, XWPFDocumentUtil.FONT_KAI_TI_GB2312, 16);
        xwpfRun.setUnderline(UnderlinePatterns.SINGLE);
        xwpfRun.setBold(true);
        xwpfRun.setText(value);

        return xwpfRun;
    }
}
