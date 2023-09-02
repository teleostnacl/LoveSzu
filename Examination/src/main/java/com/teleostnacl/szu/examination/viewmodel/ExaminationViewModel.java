package com.teleostnacl.szu.examination.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagingData;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.paging.PagingUtils;
import com.teleostnacl.szu.examination.model.ExaminationDataModel;
import com.teleostnacl.szu.examination.model.ExaminationModel;
import com.teleostnacl.szu.examination.repository.ExaminationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class ExaminationViewModel extends ViewModel {
    private final ExaminationModel examinationModel = new ExaminationModel();

    private final ExaminationRepository examinationRepository = new ExaminationRepository();

    // 展示考试信息的Flowable
    private final Map<String, Flowable<PagingData<ExaminationDataModel>>> examinationDataModelsFlowable = new HashMap<>();

    /**
     * 初始化
     *
     * @return 是否初始化成功
     */
    public Single<Boolean> init() {
        return examinationRepository.init(examinationModel)
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return 获取期末考试安排数据
     */
    public Flowable<PagingData<ExaminationDataModel>> getExaminationDataModelsFlowable(String xqxm) {
        return examinationDataModelsFlowable.computeIfAbsent(xqxm, s -> PagingUtils.getFlowable(ExaminationViewModel.this,
                () -> new RxPagingSource<Integer, ExaminationDataModel>() {
                    @NonNull
                    @Override
                    public Single<LoadResult<Integer, ExaminationDataModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                        List<ExaminationDataModel> models;

                        // 已经存在 则直接传值
                        if ((models = examinationModel.examinationDatas.get(xqxm)) != null) {
                            return Single.just(new LoadResult.Page<>(models, null, null));
                        }
                        // 从服务器获取
                        else {
                            return examinationRepository.getExaminationData(examinationModel, xqxm)
                                    .map((Function<List<ExaminationDataModel>, LoadResult<Integer, ExaminationDataModel>>) examinationDataModels ->
                                            new LoadResult.Page<>(examinationDataModels, null, null))
                                    .onErrorReturn(throwable -> {
                                        NetworkUtils.errorHandle(throwable);
                                        return new LoadResult.Error<>(throwable);
                                    });
                        }
                    }

                    @Nullable
                    @Override
                    public Integer getRefreshKey(@NonNull PagingState<Integer, ExaminationDataModel> pagingState) {
                        return null;
                    }
                }));
    }

    public ExaminationModel getExaminationModel() {
        return examinationModel;
    }
}
