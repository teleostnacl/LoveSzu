package com.teleostnacl.szu.timetable.database;

import static com.teleostnacl.szu.timetable.database.LessonEntry.string2IntList;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.teleostnacl.common.android.database.BaseEntry;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.timetable.model.Timetable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(tableName = "timetable")
public class TimetableEntry implements BaseEntry<Timetable> {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public final String id;
    //记录课表顺序
    @ColumnInfo(name = "serial_id")
    public final int serialID;

    //课程表的标题
    @ColumnInfo(name = "name")
    public final String timetableName;
    //学期开学时间
    @ColumnInfo(name = "start_day")
    public final String startDay;
    //学期总周数
    @ColumnInfo(name = "weeks")
    public final String weeks;

    //每周的起始日是否为周日
    @ColumnInfo(name = "is_Sunday")
    public final boolean isSunday;
    //是否显示周末
    @ColumnInfo(name = "show_weekend")
    public final boolean showWeekend;

    //是否显示时间
    @ColumnInfo(name = "show_time")
    public final boolean showTime;
    //每节课开始时间
    @ColumnInfo(name = "start_time")
    public final String startTime;
    //每节课结束时间
    @ColumnInfo(name = "end_time")
    public final String endTime;

    //是否自动设置时间
    @ColumnInfo(name = "auto_set_time")
    public final boolean autoSetTime;
    //通常一节课的时长
    @ColumnInfo(name = "period")
    public final String period;
    //通常的课间休息时长
    @ColumnInfo(name = "rest_period")
    public final String restPeriod;

    //一天的三个时段的节数
    @NonNull
    @ColumnInfo(name = "section")
    public final String section;

    public TimetableEntry(@NonNull String id, int serialID, String timetableName, String startDay,
                          String weeks, boolean isSunday, boolean showWeekend, boolean showTime,
                          String startTime, String endTime, boolean autoSetTime, String period,
                          String restPeriod, @NonNull String section) {
        this.id = id;
        this.serialID = serialID;
        this.timetableName = timetableName;
        this.startDay = startDay;
        this.weeks = weeks;
        this.isSunday = isSunday;
        this.showWeekend = showWeekend;
        this.showTime = showTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.autoSetTime = autoSetTime;
        this.period = period;
        this.restPeriod = restPeriod;
        this.section = section;
    }

    public TimetableEntry(@NonNull Timetable timetable) {
        this.id = EncryptUtils.encrypt(timetable.id);
        this.serialID = timetable.serialID;
        this.timetableName = EncryptUtils.encrypt(timetable.timetableName);
        this.startDay = EncryptUtils.encrypt(timetable.startDay);
        this.weeks = EncryptUtils.encrypt(timetable.weeks);
        this.isSunday = timetable.isSunday;
        this.showWeekend = timetable.showWeekend;
        this.showTime = timetable.showTime;
        this.startTime = EncryptUtils.encrypt(stringList2String(timetable.startTime));
        this.endTime = EncryptUtils.encrypt(stringList2String(timetable.endTime));
        this.autoSetTime = timetable.autoSetTime;
        this.period = EncryptUtils.encrypt(timetable.period);
        this.restPeriod = EncryptUtils.encrypt(timetable.restPeriod);
        this.section = EncryptUtils.encrypt(ints2String(timetable.section));
    }

    @Override
    public Timetable toModel() {
        Timetable timetable = new Timetable();
        timetable.id = EncryptUtils.decrypt(id);
        timetable.serialID = serialID;
        timetable.timetableName = EncryptUtils.decrypt(timetableName);
        timetable.startDay = NumberUtils.parseLong(EncryptUtils.decrypt(startDay), new Date().getTime());
        timetable.weeks = NumberUtils.parseInt(EncryptUtils.decrypt(weeks), 18);
        timetable.isSunday = isSunday;
        timetable.showWeekend = showWeekend;
        timetable.showTime = showTime;
        timetable.startTime.addAll(string2stringList(EncryptUtils.decrypt(startTime)));
        timetable.endTime.addAll(string2stringList(EncryptUtils.decrypt(endTime)));
        timetable.autoSetTime = autoSetTime;
        timetable.period = NumberUtils.parseInt(EncryptUtils.decrypt(period), 40);
        timetable.restPeriod = NumberUtils.parseInt(EncryptUtils.decrypt(restPeriod), 40);

        timetable.setSection(string2ints(EncryptUtils.decrypt(section)));

        return timetable;
    }

    /**
     * String集合转String, 使用", "进行分隔
     *
     * @param strings String集合
     * @return 转成的String
     */
    @NonNull
    static String stringList2String(@NonNull List<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : strings) {
            stringBuilder.append(s).append(", ");
        }
        String tmp = stringBuilder.toString();
        if (tmp.endsWith(", ")) {
            return tmp.substring(0, tmp.length() - 2);
        }
        return tmp;
    }

    /**
     * String转String集合, 使用", "进行分隔
     *
     * @param s 待转换的String
     * @return String集合
     */
    @NonNull
    static List<String> string2stringList(@NonNull String s) {
        List<String> list = new ArrayList<>();
        for (String tmp : s.split(", ")) {
            try {
                list.add(tmp);
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    /**
     * int型数组转String, 使用", "进行分隔
     *
     * @param ints int型数组
     * @return 转成的String
     */
    @NonNull
    static String ints2String(@NonNull int[] ints) {
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
     * String转String集合, 使用", "进行分隔
     *
     * @param s 待转换的String
     * @return String集合
     */
    @NonNull
    static int[] string2ints(@NonNull String s) {
        List<Integer> tmp = string2IntList(s);
        int[] ints = new int[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            ints[i] = tmp.get(i);
        }
        return ints;
    }
}
