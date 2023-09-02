package com.teleostnacl.szu.login.database;

import androidx.room.Dao;
import androidx.room.Query;

import com.teleostnacl.common.android.database.CRUDao;
import com.teleostnacl.szu.login.model.LoginModel;

import java.util.List;

@Dao
public interface UserDao extends CRUDao<UserEntry, LoginModel> {
    @Query("SELECT * FROM user ORDER BY last_login DESC")
    List<UserEntry> getAllFromDataBase();

    default List<LoginModel> getAll() {
        return entriesToModels(getAllFromDataBase());
    }
}
