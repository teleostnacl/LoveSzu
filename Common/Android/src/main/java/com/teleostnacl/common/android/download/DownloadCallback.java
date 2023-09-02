package com.teleostnacl.common.android.download;

public interface DownloadCallback {
    //下载完成之后的回掉
    default void onComplete() { }

    //下载过程中点击的回掉
    default void onClick() { }
}
