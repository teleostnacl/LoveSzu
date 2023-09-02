package com.teleostnacl.szu.paper.util;

import androidx.annotation.NonNull;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import java.math.BigInteger;

/**
 * 工具类
 */
public class XWPFDocumentUtil {
    // region 常用字体名
    public static final String FONT_SONG_TI = "宋体";
    public static final String FONT_HUA_WEN_XIN_KAI = "华文行楷";
    public static final String FONT_HEI_TI = "黑体";
    public static final String FONT_KAI_TI_GB2312 = "楷体_GB2312";
    public static final String FONT_TIMES_NEW_ROMAN = "Times New Roman";
    public static final String FONT_HUA_WEN_ZHONG_SONG = "华文中宋";
    // endregion

    /**
     * 半角空格
     */
    public static final String SPACE = " ";

    /**
     * 磅转twips(word的度量单位) 1磅 = 20twips
     */
    public static int pt2twip(double pt) {
        return (int) Math.round(pt * 20);
    }

    /**
     * 设置使用的字体名和字体大小
     *
     * @param xwpfRun  需要设置的文字
     * @param fontName 字体名
     * @param fontSize 字体大小
     */
    public static void setFont(@NonNull XWPFRun xwpfRun, @NonNull String fontName, double fontSize) {
        xwpfRun.setFontFamily(fontName);
        xwpfRun.setFontSize(fontSize);
    }

    /**
     * 将指定文字的西文字体设置为TimesNewRoman
     *
     * @param xwpfRun 指定的文字
     */
    public static void setFontTimesNewRoman(@NonNull XWPFRun xwpfRun) {
        xwpfRun.setFontFamily(FONT_TIMES_NEW_ROMAN, XWPFRun.FontCharRange.ascii);
        xwpfRun.setFontFamily(FONT_TIMES_NEW_ROMAN, XWPFRun.FontCharRange.cs);
        xwpfRun.setFontFamily(FONT_TIMES_NEW_ROMAN, XWPFRun.FontCharRange.hAnsi);
    }

    /**
     * 给文档插入分节符
     *
     * @param document 文档
     * @return 分节符
     */
    public static CTSectPr addNewSectPr(@NonNull XWPFDocument document) {
        document.createParagraph();
        XWPFParagraph paragraph = document.createParagraph();
        CTPPr ctpPr = paragraph.getCTP().getPPr();
        if (ctpPr == null) {
            ctpPr = CTPPr.Factory.newInstance();
        }
        CTSectPr sectPr = ctpPr.addNewSectPr();
        paragraph.getCTP().setPPr(ctpPr);

        return sectPr;
    }

    /**
     * 给段落设置大纲级别
     */
    public static void setOutlineLvl(@NonNull XWPFParagraph paragraph, int outlineLvl) {
        CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
        indentNumber.setVal(BigInteger.valueOf(outlineLvl));
        CTPPr ppr = paragraph.getCTPPr();
        ppr.setOutlineLvl(indentNumber);
    }
}
