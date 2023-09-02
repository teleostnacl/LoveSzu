package com.teleostnacl.szu.file.viewmodel;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.teleostnacl.common.android.download.DownloadBroadcastReceiver;
import com.teleostnacl.common.android.download.DownloadDefaultCallback;
import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.szu.file.FileActivity;
import com.teleostnacl.szu.file.R;
import com.teleostnacl.szu.file.model.FileModel;
import com.teleostnacl.szu.file.repository.FileRepository;
import com.teleostnacl.szu.libs.model.NeumorphCardViewTextWithIconModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FileViewModel extends ViewModel {
    private final FileRepository fileRepository = new FileRepository();

    // json原始数据
    private final List<FileModel> fileModels = new ArrayList<>();

    public Single<List<NeumorphCardViewTextWithIconModel>> getFileModels() {
        return Single.just(new Object())
                // 切换线程
                .subscribeOn(Schedulers.io())
                .flatMap(o -> fileRepository.readJson())
                .map(fileModels -> {
                    this.fileModels.clear();
                    this.fileModels.addAll(fileModels);

                    return fileModels2Models();
                })
                .onErrorReturnItem(new ArrayList<>())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 搜索
     *
     * @param keyword 关键字
     * @return 含关键字的项
     */
    public Single<List<NeumorphCardViewTextWithIconModel>> search(@NonNull String keyword) {
        return Single.just(new Object())
                .subscribeOn(Schedulers.computation())
                .map(o -> {
                    if (TextUtils.isEmpty(keyword)) {
                        return fileModels2Models();
                    }

                    String[] keys = keyword.split("\\s+");

                    List<NeumorphCardViewTextWithIconModel> list = new ArrayList<>();

                    // 遍历fileModels 保留关键字都被包含的项
                    for (FileModel model : fileModels) {
                        boolean contain = true;

                        // 遍历key 检查是否都包含
                        for (String key : keys) {
                            // 不包含某个关键字 则 忽略该项
                            if (!model.fileName.contains(key)) {
                                contain = false;
                                break;
                            }
                        }

                        // 关键字都包含 则选择
                        if (contain) {
                            list.add(getModel(model));
                        }
                    }

                    return list;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 从FileModel 创建成 FileActivity.Model
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private FileActivity.Model getModel(@NonNull FileModel model) {
        return new FileActivity.Model(model.fileName,
                Objects.requireNonNull(ResourcesUtils.getDrawable(R.drawable.drawable_file_download)), () -> {
            // 创建下载器
            DownloadManager downloadManager = (DownloadManager) ContextUtils.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(model.fileUrl))
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, model.fileName + "." + model.fileFormat)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // 开始下载
            ToastUtils.makeToast(com.teleostnacl.common.android.R.string.download_start, model.fileName);
            long id = downloadManager.enqueue(request);

            // 注册监听器
            DownloadBroadcastReceiver.addDownloadCallback(id, new DownloadDefaultCallback(model.fileName, downloadManager, id));
        },
                model.fileUrl, model.webVpnUrl);
    }

    /**
     * 将fileModels列表转为成NeumorphCardViewTextWithIconModel列表
     */
    @NonNull
    private List<NeumorphCardViewTextWithIconModel> fileModels2Models() {
        List<NeumorphCardViewTextWithIconModel> list = new ArrayList<>();

        for (FileModel fileModel : fileModels) {
            list.add(getModel(fileModel));
        }

        return list;
    }
}
