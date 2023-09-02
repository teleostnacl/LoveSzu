package com.teleostnacl.szu.library.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagingData;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.teleostnacl.common.android.paging.PagingUtils;
import com.teleostnacl.szu.library.model.list.LibraryBookListCollectionSiteModel;
import com.teleostnacl.szu.library.model.list.LibraryBookListModel;
import com.teleostnacl.szu.library.repository.LibraryListRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class LibraryListViewModel extends ViewModel {
    private final LibraryListRepository libraryListRepository = new LibraryListRepository();

    private Flowable<PagingData<Object>> libraryBookListFlowable;

    public boolean libraryBookListFlowableNeedRefresh = false;

    public List<LibraryBookListModel> lastModels;

    public Flowable<PagingData<Object>> getLibraryBookListFlowable() {
        if (libraryBookListFlowable == null) {
            libraryBookListFlowable = PagingUtils.getFlowable(this, () -> new RxPagingSource<Integer, Object>() {

                @NonNull
                @Override
                public Single<LoadResult<Integer, Object>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
                    List<Object> list = new ArrayList<>();

                    Map<String, List<LibraryBookListModel>> map = new LinkedHashMap<>();

                    List<LibraryBookListModel> libraryBookListModels = libraryListRepository.getLibraryBookListModels();

                    int index;

                    // 遍历所有model
                    for (LibraryBookListModel model : libraryBookListModels) {
                        // 恢复被勾选状态
                        if (lastModels != null && (index = lastModels.indexOf(model)) != -1) {
                            model.check = lastModels.get(index).check;
                        }

                        // 根据model的馆藏点建立map 不存在则新建map
                        map.computeIfAbsent(model.collectionSite, s -> new ArrayList<>()).add(model);
                    }

                    // 遍历map, 将其增加进list中
                    for (Map.Entry<String, List<LibraryBookListModel>> entry : map.entrySet()) {
                        List<LibraryBookListModel> models = entry.getValue();
                        // 增加馆藏点的model
                        list.add(new LibraryBookListCollectionSiteModel(entry.getKey(), models));

                        // 添加List<LibraryBookListModel>
                        list.addAll(models);

                        // 设置最后一个的last值
                        models.get(models.size() - 1).last = true;
                    }

                    lastModels = libraryBookListModels;

                    return Single.just(new LoadResult.Page<>(list, null, null));
                }

                @Nullable
                @Override
                public Integer getRefreshKey(@NonNull PagingState<Integer, Object> pagingState) {
                    return null;
                }
            });
        }

        return libraryBookListFlowable;
    }

    /**
     * 增加进数据库中
     */
    public void addLibraryBookListModel(LibraryBookListModel libraryBookListModel) {
        libraryListRepository.addLibraryBookListModel(libraryBookListModel);
        libraryBookListFlowableNeedRefresh = true;
    }

    /**
     * 从数据库中移除
     */
    public void removeLibraryBookListModel(@NonNull List<LibraryBookListModel> libraryBookListModels) {
        for (LibraryBookListModel libraryBookListModel : libraryBookListModels) {
            removeLibraryBookListModel(libraryBookListModel);
        }
    }

    /**
     * 从数据库中移除
     */
    public void removeLibraryBookListModel(LibraryBookListModel libraryBookListModel) {
        libraryListRepository.removeLibraryBookListModel(libraryBookListModel);
    }
}
