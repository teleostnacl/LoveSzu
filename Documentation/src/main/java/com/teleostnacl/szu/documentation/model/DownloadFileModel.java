package com.teleostnacl.szu.documentation.model;

import androidx.annotation.NonNull;

/**
 * 下载文件使用的Model
 */
public class DownloadFileModel {
    public final String fileName;

    public final String orderId;

    public boolean success = true;

    public DownloadFileModel(String fileName, String orderId) {
        this.fileName = fileName;
        this.orderId = orderId;
    }

    public DownloadFileModel() {
        success = false;
        this.fileName = null;
        this.orderId = null;
    }

    public DownloadFileModel(@NonNull DocumentationModel documentationModel, @NonNull HistoryFileModel historyFileModel) {
        this(documentationModel.name + " - " + documentationModel.userId + " - " + historyFileModel.title + ".pdf",
                historyFileModel.orderId);
    }

    public DownloadFileModel(@NonNull DocumentationModel documentationModel, @NonNull ItemModel itemModel, String s) {
        this(documentationModel.name + " - " + documentationModel.userId + " - " + itemModel.name + ".pdf", s);
    }

    public String getUrl() {
        return "https://jwzzfw.szu.edu.cn/wec-self-print-app-console/order/download/" + orderId;
    }
}
