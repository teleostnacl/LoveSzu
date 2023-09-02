package com.teleostnacl.common.android.download;

import static android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE;
import static android.app.DownloadManager.ACTION_NOTIFICATION_CLICKED;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class DownloadBroadcastReceiver extends BroadcastReceiver {

    //统一管理下载的回调
    private static final Map<Long, DownloadCallback> downloadCallbackMap = new HashMap<>();

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        //下载完成的回掉
        if (ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            //取出下载id
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            if (contains(id)) {
                getDownloadCallback(id).onComplete();
                //移出回掉
                removeDownloadCallback(id);
            }
        }
        //下载中途点击的回掉
        else if (ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
            //取出下载ids
            long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            for (long id : ids) {
                if (contains(id)) getDownloadCallback(id).onClick();
            }
        }
    }

    public static boolean contains(long id) {
        return downloadCallbackMap.containsKey(id);
    }

    public static DownloadCallback getDownloadCallback(long id) {
        return downloadCallbackMap.get(id);
    }

    public static void addDownloadCallback(long id, DownloadCallback downloadCallback) {
        downloadCallbackMap.put(id, downloadCallback);
    }

    public static void removeDownloadCallback(long id) {
        downloadCallbackMap.remove(id);
    }
}

