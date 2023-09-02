package com.teleostnacl.szu.timetable.database;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.teleostnacl.common.android.database.BaseEntry;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.timetable.model.Lesson;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程储存在数据库中的Entry
 */
@Entity(tableName = "lesson", foreignKeys = @ForeignKey(entity = TimetableEntry.class,
        parentColumns = "id", childColumns = "timetable_id", onDelete = CASCADE),
        indices = @Index("timetable_id"))
public class LessonEntry implements BaseEntry<Lesson> {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public final int id;
    //所属课程表的id
    @ColumnInfo(name = "timetable_id")
    public final String timetableID;
    //上课的周数
    @ColumnInfo(name = "weeks")
    public final String weeks;
    //上课时间(周几)
    @ColumnInfo(name = "week_day")
    public final String weekDay;
    //开始节数
    @ColumnInfo(name = "start_time")
    public final String startTime;
    //持续节数
    @ColumnInfo(name = "period_time")
    public final String periodTime;
    //课程名
    @ColumnInfo(name = "lesson_name")
    public final String lessonName;
    //课程简称
    @ColumnInfo(name = "lesson_name_abbr")
    public final String lessonNameAbbr;
    //课序号
    @ColumnInfo(name = "lesson_serial_number")
    public final String lessonSerialNumber;
    //课程编号
    @ColumnInfo(name = "lesson_number")
    public final String lessonNumber;
    //上课老师
    @ColumnInfo(name = "teacher")
    public final String teacher;
    //上课地点
    @ColumnInfo(name = "location")
    public final String location;
    //课程颜色索引值
    @ColumnInfo(name = "color")
    public final int color;
    //endregion


    public LessonEntry(int id, String timetableID, String weeks, String weekDay, String startTime,
                       String periodTime, String lessonName, String lessonNameAbbr, String lessonSerialNumber,
                       String lessonNumber, String teacher, String location, int color) {
        this.id = id;
        this.timetableID = timetableID;
        this.weeks = weeks;
        this.weekDay = weekDay;
        this.startTime = startTime;
        this.periodTime = periodTime;
        this.lessonName = lessonName;
        this.lessonNameAbbr = lessonNameAbbr;
        this.lessonSerialNumber = lessonSerialNumber;
        this.lessonNumber = lessonNumber;
        this.teacher = teacher;
        this.location = location;
        this.color = color;
    }

    public LessonEntry(@NonNull Lesson lesson) {
        this.id = lesson.id;
        this.timetableID = EncryptUtils.encrypt(lesson.timetableID);
        this.weeks = EncryptUtils.encrypt(intList2String(lesson.weeks));
        this.weekDay = EncryptUtils.encrypt(lesson.weekDay);
        this.startTime = EncryptUtils.encrypt(lesson.startTime);
        this.periodTime = EncryptUtils.encrypt(lesson.periodTime);
        this.lessonName = EncryptUtils.encrypt(lesson.lessonName);
        this.lessonNameAbbr = EncryptUtils.encrypt(lesson.lessonNameAbbr);
        this.lessonSerialNumber = EncryptUtils.encrypt(lesson.lessonSerialNumber);
        this.lessonNumber = EncryptUtils.encrypt(lesson.lessonNumber);
        this.teacher = EncryptUtils.encrypt(lesson.teacher);
        this.location = EncryptUtils.encrypt(lesson.location);
        this.color = lesson.color;
    }

    @Override
    public Lesson toModel() {
        Lesson lesson = new Lesson();
        lesson.id = id;
        lesson.timetableID = EncryptUtils.decrypt(timetableID);
        lesson.weeks.addAll(string2IntList(EncryptUtils.decrypt(weeks)));
        lesson.weekDay = NumberUtils.parseInt(EncryptUtils.decrypt(weekDay), 0);
        lesson.startTime = NumberUtils.parseInt(EncryptUtils.decrypt(startTime), 0);
        lesson.periodTime = NumberUtils.parseInt(EncryptUtils.decrypt(periodTime), 0);
        lesson.lessonName = EncryptUtils.decrypt(lessonName);
        lesson.lessonNameAbbr = EncryptUtils.decrypt(lessonNameAbbr);
        lesson.lessonSerialNumber = EncryptUtils.decrypt(lessonSerialNumber);
        lesson.lessonNumber = EncryptUtils.decrypt(lessonNumber);
        lesson.teacher = EncryptUtils.decrypt(teacher);
        lesson.location = EncryptUtils.decrypt(location);
        lesson.color = color;
        return lesson;
    }

    /**
     * integer集合转String, 使用", "进行分隔
     *
     * @param ints integer集合
     * @return 转成的String
     */
    @NonNull
    static String intList2String(@NonNull List<Integer> ints) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i : ints) {
            stringBuilder.append(i).append(", ");
        }
        String tmp = stringBuilder.toString();
        if (tmp.endsWith(", ")) {
            return tmp.substring(0, tmp.length() - 2);
        }
        return tmp;
    }

    /**
     * String转integer集合, 使用", "进行分隔
     *
     * @param s 待转换的String
     * @return integer集合
     */
    @NonNull
    static List<Integer> string2IntList(@NonNull String s) {
        List<Integer> list = new ArrayList<>();
        for (String tmp : s.split(", ")) {
            try {
                list.add(Integer.parseInt(tmp));
            } catch (Exception ignored) {
            }
        }
        return list;
    }
}
