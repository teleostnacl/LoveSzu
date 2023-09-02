package com.teleostnacl.szu.paper.util;

import static org.apache.poi.xwpf.usermodel.XWPFRun.FontCharRange.ascii;
import static org.apache.poi.xwpf.usermodel.XWPFRun.FontCharRange.cs;
import static org.apache.poi.xwpf.usermodel.XWPFRun.FontCharRange.eastAsia;
import static org.apache.poi.xwpf.usermodel.XWPFRun.FontCharRange.hAnsi;

import androidx.annotation.NonNull;

import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPrGeneral;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;

import java.math.BigInteger;


public class StyleUtil {
    // region 方向常量
    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    // 两个段落使用相同的间距时才显示
    public static final int BETWEEN = 4;
    // endregion

    /**
     * 创建指定StyleID的Style
     *
     * @param styleId 指定的StyleID
     * @return 生成的Style
     */
    @NonNull
    public static XWPFStyle createStyle(@NonNull String styleId) {
        // 创建style的id
        CTStyle ctStyle = CTStyle.Factory.newInstance();
        ctStyle.setStyleId(styleId);
        // 显示的名称
        CTString styleName = CTString.Factory.newInstance();
        styleName.setVal(styleId);
        ctStyle.setName(styleName);

        // 创建style
        XWPFStyle style = new XWPFStyle(ctStyle);
        style.setType(STStyleType.PARAGRAPH);

        return style;
    }

    /**
     * 将style添加进styles中
     */
    public static void addStyle(@NonNull XWPFStyles styles, @NonNull XWPFStyle style) {
        styles.addStyle(style);
    }

    /**
     * 为指定的Style设置字体名
     *
     * @param style    指定的Style
     * @param fontName 字体名
     */
    public static void setFontName(@NonNull XWPFStyle style, @NonNull String fontName) {
        setFontName(style, cs, fontName);
        setFontName(style, ascii, fontName);
        setFontName(style, eastAsia, fontName);
        setFontName(style, hAnsi, fontName);
    }

    /**
     * 为指定Style的指定类型设置字体
     *
     * @param style    指定的Style
     * @param fcr      指定类型的字符
     * @param fontName 字体名
     */
    public static void setFontName(@NonNull XWPFStyle style, @NonNull XWPFRun.FontCharRange fcr, @NonNull String fontName) {
        CTStyle ctStyle = style.getCTStyle();
        CTRPr rpr = getRPr(ctStyle);

        // 设置字体名称
        CTFonts fonts;
        CTFonts[] ctFonts = rpr.getRFontsArray();
        if (ctFonts != null && ctFonts.length > 0) {
            fonts = ctFonts[0];
        } else {
            fonts = CTFonts.Factory.newInstance();
        }
        switch (fcr) {
            case cs:
                fonts.setCs(fontName);
                break;
            case ascii:
                fonts.setAscii(fontName);
                break;
            case eastAsia:
                fonts.setEastAsia(fontName);
                break;
            case hAnsi:
                fonts.setHAnsi(fontName);
                break;
        }

        // 设置字体信息
        rpr.setRFontsArray(new CTFonts[]{fonts});
        ctStyle.setRPr(rpr);
    }

    /**
     * 为指定的Style设置字体大小
     *
     * @param style    指定的Style
     * @param fontSize 字体大小
     */
    public static void setFontSize(@NonNull XWPFStyle style, double fontSize) {
        CTStyle ctStyle = style.getCTStyle();
        CTRPr rpr = getRPr(ctStyle);

        // 字号
        CTHpsMeasure size = CTHpsMeasure.Factory.newInstance();
        size.setVal(new BigInteger(String.valueOf(Math.round(fontSize * 2))));
        rpr.setSzArray(new CTHpsMeasure[]{size});
        rpr.setSzCsArray(new CTHpsMeasure[]{size});

        // 设置字体信息
        ctStyle.setRPr(rpr);
    }

    /**
     * 为指定Style设置字体是否加粗
     *
     * @param style 指定style
     * @param value 字体是否加粗
     */
    public static void setFontBold(@NonNull XWPFStyle style, boolean value) {
        CTStyle ctStyle = style.getCTStyle();
        CTRPr rpr = getRPr(ctStyle);

        CTOnOff bold = rpr.sizeOfBArray() > 0 ? rpr.getBArray(0) : rpr.addNewB();
        bold.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
        rpr.setBArray(new CTOnOff[]{bold});

        ctStyle.setRPr(rpr);
        style.setStyle(ctStyle);
    }

    /**
     * 为Style设置主题的级别
     */
    public static void setOutlineLvl(@NonNull XWPFStyle style, int headingLevel) {
        CTStyle ctStyle = style.getCTStyle();

        CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
        indentNumber.setVal(BigInteger.valueOf(headingLevel));
        ctStyle.setUiPriority(indentNumber);

        CTPPrGeneral ppr = getPPr(ctStyle);
        ppr.setOutlineLvl(indentNumber);
        ctStyle.setPPr(ppr);
    }

    /**
     * 设置指定主题的对齐方式
     *
     * @param style 指定主题
     * @param align 对齐方式100
     */
    public static void setAlignment(@NonNull XWPFStyle style, @NonNull ParagraphAlignment align) {
        CTStyle ctStyle = style.getCTStyle();
        CTPPrGeneral ppr = getPPr(ctStyle);

        CTJc jc = ppr.isSetJc() ? ppr.getJc() : ppr.addNewJc();
        STJc.Enum en = STJc.Enum.forInt(align.getValue());
        jc.setVal(en);
        ctStyle.setPPr(ppr);
    }

