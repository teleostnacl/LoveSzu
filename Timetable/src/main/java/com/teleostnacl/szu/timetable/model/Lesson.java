package com.teleostnacl.szu.timetable.model;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import static com.teleostnacl.szu.timetable.model.Timetable.AFTERNOON;
import static com.teleostnacl.szu.timetable.model.Timetable.EVENING;
import static com.teleostnacl.szu.timetable.model.Timetable.MORNING;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.teleostnacl.common.android.database.BaseModel;
import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.utils.TimeUtils;
import com.teleostnacl.szu.timetable.BR;
import com.teleostnacl.szu.timetable.R;
import com.teleostnacl.szu.timetable.database.LessonEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 课程
 */
public class Lesson extends BaseObservable implements BaseModel<LessonEntry> {

    // 课程id
    public int id;
    //所属课程表的id
    public String timetableID;

    //上课周数
    public final List<Integer> weeks = new ArrayList<>();
    //上课时间(周几)
    public int weekDay;
    //开始节数
    public int startTime;
    //持续节数
    public int periodTime = 1;

    //课程名
    public String lessonName = "";
    //课程简称
    public String lessonNameAbbr = "";
    //课序号
    public String lessonSerialNumber = "";
    //课程编号
    public String lessonNumber = "";

    //上课老师
    public String teacher = "";
    //上课地点
    public String location = "";

    //课程颜色索引值
    public int color;
    //endregion

    public Lesson() {
    }

    public Lesson(@NonNull Lesson lesson) {
        this.id = lesson.id;
        this.timetableID = lesson.timetableID;
        this.weeks.addAll(lesson.weeks);
        this.weekDay = lesson.weekDay;
        this.startTime = lesson.startTime;
        this.periodTime = lesson.periodTime;
        this.lessonName = lesson.lessonName;
        this.lessonNameAbbr = lesson.lessonNameAbbr;
        this.lessonSerialNumber = lesson.lessonSerialNumber;
        this.lessonNumber = lesson.lessonNumber;
        this.teacher = lesson.teacher;
        this.location = lesson.location;
        this.color = lesson.color;
    }

    @Override
    public LessonEntry toEntry() {
        return new LessonEntry(this);
    }

    /**
     * 判断是否同属于同一门课程,
     * timetableID, lessonName, lessonNameAbbr, lessonSerialNumber, lessonNumber, teacher, location 相等
     */
    public boolean isSameLesson(@NonNull Lesson lesson) {
        return !(Objects.equals(id, lesson.id) && lesson.id != 0) &&
                Objects.equals(timetableID, lesson.timetableID) &&
                Objects.equals(lessonName, lesson.lessonName) &&
                Objects.equals(lessonSerialNumber, lesson.lessonSerialNumber) &&
                Objects.equals(lessonNumber, lesson.lessonNumber) &&
                Objects.equals(teacher, lesson.teacher) &&
                Objects.equals(location, lesson.location);
    }

    /**
     * @return 获取背景颜色
     */
    public int getBackgroundColor() {
        return ColorResourcesUtils.getLightColors()[color];
    }

    /**
     * @return 获取文字颜色
     */
    public int getTextColor() {
        return ColorResourcesUtils.getDeepColors()[color];
    }

