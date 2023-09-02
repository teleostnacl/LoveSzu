package com.teleostnacl.szu.library.repository;

import com.teleostnacl.common.java.util.ExecutorServiceUtils;
import com.teleostnacl.szu.library.database.LibraryBookListDao;
import com.teleostnacl.szu.library.database.LibraryDatabase;
import com.teleostnacl.szu.library.model.list.LibraryBookListModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryListRepository {
    private final LibraryBookListDao libraryBookListDao = LibraryDatabase.getInstance().libraryBookListDao();

    // 单线程访问数据库
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * @return 从数据库中获取已加入图书清单的图书
     */
    public List<LibraryBookListModel> getLibraryBookListModels() {
        return ExecutorServiceUtils.submitByExecutor(executor, libraryBookListDao::getLibraryBookListModels, ArrayList::new);
    }

    /**
     * 增加进数据库中
     */
    public void addLibraryBookListModel(LibraryBookListModel libraryBookListModel) {
        executor.submit(() -> libraryBookListDao.insertOrUpdate(libraryBookListModel));
    }

    /**
     * 从数据库中移除
     */
    public void removeLibraryBookListModel(LibraryBookListModel libraryBookListModel) {
        executor.submit(() -> libraryBookListDao.delete(libraryBookListModel));
    }
}
