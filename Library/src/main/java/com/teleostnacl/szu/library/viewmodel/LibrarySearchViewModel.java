package com.teleostnacl.szu.library.viewmodel;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagingData;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.paging.PagingUtils;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.search.SearchModel;
import com.teleostnacl.szu.library.repository.LibrarySearchRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class LibrarySearchViewModel extends ViewModel {

    public final SearchModel searchModel = new SearchModel();

    private final LibrarySearchRepository librarySearchRepository = new LibrarySearchRepository();

    // 搜索结果的Flowable
    private Flowable<PagingData<BookModel>> searchResultFlowable;

    /**
     * @return 搜索建议
     */
    public Observable<List<String>> showSearchSuggestion(String keyword) {
        return librarySearchRepository.showSearchSuggestion(searchModel, keyword)
                .onErrorReturnItem(new ArrayList<>())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return 搜索结果的Flowable 实现分页
     */
    public Flowable<PagingData<BookModel>> getSearchResultFlowable() {
        if (searchResultFlowable == null) {
            searchResultFlowable = PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, BookModel>() {
                @NonNull
                @Override
                public Single<LoadResult<Integer, BookModel>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                    if (TextUtils.isEmpty(searchModel.keyword)) {
                        return Single.just(new LoadResult.Page<>(new ArrayList<>(), null, null));
                    }

                    Integer nextPageNumber = loadParams.getKey();
                    if (nextPageNumber == null) {
                        nextPageNumber = 1;
                    }

                    return librarySearchRepository.search(searchModel, nextPageNumber)
                            .map((Function<SearchResultModel, LoadResult<Integer, BookModel>>) searchResultModel ->
                                    new LoadResult.Page<>(searchResultModel.list,
                                            searchResultModel.current > 1 ? searchResultModel.current - 1 : null,
                                            searchResultModel.current < searchResultModel.sum ? searchResultModel.current + 1 : null))
                            .onErrorReturn(throwable -> {
                                NetworkUtils.errorHandle(throwable);
                                return new LoadResult.Error<>(throwable);
                            });
                }

                @Nullable
                @Override
                public Integer getRefreshKey(@NonNull PagingState<Integer, BookModel> pagingState) {
                    return null;
                }
            });
        }

        return searchResultFlowable;
    }

    public Observable<Boolean> getSearchFields() {
        searchModel.cfModels.clear();
        searchModel.documentTypeModels.clear();
        searchModel.deptModels.clear();
        searchModel.pyfModels.clear();
        searchModel.resultSum = 0;

        return librarySearchRepository.getSearchFields(searchModel, null)
                .onErrorReturnItem(false)
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 储存搜索结果的Model
     */
    public static class SearchResultModel {
        // 数据
        public List<BookModel> list;
        // 当前页
        public int current;
        // 总页数
        public int sum;

        public SearchResultModel(List<BookModel> list, int current, int sum) {
            this.list = list;
            this.current = current;
            this.sum = sum;
        }
    }
}
