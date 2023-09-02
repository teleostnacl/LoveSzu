package com.teleostnacl.szu.library.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.teleostnacl.common.android.context.ContextUtils;

@Database(entities = {LibraryBookListEntry.class}, version = 1, exportSchema = false)
public abstract class LibraryDatabase extends RoomDatabase {
    private static LibraryDatabase libraryDatabase;

    private static final String DATABASE_NAME = "library.db";

    public static synchronized LibraryDatabase getInstance() {
        if (libraryDatabase == null) {
            libraryDatabase = Room.databaseBuilder(ContextUtils.getContext(),
                            LibraryDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return libraryDatabase;
    }

    public abstract LibraryBookListDao libraryBookListDao();
}
