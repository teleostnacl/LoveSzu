package com.teleostnacl.szu.paper.helper;

import androidx.annotation.NonNull;

import com.teleostnacl.szu.paper.util.XWPFDocumentUtil;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 * 生成目录的Helper类
 */
public class CatalogueHelper {
    private static final String CATALOGUE_TITLE = "目  录";

    public static void createCatalogueTitle(@NonNull XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        XWPFDocumentUtil.setFont(run, XWPFDocumentUtil.FONT_HEI_TI, 16);
        run.setText(CATALOGUE_TITLE);
        // 添加分节符
        XWPFDocumentUtil.addNewSectPr(document);
    }
}
