package com.teleostnacl.szu.documentation.repository;

import androidx.annotation.NonNull;

import com.teleostnacl.szu.documentation.model.DocumentationModel;
import com.teleostnacl.szu.documentation.model.HistoryFileModel;
import com.teleostnacl.szu.documentation.model.ItemModel;
import com.teleostnacl.szu.documentation.retrofit.DocumentationApi;
import com.teleostnacl.szu.documentation.retrofit.DocumentationRetrofit;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;

import org.jsoup.Jsoup;

import java.util.Date;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.ResponseBody;

public class DocumentationRepository {
    private final DocumentationApi api = DocumentationRetrofit.getInstance().getApi();

    /**
     * 初始化
     *
     * @return 是否初始化成功
     */
    public Single<Boolean> init(DocumentationModel documentationModel) {
        return initResponseBody().map(responseBody -> "自助打印".equals(Jsoup.parse(responseBody.string()).title()))
                .flatMap((Function<Boolean, SingleSource<Boolean>>) aBoolean -> {
                    if (!aBoolean) {
                        return Single.just(false);
                    } else {
                        // 获取个人信息并获取历史下载记录 同时获取可下载证明文件的列表
                        return Single.zip(getUserInfo(documentationModel), getItemList(documentationModel),
                                (o, o2) -> true);
                    }
                });
    }

    /**
     * 初始化并进行登录
     *
     * @return 登录成功后的响应
     */
    private Single<ResponseBody> initResponseBody() {
        return api.init().flatMap(LoginUtil.checkLoginHandleSingle(this::initResponseBody));
    }

    /**
     * @return 获取个人信息
     */
    private Single<DocumentationModel> getUserInfo(DocumentationModel documentationModel) {
        return api.getUserInfo().map(responseBody -> {
            String tmp = responseBody.string().split("\"user\":\\{")[1];
            documentationModel.userId = tmp.split("\"id\":\"")[1].split("\"")[0];
            documentationModel.name = tmp.split("\"name\":\"")[1].split("\"")[0];

            return documentationModel;
        });
    }

    /**
     * @return 获取历史下载列表
     */
    private Single<DocumentationModel> getHistoryFileList(@NonNull DocumentationModel documentationModel) {
        return api.getHistoryFileList(documentationModel.getHistoryFileListMap()).map(historyFileModelJson -> {
            documentationModel.historyFileModels.clear();
            long time = new Date().getTime();
            for (HistoryFileModel model : historyFileModelJson.data.orders) {
                // 排除 非打印的 已经过期了的 不存在则添加, 存在则留过期日最久的
                documentationModel.historyFileModels.compute(model.itemId, (s, historyFileModel) -> {
                    if (model.takeType == null || model.takeType != 2 || model.getExpireTime() < time) {
                        return historyFileModel;
                    } else if (historyFileModel == null) {
                        return model;
                    } else {
                        return model.compareTo(historyFileModel) > 0 ? model : historyFileModel;
                    }
                });
            }

            return documentationModel;
        });
    }

    /**
     * @return 获取可下载证明的列表
     */
    private Single<DocumentationModel> getItemList(@NonNull DocumentationModel documentationModel) {
        return api.getItemList(documentationModel.getItemListMap()).map(itemModelJson -> {
            documentationModel.itemModels.clear();
            documentationModel.itemModels.addAll(itemModelJson.data.records);
            return documentationModel;
        });
    }

    /**
     * 获取下载链接
     *
     * @return 是否获取成功
     */
    public Single<String> getDownloadLink(@NonNull DocumentationModel documentationModel,
                                          @NonNull ItemModel itemModel) {
        return api.create(documentationModel.getDownloadLinkMap(itemModel.id, itemModel.reportUrlPath))
                .map(responseBody -> responseBody.string().split("\"orderId\":\"")[1].split("\"")[0]);
    }
}