    /**
     * 进行拆分跨时段的课程,如果需要的话
     *
     * @param section 表示所需判断是否拆分依据的节数
     * @return 返回一个二元数组集合, 数组的第一个元素表示该拆分后的课程的开始时间, 第二个元素表示持续时间
     */
    public List<int[]> spiltPeriod(int[] section) {
        //如果开始时间超过总节数,忽略该课程
        if (startTime >= Arrays.stream(section).sum()) return null;

        //记录该课程在所在列的时间段索引,默认为上午
        int timeIndex = 0;
        //晚上课程
        if (startTime >= section[MORNING] + section[AFTERNOON]) {
            timeIndex = 2;
        }
        //下午课程
        else if (startTime >= section[MORNING]) {
            timeIndex = 1;
        }

        //计算当天下一个时段的第一节课的索引
        int nextFirstIndex = section[MORNING];
        for (int i = timeIndex; i > 0; i--) nextFirstIndex += section[i];

        List<int[]> list = new ArrayList<>();

        //记录拆分之后的课程的上课和持续时间
        int startTimeInTimetable = startTime;
        int periodTimeInTimetable = periodTime;

        //如果该课程已经超出一个时段,则对课程进行拆分
        if (startTime + periodTime > nextFirstIndex) {

            //超出晚上的部分,舍去
            int lastIndex = section[MORNING] + section[AFTERNOON] + section[EVENING];
            if (startTimeInTimetable + periodTimeInTimetable > lastIndex)
                periodTimeInTimetable = lastIndex - startTimeInTimetable;

            //非晚上课程且超出下午部分的课程,拆分到晚上课程
            int secondIndex = section[MORNING] + section[AFTERNOON];
            if (startTimeInTimetable < secondIndex &&
                    startTimeInTimetable + periodTimeInTimetable > secondIndex) {
                int[] time = new int[2];
                //上课时间
                time[0] = secondIndex;
                //持续时间
                time[1] = startTimeInTimetable + periodTimeInTimetable - secondIndex;
                //添加进list中
                list.add(time);

                //将原课程的持续时间减去拆分长度
                periodTimeInTimetable -= time[1];
            }

            //非下午课程超出上午部分的课程,拆分到下午
            if (startTimeInTimetable < section[MORNING] &&
                    startTimeInTimetable + periodTimeInTimetable > section[MORNING]) {
                int[] time = new int[2];
                //上课时间
                time[0] = section[MORNING];
                //持续时间
                time[1] = startTimeInTimetable + periodTimeInTimetable - section[MORNING];
                //添加进list中
                list.add(time);

                //将原课程的持续时间减去拆分长度
                periodTimeInTimetable -= time[1];
            }
        }

        //将拆分之后剩余的部分放入List中
        list.add(new int[]{startTimeInTimetable, periodTimeInTimetable});

        return list;
    }

    // region Databinding
    @Bindable
    public String getLessonName() {
        return lessonName;
    }

    @Bindable
    public Spanned getLessonInformationName() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.layout_lesson_information_lesson_name) + lessonName, FROM_HTML_MODE_COMPACT);
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
        notifyPropertyChanged(BR.lessonName);
        notifyPropertyChanged(BR.lessonInformationName);
    }

    @Bindable
    public String getLessonNameAbbr() {
        return lessonNameAbbr;
    }

    @Bindable
    public Spanned getLessonInformationNameAbbr() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.layout_lesson_information_lesson_abbr) + lessonNameAbbr, FROM_HTML_MODE_COMPACT);
    }

    public void setLessonNameAbbr(String lessonNameAbbr) {
        this.lessonNameAbbr = lessonNameAbbr;
        notifyPropertyChanged(BR.lessonNameAbbr);
    }

    @Bindable
    public String getLessonSerialNumber() {
        return lessonSerialNumber;
    }

    public void setLessonSerialNumber(String lessonSerialNumber) {
        this.lessonSerialNumber = lessonSerialNumber;
        notifyPropertyChanged(BR.lessonSerialNumber);
        notifyPropertyChanged(BR.lessonInformationNumber);
    }

    @Bindable
    public String getLessonNumber() {
        return lessonNumber;
    }

    @Bindable
    public Spanned getLessonInformationNumber() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.layout_lesson_information_lesson_number) + lessonNumber +
                // 如果lessonSerialNumber不为空 则在后面进行添加展示
                (TextUtils.isEmpty(lessonSerialNumber) ? "" : "[" + lessonSerialNumber + "]"), FROM_HTML_MODE_COMPACT);
    }

    public void setLessonNumber(String lessonNumber) {
        this.lessonNumber = lessonNumber;
        notifyPropertyChanged(BR.lessonNumber);
        notifyPropertyChanged(BR.lessonInformationNumber);
    }

    @Bindable
    public String getTeacher() {
        return teacher;
    }

    @Bindable
    public Spanned getLessonInformationTeachers() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.layout_lesson_information_lesson_teachers) + teacher, FROM_HTML_MODE_COMPACT);
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
        notifyPropertyChanged(BR.teacher);
    }

    @Bindable
    public String getLocation() {
        return location;
    }

    @Bindable
    public Spanned getLessonInformationLocation() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.layout_lesson_information_lesson_location) + location, FROM_HTML_MODE_COMPACT);
    }

    public void setLocation(String location) {
        this.location = location;
        notifyPropertyChanged(BR.location);
        notifyPropertyChanged(BR.lessonInformationLocation);
    }

    public Spanned getLessonInformationTime() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.layout_lesson_information_lesson_time) + TimeUtils.getDaySum()[weekDay] + " " + (startTime + 1) + "-" + (periodTime + startTime), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLessonInformationWeeks() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int week : weeks) {
            stringBuilder.append(week + 1).append(", ");
        }

        return Html.fromHtml(ResourcesUtils.getString(R.string.layout_lesson_information_lesson_weeks) + stringBuilder.substring(0, stringBuilder.length() - 2), FROM_HTML_MODE_COMPACT);
    }
    // endregion
}
