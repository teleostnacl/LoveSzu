package com.teleostnacl.szu.scheme.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagingData;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.paging.PagingUtils;
import com.teleostnacl.szu.scheme.json.SchemeJson;
import com.teleostnacl.szu.scheme.model.SchemeDetailModel;
import com.teleostnacl.szu.scheme.model.SchemeModel;
import com.teleostnacl.szu.scheme.repository.SchemeRepository;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class SchemeViewModel extends ViewModel {
    private final SchemeRepository schemeRepository = new SchemeRepository();

    private final SchemeModel schemeModel = new SchemeModel();

    private final Map<Integer, Flowable<PagingData<SchemeDetailModel>>> yearSchemeModels = new HashMap<>();

    // 记录展示培养方案的model
    private SchemeDetailModel mSchemeDetailModel;

    /**
     * @return 获取指定年份索引的培养方案 并使用PagingData
     */
    public Flowable<PagingData<SchemeDetailModel>> getYearSchemeModel(int yearIndex) {
        return yearSchemeModels.computeIfAbsent(yearIndex, integer ->
                PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, SchemeDetailModel>() {
                    @NonNull
                    @Override
                    public Single<LoadResult<Integer, SchemeDetailModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                        Integer nextPageNumber = loadParams.getKey();

                        if (nextPageNumber == null) {
                            nextPageNumber = 1;
                        }

                        return schemeRepository.getAllSchemeByYear(schemeModel, yearIndex, nextPageNumber)
                                .map((Function<SchemeJson, LoadResult<Integer, SchemeDetailModel>>) schemeJson -> {
                                    int sum = (schemeJson.datas.qxpyfacx.totalSize - 1) /
                                            schemeJson.datas.qxpyfacx.pageSize + 1;
                                    int current = schemeJson.datas.qxpyfacx.pageNumber;

                                    return new LoadResult.Page<>(
                                            schemeJson.datas.qxpyfacx.rows,
                                            current > 1 ? current - 1 : null,
                                            current < sum ? current + 1 : null);
                                })
                                .onErrorReturn(throwable -> {
                                    NetworkUtils.errorHandle(throwable);
                                    return new LoadResult.Error<>(throwable);
                                });
                    }

                    @Nullable
                    @Override
                    public Integer getRefreshKey(@NonNull PagingState<Integer, SchemeDetailModel> pagingState) {
                        return null;
                    }
                }));
    }

    /**
     * 获取所有可查询的年份
     */
    public Single<Boolean> getAllYears() {
        return schemeRepository.getAllYear(getSchemeModel())
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 获取指定培养方案所有课程
     */
    public Single<Boolean> getSchemeDetail() {
        return schemeRepository.getSchemeDetailGroups(getSchemeModel(), getSchemeDetailModel())
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public SchemeModel getSchemeModel() {
        return schemeModel;
    }

    public SchemeDetailModel getSchemeDetailModel() {
        return mSchemeDetailModel;
    }

    public void setSchemeDetailModel(SchemeDetailModel schemeDetailGroup) {
        this.mSchemeDetailModel = schemeDetailGroup;
    }
}
