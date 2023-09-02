package com.teleostnacl.szu.timetable.viewmodel;

import com.teleostnacl.szu.timetable.model.Timetable;
import com.teleostnacl.szu.timetable.repository.TimetableRepository;

public class TimetableAppWidgetViewModel {
    private final TimetableRepository timetableRepository = new TimetableRepository();

    /**
     * 获取serial id 最大的课表 即默认显示的课表
     */
    public Timetable getSerialIdMaxTimetableFromTimetable() {
        return timetableRepository.getSerialIdMaxTimetableFromTimetable();
    }

}
