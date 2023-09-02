package com.teleostnacl.szu.documentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.szu.documentation.model.DocumentationModel;
import com.teleostnacl.szu.documentation.model.DownloadFileModel;
import com.teleostnacl.szu.documentation.model.ItemModel;
import com.teleostnacl.szu.documentation.repository.DocumentationRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

public class DocumentationViewModel extends ViewModel {
    // 取消用的对象
    public static final ItemModel CANCEL = new ItemModel();

    private final DocumentationRepository documentationRepository = new DocumentationRepository();

    private final DocumentationModel documentationModel = new DocumentationModel();

    /**
     * 初始化
     *
     * @return 是否初始化成功
     */
    public Single<Boolean> init() {
        return documentationRepository.init(documentationModel).onErrorReturn(throwable -> {
            NetworkUtils.errorHandle(throwable);
            return false;
        }).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取下载链接
     *
     * @return 是否获取成功
     */
    public Single<DownloadFileModel> getDownloadLink(@NonNull ItemModel model) {
        return documentationRepository.getDownloadLink(documentationModel, model)
                .map(s -> new DownloadFileModel(documentationModel, model, s))
                .onErrorResumeNext(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return Single.just(new DownloadFileModel());
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public DocumentationModel getDocumentationModel() {
        return documentationModel;
    }
}
