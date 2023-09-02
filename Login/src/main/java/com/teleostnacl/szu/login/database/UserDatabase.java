package com.teleostnacl.szu.login.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.teleostnacl.common.android.context.ContextUtils;

@Database(entities = {UserEntry.class}, version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    private static UserDatabase database;

    private static final String DATABASE_NAME = "user.db";

    public static synchronized UserDatabase getInstance() {
        if (database == null) database = Room.databaseBuilder(ContextUtils.getContext(),
                        UserDatabase.class, DATABASE_NAME)
                .allowMainThreadQueries()
                .build();
        return database;
    }

    public abstract UserDao userDao();
}
