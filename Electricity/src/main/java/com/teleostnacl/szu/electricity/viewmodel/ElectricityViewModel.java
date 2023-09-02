package com.teleostnacl.szu.electricity.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.paging.PagingData;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.paging.PagingUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.electricity.model.ElectricityDateModel;
import com.teleostnacl.szu.electricity.model.ElectricityModel;
import com.teleostnacl.szu.electricity.repository.ElectricityRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class ElectricityViewModel extends AndroidViewModel {
    private final ElectricityRepository electricityRepository = new ElectricityRepository();
    public final ElectricityModel electricityModel = new ElectricityModel();

    private Flowable<PagingData<ElectricityDateModel>> electricityFlowable;

    public ElectricityViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * @return 获取校区并检查是否可连通查询电费服务
     */
    public Single<Boolean> check() {
        return electricityRepository.check(electricityModel)
                .onErrorReturnItem(false)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return 获取所有楼栋信息
     */
    public Single<Boolean> getBuilding(String client) {
        electricityModel.clientName = client;
        electricityModel.client = electricityModel.clientMap.get(client);

        return electricityRepository.getBuilding(electricityModel)
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param room 房间号
     * @return 检查房间号是否有效
     */
    public Single<Boolean> checkRoomId(int room) {
        electricityModel.roomName = room;

        return electricityRepository.checkRoomId(electricityModel)
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return 展示数据的Model, 并实现分页
     */
    public Flowable<PagingData<ElectricityDateModel>> getElectricityFlowable() {
        if (electricityFlowable == null) {
            electricityFlowable = PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, ElectricityDateModel>() {

                private final int current = NumberUtils.parseInt(
                        new SimpleDateFormat("yyyyMM", Locale.CHINA).format(new Date().getTime()), 0);

                @NonNull
                @Override
                public Single<LoadResult<Integer, ElectricityDateModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                    Integer nextPageNumber = loadParams.getKey();
                    if (nextPageNumber == null) {
                        // 尝试获取储存在数据库中时间
                        if (!electricityModel.useLocal ||
                                (nextPageNumber = electricityModel.getLocalDate()) == 0) {
                            nextPageNumber = current;
                        }
                    }

                    // 除100 取整数 为年
                    int year = nextPageNumber / 100;
                    // 除100 取余数 为月
                    int month = nextPageNumber % 100;

                    // 如果为0, 则年份减1, 月份设为12
                    if (month == 0) {
                        year -= 1;
                        month = 12;
                        nextPageNumber = year * 100 + month;
                    }
                    // 如果为13 则年份+1 月份设为1
                    else if (month == 13) {
                        year += 1;
                        month = 1;
                        nextPageNumber = year * 100 + month;
                    }

                    int finalNextPageNumber = nextPageNumber;
                    return electricityRepository.getMonthData(electricityModel, nextPageNumber).map((Function<List<ElectricityDateModel>, LoadResult<Integer, ElectricityDateModel>>) electricityDateModels ->
                                    new LoadResult.Page<>(electricityDateModels,
                                            finalNextPageNumber < current ? finalNextPageNumber + 1 : null,
                                            electricityDateModels.size() != 0 ? finalNextPageNumber - 1 : null))
                            .onErrorReturn(throwable -> {
                                NetworkUtils.errorHandle(throwable);
                                return new LoadResult.Error<>(throwable);
                            });
                }

                @Nullable
                @Override
                public Integer getRefreshKey(@NonNull PagingState<Integer, ElectricityDateModel> pagingState) {
                    return null;
                }
            });
        }

        return electricityFlowable;
    }

    /**
     * 删除数据库所有的数据
     */
    public void deleteAll() {
        electricityRepository.deleteAll();
    }
}
