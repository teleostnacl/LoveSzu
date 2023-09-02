package com.teleostnacl.szu.library.database;

import androidx.room.Dao;
import androidx.room.Query;

import com.teleostnacl.common.android.database.CRUDao;
import com.teleostnacl.szu.library.model.list.LibraryBookListModel;

import java.util.List;

@Dao
public interface LibraryBookListDao extends CRUDao<LibraryBookListEntry, LibraryBookListModel> {
    @Query("SELECT * From book_list ORDER BY date")
    List<LibraryBookListEntry> getLibraryBookListEntries();

    default List<LibraryBookListModel> getLibraryBookListModels() {
        return entriesToModels(getLibraryBookListEntries());
    }
}
