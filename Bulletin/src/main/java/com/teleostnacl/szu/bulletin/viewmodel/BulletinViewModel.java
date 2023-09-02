package com.teleostnacl.szu.bulletin.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagingData;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.paging.PagingUtils;
import com.teleostnacl.szu.bulletin.model.Bulletin;
import com.teleostnacl.szu.bulletin.model.BulletinModel;
import com.teleostnacl.szu.bulletin.repository.BulletinRepository;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class BulletinViewModel extends ViewModel {
    private final BulletinRepository bulletinRepository = new BulletinRepository();

    public final BulletinModel bulletinModel = new BulletinModel();

    private final Map<String, Flowable<PagingData<Bulletin>>> infoTypeBulletinsPagingDataMap = new HashMap<>();

    private Map<String, Flowable<PagingData<Bulletin>>> searchBulletinsPagingData;

    // 显示公文通详细信息的公文通
    private Bulletin bulletin;

    /**
     * 检查登录
     *
     * @return 是否登录成功
     */
    public Single<Boolean> checkLogin() {
        return LoginUtil.checkLogin().flatMap(aBoolean -> {
                    if (aBoolean) {
                        return bulletinRepository.getInfo(bulletinModel);
                    } else {
                        return Single.just(false);
                    }
                })
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param infoType 公文通类别
     * @return 返回指定公文通类别的flowable
     */
    public Flowable<PagingData<Bulletin>> getInfoTypeBulletinsPagingData(String infoType) {
        // map中没有则新建
        return infoTypeBulletinsPagingDataMap.computeIfAbsent(infoType, s -> PagingUtils.getFlowable(
                BulletinViewModel.this, () -> new RxPagingSource<Integer, Bulletin>() {
                    @NonNull
                    @Override
                    public Single<LoadResult<Integer, Bulletin>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                        return bulletinRepository.getBulletinsByInfoType(infoType)
                                .map((Function<List<Bulletin>, LoadResult<Integer, Bulletin>>) bulletins ->
                                        new LoadResult.Page<>(bulletins, null, null))
                                .onErrorReturn(throwable -> {
                                    NetworkUtils.errorHandle(throwable);
                                    return new LoadResult.Error<>(throwable);
                                });
                    }

                    @Nullable
                    @Override
                    public Integer getRefreshKey(@NonNull PagingState<Integer, Bulletin> pagingState) {
                        return null;
                    }
                }));
    }

    /**
     * @return 搜索结果分页Flowable
     */
    public Flowable<PagingData<Bulletin>> getSearchBulletinsResultPagingData() {
        return PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, Bulletin>() {
            @NonNull
            @Override
            public Single<LoadResult<Integer, Bulletin>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                return bulletinRepository.getBulletinsBySearch(bulletinModel)
                        .map((Function<List<Bulletin>, LoadResult<Integer, Bulletin>>) bulletins ->
                                new LoadResult.Page<>(bulletins, null, null))
                        .onErrorReturn(throwable -> {
                            NetworkUtils.errorHandle(throwable);
                            return new LoadResult.Error<>(throwable);
                        });
            }

            @Nullable
            @Override
            public Integer getRefreshKey(@NonNull PagingState<Integer, Bulletin> pagingState) {
                return null;
            }
        });
    }

//    /**
//     * 获取公文通详细信息
//     *
//     * @param bulletin 公文通
//     * @return 是否成功
//     */
//    public Single<Boolean> getBulletinContent(Bulletin bulletin) {
//        return bulletinRepository.getBulletinContent(bulletin)
//                .onErrorReturn(throwable -> {
//                    NetworkUtils.errorHandle(throwable);
//                    return false;
//                })
//                .observeOn(AndroidSchedulers.mainThread());
//    }

    public Bulletin getBulletin() {
        return bulletin;
    }

    public void setBulletin(Bulletin bulletin) {
        this.bulletin = bulletin;
    }
}
