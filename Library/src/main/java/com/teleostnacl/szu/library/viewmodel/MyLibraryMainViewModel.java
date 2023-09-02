package com.teleostnacl.szu.library.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagingData;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.paging.PagingUtils;
import com.teleostnacl.common.android.paging.model.PagingModel;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.user.BorrowHistoryModel;
import com.teleostnacl.szu.library.model.user.CollectionModel;
import com.teleostnacl.szu.library.model.user.CurrentBorrowModel;
import com.teleostnacl.szu.library.model.user.MyLibraryModel;
import com.teleostnacl.szu.library.model.user.ReservationModel;
import com.teleostnacl.szu.library.repository.MyLibraryRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class MyLibraryMainViewModel extends ViewModel {

    public final MyLibraryModel myLibraryModel = new MyLibraryModel();

    private final MyLibraryRepository myLibraryRepository = new MyLibraryRepository();

    // 用于给Emitter发送取消事件的MODEL
    public static final BookModel CANCEL_BOOK_MODEL = new BookModel();

    // 当前借阅图书的Flowable
    private Flowable<PagingData<CurrentBorrowModel>> currentBorrowModelsFlowable;

    // 预约的图书
    private Flowable<PagingData<ReservationModel>> reservableModelsFlowable;

    // 预借的图书
    private Flowable<PagingData<ReservationModel>> requestModelsFlowable;

    // 历史借阅的图书
    private Flowable<PagingData<BorrowHistoryModel>> borrowHistoryModelsFlowable;

    // 收藏的图书
    private Flowable<PagingData<CollectionModel>> collectionModelsFlowable;

    // 当前借阅图书的数量
    public final MutableLiveData<Integer> currentBorrowBooks = new MutableLiveData<>();

    /**
     * 获取个人信息
     *
     * @return 是否成功
     */
    public Single<Boolean> getUserInfo(boolean refresh) {
        if (!refresh && myLibraryModel.userInfoModel != null) {
            return Single.just(true);
        }

        return myLibraryRepository.getUserInfo(myLibraryModel)
                .flatMap(aBoolean -> getHistoryBorrowBooks())
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 修改联系方式
     *
     * @return 是否成功
     */
    public Observable<Boolean> modifyUserInfo() {
        return myLibraryRepository.modifyUserInfo(myLibraryModel.userInfoModel)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取历史借阅图书的总数
     *
     * @return 成功
     */
    private Single<Boolean> getHistoryBorrowBooks() {
        return myLibraryRepository.getBorrowHistoryModels(1)
                // 首先根据第一页获得的数据 得到总页数 并跳转到最后一页获取数据
                .flatMap(pagingModel -> {
                    // 总数一页 不重复请求
                    if (pagingModel.pages == 1) {
                        return Single.just(pagingModel);
                    }

                    return myLibraryRepository.getBorrowHistoryModels(pagingModel.pages);
                })
                .map(pagingModel -> {
                    // (总页数 - 1) * 10 + 最后一页的项数
                    myLibraryModel.historyBorrowBooks.postValue(
                            ((pagingModel.pages > 0 ? pagingModel.pages : 1) - 1) * 10 + pagingModel.models.size());
                    return true;
                });
    }

    /**
     * @return 记录当前借阅图书列表的Flowable, 并实现分页功能
     */
    public Flowable<PagingData<CurrentBorrowModel>> getCurrentBorrowModelsFlowable() {
        if (currentBorrowModelsFlowable == null) {
            currentBorrowModelsFlowable = PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, CurrentBorrowModel>() {
                @NonNull
                @Override
                public Single<LoadResult<Integer, CurrentBorrowModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                    return myLibraryRepository.getCurrentBorrowedBooks()
                            .map((Function<List<CurrentBorrowModel>, LoadResult<Integer, CurrentBorrowModel>>) list -> {
                                currentBorrowBooks.postValue(list.size());
                                return new LoadResult.Page<>(list, null, null);
                            })
                            .onErrorReturn(throwable -> {
                                currentBorrowBooks.postValue(0);
                                NetworkUtils.errorHandle(throwable);
                                return new LoadResult.Error<>(throwable);
                            });
                }

                @Nullable
                @Override
                public Integer getRefreshKey(@NonNull PagingState<Integer, CurrentBorrowModel> pagingState) {
                    return null;
                }

            });
        }

        return currentBorrowModelsFlowable;
    }

    /**
     * @return 记录我的预约图书列表的Flowable, 并实现分页功能
     */
    public Flowable<PagingData<ReservationModel>> getReservableModelsFlowable() {
        if (reservableModelsFlowable == null) {
            reservableModelsFlowable = PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, ReservationModel>() {
                @NonNull
                @Override
                public Single<LoadResult<Integer, ReservationModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                    return myLibraryRepository.getReservationModels()
                            .map((Function<List<ReservationModel>, LoadResult<Integer, ReservationModel>>) list -> new LoadResult.Page<>(list, null, null))
                            .onErrorReturn(throwable -> {
                                NetworkUtils.errorHandle(throwable);
                                return new LoadResult.Error<>(throwable);
                            });
                }

                @Nullable
                @Override
                public Integer getRefreshKey(@NonNull PagingState<Integer, ReservationModel> pagingState) {
                    return null;
                }
            });
        }

        return reservableModelsFlowable;
    }

    /**
     * 取消预约
     *
     * @param reservationModel 取消预约的模型
     * @return 是否成功
     */
    public Observable<Boolean> cancelReserve(ReservationModel reservationModel) {
        return myLibraryRepository.cancelReserve(reservationModel)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return 记录当我的预借图书列表的Flowable, 并实现分页功能
     */
    public Flowable<PagingData<ReservationModel>> getRequestModelsFlowable() {
        if (requestModelsFlowable == null) {
            requestModelsFlowable = PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, ReservationModel>() {
                @NonNull
                @Override
                public Single<LoadResult<Integer, ReservationModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                    Integer nextPageNumber = loadParams.getKey();
                    if (nextPageNumber == null) {
                        nextPageNumber = 1;
                    }

                    return myLibraryRepository.getRequestModels(nextPageNumber)
                            .map((Function<PagingModel<ReservationModel>, LoadResult<Integer, ReservationModel>>) requestModel ->
                                    new LoadResult.Page<>(requestModel.models,
                                            requestModel.current > 1 ? requestModel.current - 1 : null,
                                            requestModel.current < requestModel.pages ? requestModel.current + 1 : null))
                            .onErrorReturn(throwable -> {
                                NetworkUtils.errorHandle(throwable);
                                return new LoadResult.Error<>(throwable);
                            });
                }

                @Nullable
                @Override
                public Integer getRefreshKey(@NonNull PagingState<Integer, ReservationModel> pagingState) {
                    return null;
                }
            });
        }

        return requestModelsFlowable;
    }

    /**
     * 取消预借
     *
     * @param reservationModel 取消预借的模型
     * @return 是否成功
     */
    public Observable<Boolean> cancelBorrowAdvance(ReservationModel reservationModel) {
        return myLibraryRepository.cancelBorrowAdvance(reservationModel)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return 记录借阅历史图书列表的Flowable, 并实现分页功能
     */
    public Flowable<PagingData<BorrowHistoryModel>> getBorrowHistoryModelsFlowable() {
        if (borrowHistoryModelsFlowable == null) {
            borrowHistoryModelsFlowable = PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, BorrowHistoryModel>() {
                @NonNull
                @Override
                public Single<LoadResult<Integer, BorrowHistoryModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                    Integer nextPageNumber = loadParams.getKey();
                    if (nextPageNumber == null) {
                        nextPageNumber = 1;
                    }

                    return myLibraryRepository.getBorrowHistoryModels(nextPageNumber)
                            .map((Function<PagingModel<BorrowHistoryModel>, LoadResult<Integer, BorrowHistoryModel>>) pagingModel ->
                                    new LoadResult.Page<>(pagingModel.models,
                                            pagingModel.current > 1 ? pagingModel.current - 1 : null,
                                            pagingModel.current < pagingModel.pages ? pagingModel.current + 1 : null))
                            .onErrorReturn(throwable -> {
                                NetworkUtils.errorHandle(throwable);
                                return new LoadResult.Error<>(throwable);
                            });
                }

                @Nullable
                @Override
                public Integer getRefreshKey(@NonNull PagingState<Integer, BorrowHistoryModel> pagingState) {
                    return null;
                }
            });
        }

        return borrowHistoryModelsFlowable;
    }

    /**
     * @return 记录收藏图书列表的Flowable, 并实现分页功能
     */
    public Flowable<PagingData<CollectionModel>> getCollectionModelsFlowable() {
        if (collectionModelsFlowable == null) {
            collectionModelsFlowable = PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, CollectionModel>() {
                @NonNull
                @Override
                public Single<LoadResult<Integer, CollectionModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                    Integer nextPageNumber = loadParams.getKey();
                    if (nextPageNumber == null) {
                        nextPageNumber = 1;
                    }

                    return myLibraryRepository.getCollectionModels(nextPageNumber)
                            .map((Function<PagingModel<CollectionModel>, LoadResult<Integer, CollectionModel>>) pagingModel ->
                                    new LoadResult.Page<>(pagingModel.models,
                                            pagingModel.current > 1 ? pagingModel.current - 1 : null,
                                            pagingModel.current < pagingModel.pages ? pagingModel.current + 1 : null))
                            .onErrorReturn(throwable -> {
                                NetworkUtils.errorHandle(throwable);
                                return new LoadResult.Error<>(throwable);
                            });
                }

                @Nullable
                @Override
                public Integer getRefreshKey(@NonNull PagingState<Integer, CollectionModel> pagingState) {
                    return null;
                }
            });
        }

        return collectionModelsFlowable;
    }

    /**
     * 取消收藏图书
     *
     * @param collectionModel 需取消收藏的图书
     * @return 是否成功
     */
    public Observable<Boolean> cancelCollectBook(CollectionModel collectionModel) {
        return myLibraryRepository.cancelCollectBook(collectionModel)
                .observeOn(AndroidSchedulers.mainThread());
    }
}