    /**
     * 设置指定主题的行距
     *
     * @param style   指定主题
     * @param spacing 行距的倍数
     */
    public static void setSpacingBetween(@NonNull XWPFStyle style, double spacing) {
        CTStyle ctStyle = style.getCTStyle();
        CTPPrGeneral ppr = getPPr(ctStyle);
        CTSpacing ct = getSpacing(ppr);

        ct.setLine(new BigInteger(String.valueOf(Math.round(spacing * 240.0))));
        ct.setLineRule(STLineSpacingRule.Enum.forInt(LineSpacingRule.AUTO.getValue()));
        ctStyle.setPPr(ppr);
    }

    /**
     * 设置主题段前的行距
     *
     * @param style  指定主题
     * @param spaces 行距的倍数
     */
    public static void setSpacingAfterLines(@NonNull XWPFStyle style, double spaces) {
        CTStyle ctStyle = style.getCTStyle();
        CTPPrGeneral ppr = getPPr(ctStyle);
        CTSpacing ct = getSpacing(ppr);

        BigInteger bi = new BigInteger(String.valueOf(Math.round(spaces * 100)));
        ct.setAfterLines(bi);
        ctStyle.setPPr(ppr);
    }

    /**
     * 设置主题段后的行距
     *
     * @param style  指定主题
     * @param spaces 行距的倍数
     */
    public static void setSpacingBeforeLines(@NonNull XWPFStyle style, double spaces) {
        CTStyle ctStyle = style.getCTStyle();
        CTPPrGeneral ppr = getPPr(ctStyle);
        CTSpacing ct = getSpacing(ppr);

        BigInteger bi = new BigInteger(String.valueOf(Math.round(spaces * 100)));
        ct.setBeforeLines(bi);
        ctStyle.setPPr(ppr);
    }

    /**
     * 设置主题段前段后的行距
     *
     * @param style  指定主题
     * @param spaces 行距的倍数
     */
    public static void setSpacingLines(@NonNull XWPFStyle style, double spaces) {
        setSpacingBeforeLines(style, spaces);
        setSpacingAfterLines(style, spaces);
    }

    /**
     * 设置主题首行缩进
     *
     * @param style       指定主题
     * @param fontSize    字体大小
     * @param indentation 首行缩进字符数
     */
    public static void setIndentationFirstLine(@NonNull XWPFStyle style, double fontSize, double indentation) {
        CTStyle ctStyle = style.getCTStyle();
        CTPPrGeneral ppr = getPPr(ctStyle);
        CTInd ct = ppr.getInd();
        if (ct == null) {
            ct = ppr.addNewInd();
        }
        BigInteger bi = new BigInteger(String.valueOf(XWPFDocumentUtil.pt2twip(fontSize * indentation)));
        ct.setFirstLine(bi);

        ctStyle.setPPr(ppr);
    }

    /**
     * 给主题设置边框
     *
     * @param style       指定主题
     * @param orientation 方向
     * @param border      边框类型
     */
    public static void setBorder(@NonNull XWPFStyle style, int orientation, Borders border) {
        CTStyle ctStyle = style.getCTStyle();
        CTPPrGeneral pr = getPPr(ctStyle);
        CTPBdr ct = pr.getPBdr();
        if (ct == null) {
            ct = pr.addNewPBdr();
        }
        CTBorder ctBorder = null;

        switch (orientation) {
            case TOP:
                ctBorder = ct.isSetTop() ? ct.getTop() : ct.addNewTop();
                if (border.getValue() == Borders.NONE.getValue()) {
                    ct.unsetTop();
                }
                break;
            case BOTTOM:
                ctBorder = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
                if (border.getValue() == Borders.NONE.getValue()) {
                    ct.unsetBottom();
                }
                break;
            case LEFT:
                ctBorder = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
                if (border.getValue() == Borders.NONE.getValue()) {
                    ct.unsetLeft();
                }
                break;
            case RIGHT:
                ctBorder = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
                if (border.getValue() == Borders.NONE.getValue()) {
                    ct.unsetRight();
                }
                break;
            case BETWEEN:
                ctBorder = ct.isSetBetween() ? ct.getBetween() : ct.addNewBetween();
                if (border.getValue() == Borders.NONE.getValue()) {
                    ct.unsetBetween();
                }
                break;
        }

        if (ctBorder != null && border.getValue() != Borders.NONE.getValue()) {
            ctBorder.setVal(STBorder.Enum.forInt(border.getValue()));
        }

        pr.setPBdr(ct);
        ctStyle.setPPr(pr);
    }

    @NonNull
    private static CTRPr getRPr(@NonNull CTStyle ctStyle) {
        CTRPr rpr = ctStyle.getRPr();
        if (rpr == null) {
            rpr = CTRPr.Factory.newInstance();
        }

        return rpr;
    }

    @NonNull
    private static CTPPrGeneral getPPr(@NonNull CTStyle ctStyle) {
        CTPPrGeneral ppr = ctStyle.getPPr();
        if (ppr == null) {
            ppr = CTPPrGeneral.Factory.newInstance();
        }
        return ppr;
    }

    @NonNull
    private static CTSpacing getSpacing(@NonNull CTPPrGeneral ppr) {
        CTSpacing ct = ppr.getSpacing();
        if (ct == null) {
            ct = ppr.addNewSpacing();
        }
        return ct;
    }
}

