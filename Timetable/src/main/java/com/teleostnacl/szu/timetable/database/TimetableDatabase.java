package com.teleostnacl.szu.timetable.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.teleostnacl.common.android.context.ContextUtils;

@Database(entities = {TimetableEntry.class, LessonEntry.class}, exportSchema = false, version = 1)
public abstract class TimetableDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "timetable.db";

    //单例模式
    private static TimetableDatabase database;

    public static synchronized TimetableDatabase getInstance() {
        if (database == null)
            database = Room.databaseBuilder(ContextUtils.getContext(),
                            TimetableDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        return database;
    }

    public abstract TimetableDao timetableDao();

    public abstract LessonDao lessonDao();
}

