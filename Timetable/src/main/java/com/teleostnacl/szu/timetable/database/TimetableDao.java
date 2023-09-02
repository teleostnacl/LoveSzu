package com.teleostnacl.szu.timetable.database;

import androidx.room.Dao;
import androidx.room.Query;

import com.teleostnacl.common.android.database.CRUDao;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.szu.timetable.model.Timetable;

import java.util.List;

@Dao
public interface TimetableDao extends CRUDao<TimetableEntry, Timetable> {

    /**
     * @return 获取所有课表
     */
    @Query("SELECT * FROM timetable ORDER BY serial_id DESC")
    List<TimetableEntry> getAllTimetablesFromDatabase();

    default List<Timetable> getAllTimetables() {
        return entriesToModels(getAllTimetablesFromDatabase());
    }

    /**
     * @return 获取serial_id最大的课表(即默认显示的课表)
     */
    @Query("SELECT * FROM timetable WHERE serial_id = (SELECT MAX(serial_id) FROM timetable)")
    TimetableEntry getSerialIdMaxTimetableFromTimetable();

    default Timetable getSerialIdMaxTimetable() {
        TimetableEntry entry = getSerialIdMaxTimetableFromTimetable();
        if (entry == null) {
            return null;
        }
        return entry.toModel();
    }

    /**
     * @return 获取当前索引的最大值
     */
    @Query("SELECT MAX(serial_id) FROM timetable")
    int getSerialIdMax();

    /**
     * 通过课程表id查课表
     */
    @Query("SELECT * FROM timetable WHERE id = :id")
    TimetableEntry getByIDFromDatabase(String id);

    default Timetable getByID(String id) {
        TimetableEntry timetableEntry = getByIDFromDatabase(EncryptUtils.encrypt(id));
        return timetableEntry == null ? null : timetableEntry.toModel();
    }
}
