package com.teleostnacl.szu.timetable.database;

import androidx.room.Dao;
import androidx.room.Query;

import com.teleostnacl.common.android.database.CRUDao;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.szu.timetable.model.Lesson;

import java.util.List;

@Dao
public interface LessonDao extends CRUDao<LessonEntry, Lesson> {

    //获取指定课程表的所有课程
    @Query("SELECT * FROM lesson WHERE timetable_id = :timetableID")
    List<LessonEntry> getLessonsByTimetableFromDatabase(String timetableID);

    default List<Lesson> getLessonsByTimetable(String timetableID) {
        return entriesToModels(getLessonsByTimetableFromDatabase(EncryptUtils.encrypt(timetableID)));
    }
}
