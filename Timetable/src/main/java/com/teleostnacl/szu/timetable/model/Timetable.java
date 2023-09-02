package com.teleostnacl.szu.timetable.model;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.recyclerview.widget.ListAdapter;

import com.teleostnacl.common.android.database.BaseModel;
import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.timetable.BR;
import com.teleostnacl.szu.timetable.R;
import com.teleostnacl.szu.timetable.database.TimetableEntry;
import com.teleostnacl.szu.timetable.databinding.ItemEditTimetableLessonTimeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Timetable extends BaseObservable implements BaseModel<TimetableEntry> {

    //region sections中早中晚的索引值
    public final static int MORNING = 0;
    public final static int AFTERNOON = 1;
    public final static int EVENING = 2;
    // endregion

    // 课程表id
    public String id;
    //记录课表顺序
    public int serialID;
    //课程表的标题
    public String timetableName;
    //学期开学时间
    public long startDay = new Date().getTime();
    //学期总周数
    public int weeks = 18;

    //每周的起始日是否为周日
    public boolean isSunday = true;
    //是否显示周末
    public boolean showWeekend = true;

    //是否显示时间
    public boolean showTime = true;
    //每节课开始时间
    public final List<String> startTime = new ArrayList<>();
    //每节课结束时间
    public final List<String> endTime = new ArrayList<>();
    //是否自动设置时间
    public boolean autoSetTime = true;
    //通常一节课的时长
    public int period = 40;
    //通常的课间休息时长
    public int restPeriod = 10;
    //一天的三个时段的节数
    public final int[] section = new int[]{4, 4, 0};

    //存放当前显示的课程表按照周次顺序排列的课程
    public Map<Integer, List<Lesson>> weekMapLesson;

    //显示三个时间段的具体节的Recycler.Adapter
    public final ListAdapter<LessonTimeModel, DataBindingVH<ItemEditTimetableLessonTimeBinding>>
            morningAdapter = new ListAdapter<>(new DefaultItemCallback<>()) {
        @NonNull
        @Override
        public DataBindingVH<ItemEditTimetableLessonTimeBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DataBindingVH<>(parent.getContext(),
                    R.layout.item_edit_timetable_lesson_time, parent, false);
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ItemEditTimetableLessonTimeBinding> holder, int position) {
            holder.binding.setTimeModel(getCurrentList().get(position));
            holder.binding.getRoot().setOnClickListener(v ->
                    setSectionTime(v.getContext(), 0, position));
        }
    };
    public final ListAdapter<LessonTimeModel, DataBindingVH<ItemEditTimetableLessonTimeBinding>>
            afternoonAdapter = new ListAdapter<>(new DefaultItemCallback<>()) {
        @NonNull
        @Override
        public DataBindingVH<ItemEditTimetableLessonTimeBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DataBindingVH<>(parent.getContext(),
                    R.layout.item_edit_timetable_lesson_time, parent, false);
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ItemEditTimetableLessonTimeBinding> holder, int position) {
            holder.binding.setTimeModel(getCurrentList().get(position));
            holder.binding.getRoot().setOnClickListener(v ->
                    setSectionTime(v.getContext(), section[MORNING], position));
        }
    };
    public final ListAdapter<LessonTimeModel, DataBindingVH<ItemEditTimetableLessonTimeBinding>>
            eveningAdapter = new ListAdapter<>(new DefaultItemCallback<>()) {
        @NonNull
        @Override
        public DataBindingVH<ItemEditTimetableLessonTimeBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DataBindingVH<>(parent.getContext(),
                    R.layout.item_edit_timetable_lesson_time, parent, false);
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ItemEditTimetableLessonTimeBinding> holder, int position) {
            holder.binding.setTimeModel(getCurrentList().get(position));
            holder.binding.getRoot().setOnClickListener(v ->
                    setSectionTime(v.getContext(), section[MORNING] + section[AFTERNOON], position));
        }
    };

    // 导入课程表时记录当前的课程表所获取到的课程
    public List<Lesson> lessonsFromImport;


    public Timetable() {
    }

    public Timetable(String id) {
        this.id = id;
    }

    public Timetable(@NonNull Timetable timetable) {
        this.id = timetable.id;
        this.serialID = timetable.serialID;
        this.timetableName = timetable.timetableName;
        this.startDay = timetable.startDay;
        this.weeks = timetable.weeks;
        this.isSunday = timetable.isSunday;
        this.showWeekend = timetable.showWeekend;
        this.showTime = timetable.showTime;
        this.startTime.addAll(timetable.startTime);
        this.endTime.addAll(timetable.endTime);
        this.autoSetTime = timetable.autoSetTime;
        this.period = timetable.period;
        this.restPeriod = timetable.restPeriod;
        setSection(timetable.section);
        this.weekMapLesson = timetable.weekMapLesson;
        this.lessonsFromImport = timetable.lessonsFromImport;
    }

    @Override

    public TimetableEntry toEntry() {
        return new TimetableEntry(this);
    }

    /**
     * 设置一天三段的上课节数
     * 若给定的参数sections不足三个 则为设置的置0
     * 若超过了三个 则只取前三个
     *
     * @param sections 一天三段的上课节数
     */
    public void setSection(@NonNull int[] sections) {
        // 全部先置0
        for (int i = 0; i < 3; i++) {
            section[i] = 0;
        }

        // 只赋值前三项 不足三项则不理会
        for (int i = 0; i < sections.length; i++) {
            if (i < 3) {
                section[i] = sections[i];
            }
        }
    }

    /**
     * 设置一天三段的上课节数
     *
     * @param morning   上午
     * @param afternoon 下午
     * @param evening   晚上
     */
    public void setSection(int morning, int afternoon, int evening) {
        section[MORNING] = morning;
        section[AFTERNOON] = afternoon;
        section[EVENING] = evening;
    }

    /**
     * 设置指定时段的上课节数
     *
     * @param time    指定时段
     * @param section 上课节数
     */
    public void setSection(int time, int section) {
        int[] newSection = Arrays.copyOf(this.section, 3);
        newSection[time] = section;
        // 更新指定时段的上下课时间
        switch (time) {
            case MORNING:
                notifyPropertyChanged(BR.morningSection);
                break;
            case AFTERNOON:
                notifyPropertyChanged(BR.afternoonSection);
                break;
            case EVENING:
                notifyPropertyChanged(BR.eveningSection);
                break;
        }
        setStartAndEndTime(newSection, time);
        this.section[time] = section;
        submitLessonModel();
    }

    /**
     * @return 一天的总节数
     */
    public int getSections() {
        return Arrays.stream(section).sum();
    }

    /**
     * @return 返回一天三段中最多的节数
     */
    public int getMaxSection() {
        return Math.max(Math.max(section[MORNING], section[AFTERNOON]), section[EVENING]);
    }

    /**
     * @return 在WeekTimetable的RecyclerView中一行有多少个spanSize
     */
    public int getSpanCount() {
        return 2 * getColumnSize() - 1;
    }

    /**
     * 一行有几项,即一周显示的总天数加上时间轴
     */
    public int getColumnSize() {
        //显示周末课程,则总共有8列
        if (showWeekend) {
            return 8;
        }
        //不显示则只有6列
        return 6;
    }

    /**
     * @return 总共的项数
     */
    public int getItemCount() {
        return getColumnSize() * (getSections() + 1) + //一行日期行加上每节课的行
                (section[EVENING] == 0 ? 1 : 2); //是否有晚上课程,没有的话则不显示晚休
    }

    /**
     * 获取午休在recyclerView中的索引,包括了日期列
     */
    public int getNoonIndex() {
        return (section[MORNING] + 1) * getColumnSize();
    }

    /**
     * 获取晚休在recyclerView中的索引,包括了日期列
     */
    public int getDuskIndex() {
        return (section[MORNING] + section[AFTERNOON] + 1) * getColumnSize() + 1;
    }

    /**
     * 只有当showTime为true 同时上下课时间与节数一致, 才显示时间
     *
     * @return 是否显示时间
     */
    @Bindable
    public boolean isShowTime() {
        return showTime;
    }

    public boolean checkShowTime() {
        return isShowTime() && checkTime();
    }

    /**
     * @return 检查时间是否与节数一致
     */
    public boolean checkTime() {
        //记录上下课时间的数组长度与总节数不相等,则返回false
        if (startTime.size() != getSections() || endTime.size() != getSections()) {
            return false;
        } else {
            //遍历每一个数组值 是否符合要求
            for (String start : startTime) {
                if (!start.matches("\\d{2}:\\d{2}")) {
                    return false;
                }
            }

            for (String start : endTime) {
                if (!start.matches("\\d{2}:\\d{2}")) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 根据在List中index值,获取在recyclerView的位置
     *
     * @param index index
     * @return index在recyclerView的位置
     */
    public int getPositionInRecyclerView(int index) {
        //一周的天数
        int week = getColumnSize() - 1;
        //下午课程在表中开始的位置
        int afternoon = section[MORNING] * week;
        //晚上课程在表中开始的位置
        int evening = (section[MORNING] + section[AFTERNOON]) * week;
        //上午课程
        if (index < afternoon) {
            return index % week * section[MORNING]
                    + index / week
                    + getColumnSize() + section[MORNING];
        }
        //下午课程
        else if (index < evening) {
            return (index - afternoon) % week * section[AFTERNOON]
                    + (index - afternoon) / week
                    + getNoonIndex() + section[AFTERNOON] + 1;
        }
        //晚上课程
        else return (index - evening) % week * section[EVENING]
                    + (index - evening) / week
                    + getDuskIndex() + section[EVENING] + 1;
    }

    /**
     * 根据给出的周几和上课开始时间,获取在recyclerView的位置
     *
     * @param weekDay   周几
     * @param startTime 上课开始时间
     * @return 在recyclerView的位置
     */
    public int getPositionInRecyclerView(int weekDay, int startTime) {
        return getPositionInRecyclerView(getPositionInList(weekDay, startTime));
    }

    /**
     * 根据给出的周几和上课开始时间,返回在list中的位置
     *
     * @param weekDay   周几
     * @param startTime 上课开始时间
     * @return 在list中的位置
     */
    public int getPositionInList(int weekDay, int startTime) {
        //按课表的顺序,从左到右进行排列,获取相应位置在排列中的索引
        int index;
        //不显示周末,则显示的一周的起始为周一
        if (!showWeekend) {
            index = (weekDay - 1) + startTime * (getColumnSize() - 1);
        }
        //显示周末,但一周开始不是在周日,周末显示在一周末尾,则将Sunday往后移
        else if (!isSunday) {
            if (weekDay == 0) {
                weekDay = 7;
            }
            index = (weekDay - 1) + startTime * (getColumnSize() - 1);
        }
        //一周起始为周日,且显示周末
        else {
            index = weekDay + startTime * (getColumnSize() - 1);
        }

        return index;
    }

    /**
     * 生成显示上下课时间的model
     */
    public void submitLessonModel() {
        if (checkShowTime()) {
            List<LessonTimeModel> list = new ArrayList<>();
            for (int i = 0; i < Arrays.stream(section).sum(); i++) {
                list.add(new LessonTimeModel(i));
                //上午课程添加完成
                if (i == section[MORNING] - 1) {
                    morningAdapter.submitList(list);
                    list = new ArrayList<>();
                }
                //下午课程完成
                else if (i == section[MORNING] + section[AFTERNOON] - 1) {
                    afternoonAdapter.submitList(list);
                    list = new ArrayList<>();

                    // 不存在晚上课程时
                    if (section[EVENING] == 0) {
                        eveningAdapter.submitList(null);
                    }
                }
                //晚上课程完成
                else if (i == Arrays.stream(section).sum() - 1) {
                    eveningAdapter.submitList(list);
                }

            }
        } else {
            morningAdapter.submitList(null);
            afternoonAdapter.submitList(null);
            eveningAdapter.submitList(null);
        }
    }

    /**
     * 设置指定节数的时间
     *
     * @param context    context
     * @param startIndex 开始节数
     * @param position   所在RecyclerView的位置
     */
    private void setSectionTime(Context context, int startIndex, int position) {

        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_timetable_edit_timetable_lesson_time_settings, null);

        //获取时间选择器
        View[] views = new View[4];
        LinearLayout layout = view.findViewById(R.id.edit_timetable_lesson_time_number_picker);
        for (int i = 0, j = 0; i < 5; i++, j++) {
            //跳过间隔符
            if (i == 2) i++;
            views[j] = layout.getChildAt(i);
        }
        NumberPicker[] numberPickers = new NumberPicker[4];
        //设置时间选择器的单位并获取数值选择器
        for (int i = 0; i < 4; i++) {
            views[i].findViewById(R.id.number_picker_with_unit_text).setVisibility(View.VISIBLE);
            ((TextView) views[i].findViewById(R.id.number_picker_with_unit_text))
                    .setText(i % 2 == 0 ? R.string.timetable_edit_timetable_hour : R.string.timetable_edit_timetable_minute);
            numberPickers[i] = views[i].findViewById(R.id.number_picker_with_unit_view);
            numberPickers[i].setFormatter(i1 -> String.format(Locale.getDefault(), "%02d", i1));
        }

        //构建Dialog
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, (dialog, which) -> {
                    startTime.set(startIndex + position,
                            String.format(Locale.PRC, "%02d", numberPickers[0].getValue())
                                    + ":" + String.format(Locale.PRC, "%02d", numberPickers[1].getValue()));
                    endTime.set(startIndex + position,
                            String.format(Locale.PRC, "%02d", numberPickers[2].getValue())
                                    + ":" + String.format(Locale.PRC, "%02d", numberPickers[3].getValue()));

                    setStartAndEndTime(startIndex + position);

                    submitLessonModel();
                })
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                .create();

        //region初始化各时间选择器的最大值,最小值和初值
        //开始时间 的 时
        if (startIndex + position != 0) {
            //如果所调整的时间选择器不是第一节课,则该节课的上课时间不能比上一节下课时间早
            numberPickers[0].setMinValue(TimeUtils.spiltHour(endTime.get(startIndex + position - 1)));
        }
        //第一节课可以任意设置
        else numberPickers[0].setMinValue(0);
        //可以取到一天的最大值,即最晚23h
        numberPickers[0].setMaxValue(23);
        //设置初值
        numberPickers[0].setValue(TimeUtils.spiltHour(startTime.get(startIndex + position)));

        //开始时间 的 分
        if (startIndex + position != 0
                && TimeUtils.spiltHour(endTime.get(startIndex + position - 1)) ==
                TimeUtils.spiltHour(startTime.get(startIndex + position))) {
            //如果所调整的时间选择器不是第一节课,且上一节课的下课时间与该节课的时相同,则该节课的上课时间的分不能比上一节下课时间早
            numberPickers[1].setMinValue(TimeUtils.spiltMinute(endTime.get(startIndex + position - 1)));
        }
        //第一节课可以任意设置
        else numberPickers[1].setMinValue(0);
        //可以取到一小时的最大值,即最晚59min
        numberPickers[1].setMaxValue(59);
        //设置初值
        numberPickers[1].setValue(TimeUtils.spiltMinute(startTime.get(startIndex + position)));

        //结束时间 的 时
        //该节下课时间的时应该比上课时间晚
        numberPickers[2].setMinValue(TimeUtils.spiltHour(startTime.get(startIndex + position)));
        //可以取到一天的最大值,即最晚23h
        numberPickers[2].setMaxValue(23);
        //设置初值
        numberPickers[2].setValue(TimeUtils.spiltHour(endTime.get(startIndex + position)));

        //下课时间 的 分
        if (TimeUtils.spiltHour(endTime.get(startIndex + position)) ==
                TimeUtils.spiltHour(startTime.get(startIndex + position))) {
            //如果上课时间与下课的时相同,则该节课的下课时间的分不能比上课时间早
            numberPickers[3].setMinValue(TimeUtils.spiltMinute(startTime.get(startIndex + position)));
        }
        //不在同一小时则可以任意设置
        else numberPickers[3].setMinValue(0);
        //可以取到一小时的最大值,即最晚59min
        numberPickers[3].setMaxValue(59);
        //设置初值
        numberPickers[3].setValue(TimeUtils.spiltMinute(endTime.get(startIndex + position)));

        //禁止循环滚动
        for (NumberPicker numberPicker : numberPickers)
            numberPicker.setWrapSelectorWheel(false);
        //endregion

        //region设置各数值选择器的监听器
        AtomicInteger interval = new AtomicInteger(period);

        //上课时间 的 时
        numberPickers[0].setOnValueChangedListener((picker, oldVal, newVal) -> {
            //不是第一节课,则需调整上课时间的分
            if (startIndex + position != 0) {
                //如果新值与上一节课的下课时间相同,则调整分钟的最小值为上一节下课时间的分
                if (newVal == TimeUtils.spiltHour(endTime.get(startIndex + position - 1))) {
                    numberPickers[1].setMinValue(TimeUtils.spiltMinute(endTime.get(startIndex + position - 1)));
                }
                //否则可以取全部值
                else {
                    numberPickers[1].setMinValue(0);
                }
            }
            //设置下课时间的最小值
            numberPickers[2].setMinValue(newVal);
            numberPickers[3].setMinValue(0);
            //计算下课时间的字符串
            String tmp = TimeUtils.calTime(numberPickers[0].getValue() + ":" + numberPickers[1].getValue(), interval.get());
            //设置上下课时间
            numberPickers[2].setValue(TimeUtils.spiltHour(tmp));
            numberPickers[3].setValue(TimeUtils.spiltMinute(tmp));
            //下课时间与上课时间的时相同,则需调整分钟
            if (numberPickers[2].getValue() == newVal)
                numberPickers[3].setMinValue(numberPickers[1].getValue());
            //修改标题
            alertDialog.setTitle(ResourcesUtils.getString(R.string.timetable_edit_timetable_each_time_title_with_time, (startIndex + position + 1), interval.get()));
        });

        //上课时间 的 分
        numberPickers[1].setOnValueChangedListener((picker, oldVal, newVal) -> {
            //计算下课时间的字符串
            String tmp = TimeUtils.calTime(numberPickers[0].getValue() + ":" + numberPickers[1].getValue(), interval.get());
            //设置上下课时间
            numberPickers[2].setValue(TimeUtils.spiltHour(tmp));
            //更改下课时间分的最小值
            numberPickers[3].setMinValue(0);
            numberPickers[3].setValue(TimeUtils.spiltMinute(tmp));
            //上课时间与下课时间的时相同,则设置分的最小值为newVal
            if (numberPickers[2].getValue() == numberPickers[0].getValue())
                numberPickers[3].setMinValue(newVal);
            //修改标题
            alertDialog.setTitle(ResourcesUtils.getString(R.string.timetable_edit_timetable_each_time_title_with_time, (startIndex + position + 1), interval.get()));
        });
        //下课时间的时
        numberPickers[2].setOnValueChangedListener((picker, oldVal, newVal) -> {
            //如果下课时间的时与上课时间相同,则设置下课时间的分的最小值为上课时间的分
            if (newVal == numberPickers[0].getValue())
                numberPickers[3].setMinValue(numberPickers[1].getValue());
                //否则可以任意设置
            else numberPickers[3].setMinValue(0);
            //修改间隔
            interval.set(TimeUtils.calLateTime(numberPickers[0].getValue() + ":" + numberPickers[1].getValue(),
                    numberPickers[2].getValue() + ":" + numberPickers[3].getValue()));
            //修改标题
            alertDialog.setTitle(ResourcesUtils.getString(R.string.timetable_edit_timetable_each_time_title_with_time, (startIndex + position + 1), interval.get()));
        });
        //下课时间的分
        numberPickers[3].setOnValueChangedListener((picker, oldVal, newVal) -> {
            //修改间隔
            interval.set(TimeUtils.calLateTime(numberPickers[0].getValue() + ":" + numberPickers[1].getValue(),
                    numberPickers[2].getValue() + ":" + numberPickers[3].getValue()));
            //修改标题
            alertDialog.setTitle(ResourcesUtils.getString(R.string.timetable_edit_timetable_each_time_title_with_time, (startIndex + position + 1), interval.get()));
        });
        //endregion

        if (isShowTime()) {
            alertDialog.setTitle(ResourcesUtils.getString(R.string.timetable_edit_timetable_each_time_title_with_time, (startIndex + position + 1),
                    TimeUtils.calLateTime(startTime.get(startIndex + position), endTime.get(startIndex + position))));
        } else {
            alertDialog.setTitle(ResourcesUtils.getString(R.string.timetable_edit_timetable_each_time_title, (startIndex + position + 1)));
        }
        alertDialog.setView(view);
        alertDialog.show();
    }

    /**
     * 从指定位置更新上下课时间,不跨时段
     */
    public void setStartAndEndTime(int startSection) {
        //设置了自动设置上下课则设置时间
        if (autoSetTime) {

            //设置结束节数
            int last = section[MORNING];
            //晚上课程
            if (startSection >= section[MORNING] + section[AFTERNOON]) {
                last = section[MORNING] + section[AFTERNOON] + section[EVENING];
            }
            //下午课程
            else if (startSection >= section[MORNING]) {
                last = section[MORNING] + section[AFTERNOON];
            }

            //自动调整剩上下课时间
            for (int i = startSection + 1; i < last; i++) {
                startTime.set(i, TimeUtils.calTime(endTime.get(i - 1), restPeriod));
                endTime.set(i, TimeUtils.calTime(startTime.get(i), period));
            }
        }
    }

    /**
     * 更新指定时段的上下课时间
     *
     * @param newSection 新的课程数
     * @param period     指定时段
     */
    private void setStartAndEndTime(int[] newSection, int period) {
        //设置了自动设置上下课则设置时间
        if (autoSetTime) {

            // 获取默认的时间
            List<String> originStartTime = new ArrayList<>(startTime);
            List<String> originEndTime = new ArrayList<>(endTime);

            String[] time = new String[3];
            if (startTime.size() == getSections()) {
                //获取原始的早中晚开始的节数
                time[0] = startTime.get(0);
                time[1] = startTime.get(section[MORNING]);
                //有晚上课程,则获取晚上上课时间
                if (section[EVENING] != 0)
                    time[2] = startTime.get(section[MORNING] + section[AFTERNOON]);
            }

            //使用默认值
            if (time[0] == null) time[0] = "08:30";
            if (time[1] == null) time[1] = "14:00";
            if (time[2] == null) time[2] = "19:00";

            //计算时间
            startTime.clear();
            endTime.clear();
            // 暂时使用null进行填充
            for (int i = 0; i < Arrays.stream(newSection).sum(); i++) {
                startTime.add(null);
                endTime.add(null);
            }

            for (int i = 0; i < 3; i++) {
                //获取该段时间上课节数的初值
                int startIndex = 0;
                int tmp = i;
                while (tmp > 0) {
                    startIndex += newSection[--tmp];
                }

                // 非指定的节数, 则复制之前的内容
                if (period != i && originStartTime.size() == getSections() && originEndTime.size() == getSections()) {
                    // 获取该时段原始上课节数的初值
                    int oldStartIndex = 0;
                    tmp = i;
                    while (tmp > 0) {
                        oldStartIndex += section[--tmp];
                    }

                    for (int j = 0; j < section[i]; j++) {
                        startTime.set(startIndex + j, originStartTime.get(oldStartIndex + j));
                        endTime.set(startIndex + j, originEndTime.get(oldStartIndex + j));
                    }
                } else {
                    for (int j = 0; j < newSection[i]; j++) {
                        //设置上下课时间
                        startTime.set(startIndex + j, TimeUtils.calTime(time[i], (this.period + this.restPeriod) * j));
                        endTime.set(startIndex + j, TimeUtils.calTime(TimeUtils.calTime(time[i], this.period), (this.period + this.restPeriod) * j));
                    }
                }
            }
        }
    }

    /**
     * 重新设置上下课时间
     */
    public void resetStartAndEndTime() {
        startTime.clear();
        endTime.clear();
        setStartAndEndTime(section, 3);
        submitLessonModel();
    }

    //region DataBinding

    @Bindable
    public String getTimetableName() {
        return timetableName;
    }

    @Bindable
    public String getStartDay() {
        return new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA).format(startDay);
    }

    public void setStartDay(long startDay) {
        this.startDay = startDay;
        notifyPropertyChanged(BR.startDay);
        notifyPropertyChanged(BR.currentWeek);
    }

    @Bindable
    public String getCurrentWeek() {
        int week = (int) ((new Date().getTime() - TimeUtils.calDateWeekStartTime(startDay, isSunday)) / TimeUtils.WEEK);

        //学期已结束
        if (week >= weeks) {
            return ResourcesUtils.getString(R.string.timetable_edit_timetable_has_end);
        }
        //学期尚未开始
        else if (week < 0) {
            return ResourcesUtils.getString(R.string.timetable_edit_timetable_not_start);
        }

        return ResourcesUtils.getString(R.string.timetable_edit_timetable_current_week, week + 1);
    }

    @Bindable
    public String getWeeks() {
        return String.valueOf(weeks);
    }

    @Bindable
    public boolean isIsSunday() {
        return isSunday;
    }

    public void setIsSunday(boolean sunday) {
        isSunday = sunday;
        notifyPropertyChanged(BR.isSunday);
    }

    @Bindable
    public boolean isShowWeekend() {
        return showWeekend;
    }

    public void setShowWeekend(boolean showWeekend) {
        this.showWeekend = showWeekend;
        notifyPropertyChanged(BR.showWeekend);
    }


    public void setShowTime(boolean showTime) {
        if (this.showTime == showTime) {
            return;
        }
        this.showTime = showTime;
        //设置了显示上下课时间,但上下课时间为空,则初始化上下课时间
        if (checkShowTime()) {
            //使用默认的三时段开始值
            String[] time = new String[]{"08:00", "14:00", "19:00"};

            //计算时间
            startTime.clear();
            endTime.clear();

            // 暂时使用null进行填充
            for (int i = 0; i < getSections(); i++) {
                startTime.add(null);
                endTime.add(null);
            }

            for (int i = 0; i < 3; i++) {
                //获取该段时间上课节数的初值
                int startIndex = 0;
                int tmp = i;
                while (tmp > 0) {
                    startIndex += section[--tmp];
                }

                for (int j = 0; j < section[i]; j++) {
                    //设置上下课时间
                    startTime.set(startIndex + j, TimeUtils.calTime(time[i], (period + restPeriod) * j));
                    endTime.set(startIndex + j, TimeUtils.calTime(TimeUtils.calTime(time[i], period), (period + restPeriod) * j));
                }
            }
        }
        submitLessonModel();
        notifyPropertyChanged(BR.showTime);
    }

    @Bindable
    public boolean isAutoSetTime() {
        return autoSetTime;
    }

    public void setAutoSetTime(boolean autoSetTime) {
        this.autoSetTime = autoSetTime;
        notifyPropertyChanged(BR.autoSetTime);
    }

    @Bindable
    public String getPeriod() {
        return period + ResourcesUtils.getString(R.string.timetable_edit_timetable_minute);
    }

    @Bindable
    public String getRestPeriod() {
        return restPeriod + ResourcesUtils.getString(R.string.timetable_edit_timetable_minute);
    }

    @Bindable
    public String getMorningSection() {
        return String.valueOf(section[MORNING]);
    }

    @Bindable
    public String getAfternoonSection() {
        return String.valueOf(section[AFTERNOON]);
    }

    @Bindable
    public String getEveningSection() {
        return String.valueOf(section[EVENING]);
    }

    //region 各设置项的单击响应事件

    /**
     * 课表名称
     */
    public void setTimetableName(@NonNull View v) {
        int margin = ResourcesUtils.dp_int_16;

        EditText editText = new EditText(v.getContext());
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin, margin, margin, margin);
        editText.setLayoutParams(layoutParams);
        editText.setBackgroundResource(R.drawable.background_timetable_edit_text);
        editText.setGravity(Gravity.END | Gravity.CENTER_HORIZONTAL);
        editText.setHint(R.string.hint_timetable_edit_text_must);
        editText.setPadding(margin, margin, margin, margin);
        editText.setHintTextColor(ColorResourcesUtils.getColor(R.color.color_timetable_edit_text_hint));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, v.getContext().getResources().getDimension(R.dimen.text_size_timetable_edit_text));
        editText.setEllipsize(TextUtils.TruncateAt.END);
        editText.setSingleLine();
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        //设置初值
        editText.setText(timetableName);

        FrameLayout frameLayout = new FrameLayout(v.getContext());
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        frameLayout.addView(editText);

        new AlertDialog.Builder(v.getContext())
                .setTitle(v.getContext().getString(R.string.timetable_edit_timetable_title_name))
                .setView(frameLayout)
                //保存修改
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, (dialog, which) -> {
                    this.timetableName = editText.getText().toString();
                    notifyPropertyChanged(BR.timetableName);
                })
                //取消保存
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                .show();
    }

    /**
     * 开学日期
     */
    public void setStartDay(@NonNull View v) {
        ToastUtils.makeToast(R.string.timetable_edit_timetable_school_days_choose_tip);

        //实例化日期选择的对话框
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(startDay));
        DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(),
                //时间更改监听器
                (view, year, month, dayOfMonth) -> {
                    //设置日期
                    c.set(year, month, dayOfMonth);
                    //更新开学日期
                    setStartDay(c.getTimeInMillis());
                },
                //初始日期
                c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setFirstDayOfWeek(isSunday ? 1 : 2);
        datePickerDialog.show();
    }

    /**
     * 当前周
     */
    public void setCurrentWeek(@NonNull View v) {
        View view = LayoutInflater.from(v.getContext())
                .inflate(R.layout.layout_number_picker_with_unit, null);
        //数值选择器的最大值与最小值
        NumberPicker numberPicker = view.findViewById(R.id.number_picker_with_unit_view);
        numberPicker.setMaxValue(weeks);
        numberPicker.setMinValue(1);
        //设置初值,即获取当前周
        int currentWeek;
        long time = new Date().getTime() - TimeUtils.calDateWeekStartTime(
                startDay, isSunday);
        if (time / TimeUtils.WEEK + 1 < 1)
            //如果所得周数小于1,则设为1
            currentWeek = 1;
        else if (time / TimeUtils.WEEK + 1 > weeks)
            //如果所得周数大于课程表的总周数,则表示学期已结束,设为最大的值
            currentWeek = weeks;
        else
            currentWeek = (int) (time / TimeUtils.WEEK + 1);
        numberPicker.setValue(currentWeek);

        //构造dialog
        new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.timetable_edit_timetable_title_current_week)
                .setView(view)
                //保存修改
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, ((dialog, which) -> {
                    //重新设置开学日期
                    setStartDay(startDay - (numberPicker.getValue() - (int) (time / TimeUtils.WEEK + 1)) * TimeUtils.WEEK);
                }))
                //取消保存
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                //创建并显示
                .show();
    }

    /**
     * 学期总周数
     */
    public void setWeeks(@NonNull View v) {
        View view = LayoutInflater.from(v.getContext())
                .inflate(R.layout.layout_number_picker_with_unit, null);
        NumberPicker numberPicker = view.findViewById(R.id.number_picker_with_unit_view);
        //数值选择器的最大值与最小值
        numberPicker.setMaxValue(30);
        numberPicker.setMinValue(1);
        //设置初值,即当前设置
        numberPicker.setValue(weeks);
        //构造dialog
        new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.timetable_edit_timetable_title_weeks)
                .setView(view)
                //保存修改
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, (dialog, which) -> {
                    //更新课程表设置的list和recyclerView
                    this.weeks = numberPicker.getValue();
                    notifyPropertyChanged(BR.weeks);
                })
                //取消保存
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                //创建并显示
                .show();
    }

    /**
     * 上课一般时间
     */
    public void setPeriod(@NonNull View v) {
        //单个数值选择器
        View view = LayoutInflater.from(v.getContext()).inflate(
                R.layout.layout_number_picker_with_unit, null);
        //设置数值选择器的单位
        view.findViewById(R.id.number_picker_with_unit_text).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.number_picker_with_unit_text))
                .setText(R.string.timetable_edit_timetable_minute);
        //设置数值选择器显示的最大值与最小值
        String[] displayedValues = new String[24];
        //显示的值为5,10,15,20...一直到120,共24个值
        for (int i = 0; i < displayedValues.length; i++)
            displayedValues[i] = String.valueOf(5 * (i + 1));
        //设置数值选择器的最大值与最小值
        NumberPicker numberPicker =
                view.findViewById(R.id.number_picker_with_unit_view);
        numberPicker.setMaxValue(displayedValues.length - 1);
        numberPicker.setMinValue(0);
        numberPicker.setDisplayedValues(displayedValues);
        //设置初值
        numberPicker.setValue(period / 5 - 1);

        //构造dialog
        new AlertDialog.Builder(v.getContext())
                //设置确认按钮的响应事件
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, (dialog, which) -> {
                    period = (numberPicker.getValue() + 1) * 5;
                    resetStartAndEndTime();
                    notifyPropertyChanged(BR.period);
                })
                .setTitle(R.string.timetable_edit_timetable_title_period)
                .setView(view)
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                .show();
    }

    /**
     * 下课一般时长
     */
    public void setRestPeriod(@NonNull View v) {
        //单个数值选择器
        View view = LayoutInflater.from(v.getContext()).inflate(
                R.layout.layout_number_picker_with_unit, null);
        //设置数值选择器的单位
        view.findViewById(R.id.number_picker_with_unit_text).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.number_picker_with_unit_text))
                .setText(R.string.timetable_edit_timetable_minute);
        //设置数值选择器显示的最大值与最小值
        String[] displayedValues = new String[24];
        //显示的值为5,10,15,20...一直到120,共24个值
        for (int i = 0; i < displayedValues.length; i++)
            displayedValues[i] = String.valueOf(5 * (i + 1));
        //设置数值选择器的最大值与最小值
        NumberPicker numberPicker =
                view.findViewById(R.id.number_picker_with_unit_view);
        numberPicker.setMaxValue(displayedValues.length - 1);
        numberPicker.setMinValue(0);
        numberPicker.setDisplayedValues(displayedValues);
        //设置初值
        numberPicker.setValue(restPeriod / 5 - 1);

        //构造dialog
        new AlertDialog.Builder(v.getContext())
                //设置确认按钮的响应事件
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, (dialog, which) -> {
                    restPeriod = (numberPicker.getValue() + 1) * 5;
                    resetStartAndEndTime();
                    notifyPropertyChanged(BR.restPeriod);
                })
                .setTitle(R.string.timetable_edit_timetable_title_rest_period)
                .setView(view)
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                .show();
    }

    // region 设置各时段节数

    /**
     * 上午
     */
    public void setMorningSection(@NonNull View v) {
        View view = LayoutInflater.from(v.getContext())
                .inflate(R.layout.layout_number_picker_with_unit, null);
        //数值选择器的最大值与最小值
        NumberPicker numberPicker = view.findViewById(R.id.number_picker_with_unit_view);
        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(1);
        //设置初值,即设置当前设置的节数
        numberPicker.setValue(section[MORNING]);

        //构造dialog
        new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.timetable_edit_timetable_morning_lessons)
                .setView(view)
                //保存修改
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, (dialog, which) -> setSection(MORNING, numberPicker.getValue()))
                //取消保存
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                //创建并显示
                .show();
    }

    /**
     * 下午
     */
    public void setAfternoonSection(@NonNull View v) {
        View view = LayoutInflater.from(v.getContext())
                .inflate(R.layout.layout_number_picker_with_unit, null);
        //数值选择器的最大值与最小值
        NumberPicker numberPicker = view.findViewById(R.id.number_picker_with_unit_view);
        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(1);
        //设置初值,即设置当前设置的节数
        numberPicker.setValue(section[AFTERNOON]);

        //构造dialog
        new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.timetable_edit_timetable_afternoon_lessons)
                .setView(view)
                //保存修改
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, (dialog, which) -> setSection(AFTERNOON, numberPicker.getValue()))
                //取消保存
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                //创建并显示
                .show();
    }

    /**
     * 晚上
     */
    public void setEveningSection(@NonNull View v) {
        View view = LayoutInflater.from(v.getContext())
                .inflate(R.layout.layout_number_picker_with_unit, null);
        //数值选择器的最大值与最小值
        NumberPicker numberPicker = view.findViewById(R.id.number_picker_with_unit_view);
        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(0);
        //设置初值,即设置当前设置的节数
        numberPicker.setValue(section[EVENING]);

        //构造dialog
        new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.timetable_edit_timetable_evening_lessons)
                .setView(view)
                //保存修改
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, (dialog, which) -> setSection(EVENING, numberPicker.getValue()))
                //取消保存
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                //创建并显示
                .show();
    }
    // endregion

    // endregion

    // endregion

    /**
     * 用于处理修改一天三段时间的课程数
     */
    public class LessonTimeModel {
        public int no;
        public String startTime = "";
        public String endTime = "";

        public LessonTimeModel(int no) {
            this.no = no;
            if (checkShowTime()) {
                this.startTime = Timetable.this.startTime.get(no);
                this.endTime = Timetable.this.endTime.get(no);
            }
        }

        public String getNo() {
            return ResourcesUtils.getString(R.string.timetable_edit_timetable_lesson, no + 1);
        }

        public String getTime() {
            return startTime + " - " + endTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LessonTimeModel that = (LessonTimeModel) o;
            return no == that.no && Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(no, startTime, endTime);
        }
    }

    /**
     * 时间工具类
     */
    private static class TimeUtils extends com.teleostnacl.common.android.utils.TimeUtils {
        /**
         * 判断形如"HH:mm"的字符串所代表的时间,后者时间比前者晚多少
         */
        public static int calLateTime(String time1, String time2) {
            //将各时间转换成数字
            int hour1 = spiltHour(time1);
            int hour2 = spiltHour(time2);
            int minute1 = spiltMinute(time1);
            int minute2 = spiltMinute(time2);
            //计算相差的时间
            return (hour2 - hour1) * 60 + (minute2 - minute1);
        }

        /**
         * 将形如"HH:mm"的字符串所代表的时间取出小时
         */
        public static int spiltHour(@NonNull String time) {
            return Integer.parseInt(time.split(":")[0]);
        }

        /**
         * 将形如"HH:mm"的字符串所代表的时间取出小时
         */
        public static int spiltMinute(@NonNull String time) {
            return Integer.parseInt(time.split(":")[1]);
        }

        /**
         * 将形如"HH:mm"的字符串所代表的时间加上指定的时间(分),返回得到的时间
         */
        @NonNull
        public static String calTime(String time, int interval) {
            //将各时间转换成数字
            int hour = spiltHour(time);
            int minute = spiltMinute(time);

            minute += interval;

            while (minute >= 60) {
                hour++;
                minute -= 60;
            }

            while (hour >= 24) {
                hour -= 24;
            }

            return String.format(Locale.getDefault(), "%02d", hour) + ":" +
                    String.format(Locale.getDefault(), "%02d", minute);
        }
    }
}
