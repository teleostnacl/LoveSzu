package com.teleostnacl.common.android.download;

import android.app.DownloadManager;

import com.teleostnacl.common.android.R;
import com.teleostnacl.common.android.context.ToastUtils;

/**
 * 默认的下载回调
 */
public class DownloadDefaultCallback implements DownloadCallback {
    //取消事件效应后,不再效应完成事件
    private boolean cancelFlag = false;

    private final String fileName;
    private final DownloadManager downloadManager;
    private final long id;

    public DownloadDefaultCallback(String fileName, DownloadManager downloadManager, long id) {
        this.fileName = fileName;
        this.downloadManager = downloadManager;
        this.id = id;
    }

    @Override
    public void onComplete() {
        if (!cancelFlag) {
            ToastUtils.makeToast(R.string.download_complete, fileName);
        }
    }

    @Override
    public void onClick() {
        cancelFlag = true;
        //点击事件取消下载
        ToastUtils.makeToast(R.string.download_cancel, fileName);
        downloadManager.remove(id);
    }
}
