package com.teleostnacl.szu.record.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagingData;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.paging.PagingUtils;
import com.teleostnacl.szu.record.model.GrowthRecordModel;
import com.teleostnacl.szu.record.model.SemesterModel;
import com.teleostnacl.szu.record.repository.GrowthRecordRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class GrowthRecordViewModel extends ViewModel {
    private final GrowthRecordRepository growthRecordRepository = new GrowthRecordRepository();

    private final GrowthRecordModel growthRecordModel = new GrowthRecordModel();

    private final Map<SemesterModel, Flowable<PagingData<SemesterModel>>> flowableMap = new HashMap<>();

    /**
     * 获取全部学期
     *
     * @return 是否成功
     */
    public Single<Boolean> getAllSemesters() {
        return growthRecordRepository.getAllSemesters(growthRecordModel)
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                });
    }

    /**
     * 获取指定学期的成长记录
     */
    public Flowable<PagingData<SemesterModel>> getSemesterGrowthRecord(SemesterModel semesterModel) {
        return flowableMap.computeIfAbsent(semesterModel, semesterModel1 ->
                PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, SemesterModel>() {
                    @NonNull
                    @Override
                    public Single<LoadResult<Integer, SemesterModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                        return growthRecordRepository.getSemesterGrowthRecord(getGrowthRecordModel(), semesterModel)
                                .map((Function<SemesterModel, LoadResult<Integer, SemesterModel>>) semesterModel2 ->
                                        new LoadResult.Page<>(Collections.singletonList(semesterModel), null, null))
                                .onErrorReturn(throwable -> {
                                    NetworkUtils.errorHandle(throwable);
                                    return new LoadResult.Error<>(throwable);
                                });
                    }

                    @Nullable
                    @Override
                    public Integer getRefreshKey(@NonNull PagingState<Integer, SemesterModel> pagingState) {
                        return null;
                    }
                }));
    }

    public GrowthRecordModel getGrowthRecordModel() {
        return growthRecordModel;
    }
}
