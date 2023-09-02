package com.teleostnacl.szu.electricity.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.teleostnacl.common.android.context.ContextUtils;

@Database(entities = {ElectricityDateEntry.class, ElectricityBuyingEntry.class},
        version = 1, exportSchema = false)
public abstract class ElectricityDatabase extends RoomDatabase {
    private static ElectricityDatabase database;

    private static final String DATABASE_NAME = "electricity.db";

    public static synchronized ElectricityDatabase getInstance() {
        if (database == null) {
            database = Room.databaseBuilder(ContextUtils.getContext(),
                            ElectricityDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return database;
    }

    public abstract ElectricityDateDao electricityDateDao();

    public abstract ElectricityBuyingDao electricityBuyingDao();
}
