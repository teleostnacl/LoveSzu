package com.teleostnacl.szu.timetable.viewmodel;

import static com.teleostnacl.szu.timetable.fragment.WeekTimetableFragment.LONGER;
import static com.teleostnacl.szu.timetable.fragment.WeekTimetableFragment.SHORTER;
import static com.teleostnacl.szu.timetable.model.Timetable.AFTERNOON;
import static com.teleostnacl.szu.timetable.model.Timetable.EVENING;
import static com.teleostnacl.szu.timetable.model.Timetable.MORNING;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.utils.TimeUtils;
import com.teleostnacl.szu.timetable.fragment.WeekTimetableFragment;
import com.teleostnacl.szu.timetable.fragment.WeekTimetableFragment.LessonModel;
import com.teleostnacl.szu.timetable.model.Lesson;
import com.teleostnacl.szu.timetable.model.Timetable;
import com.teleostnacl.szu.timetable.model.WeekTimetableModel.WeekTimetableDateModel;
import com.teleostnacl.szu.timetable.model.WeekTimetableModel.WeekTimetableTimeModel;
import com.teleostnacl.szu.timetable.repository.TimetableRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TimetableViewModel extends ViewModel {

    private final TimetableRepository timetableRepository = new TimetableRepository();

    // 记录存储在数据库中所有的课程表
    private List<Timetable> timetables;
    // 当前所选择的Timetable
    private Timetable currentTimetable;

    // 记录当前WeekTimetableFragment 的 ViewPager2 显示的是第几页课程表
    public int viewPage2CurrentPage = -1;

    // 记录当前所需修改的课程表
    private Timetable currentModifyTimetable;

    // 记录当前所需要修改的课程
    private Lesson currentModifyLesson;

    /**
     * 从办事大厅中导入课程表
     *
     * @param fromOther 是否从其他账号导入
     * @return 是否成功
     */
    public Single<List<Timetable>> importFromEHall(boolean fromOther) {
        timetables = null;
        setCurrentTimetable(null);
        return timetableRepository.importFromEHall(fromOther)
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return new ArrayList<>();
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void importTimetables(@NonNull Set<Timetable> timetables) {
        timetableRepository.importTimetables(timetables);
    }

    /**
     * @return 从数据库中获取所有课程表
     */
    @NonNull
    public List<Timetable> getTimetables() {
        if (timetables == null) {
            return new ArrayList<>();
        }
        return timetables;
    }

    /**
     * @return 直接获取当前显示的课程表
     */
    public Timetable getTimetable() {
        return currentTimetable;
    }

    /**
     * @return 获取当前的显示的Timetable, 并从数据库中获取已存储在数据库中的课程表(若未获取)
     */
    public Single<Optional<Timetable>> getCurrentTimetable() {
        return Single.just(new Object())
                .map(o -> {
                    if (timetables == null) {
                        timetables = timetableRepository.getTimetablesFromDatabase();
                    }
                    return timetables;
                })
                .map((Function<List<Timetable>, Optional<Timetable>>) timetables -> {
                    // 课程表集合不为空时
                    if (timetables.size() != 0) {
                        // 获取已经记录在currentTimetable的值
                        // 为空时 去除list中第一个
                        if (currentTimetable == null) {
                            setCurrentTimetable(timetables.get(0));
                        }
                        return Optional.of(currentTimetable);
                    }

                    // 课程表集合为空 表示本地数据中暂时没有课程表
                    return Optional.empty();
                })
                .onErrorReturnItem(Optional.empty())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 设置需当前显示的课程表
     *
     * @param timetable 需当前显示的课程表
     */
    public void setCurrentTimetable(Timetable timetable) {
        currentTimetable = timetable;
        viewPage2CurrentPage = -1;
    }

    /**
     * 从数据库获取对应课程表的Lesson的List,并制成可供RecyclerView使用的数据
     *
     * @param timetable 所需处理的课程表
     */
    public Single<List<List<LessonModel>>> getLessonModel(Timetable timetable) {
        //午休晚休
        final LessonModel noon = new LessonModel(true);
        final LessonModel dusk = new LessonModel(false);

        //占位符
        final LessonModel placeHolder = new LessonModel();

        return Single.just(new Object()).flatMap(o -> {

                    // 先从数据库中获取数据
                    List<Lesson> lessonList = timetableRepository.getLessonsByTimetableFromDatabase(timetable);

                    //处理数据,最后一个用于传递课程表的高度
                    List<List<LessonModel>> weekLessonsList = new ArrayList<>(timetable.weeks + 1);
                    Map<Integer, List<Lesson>> weekMapLesson = timetable.weekMapLesson = new HashMap<>();

                    //记录有多少个课程格子
                    int sheetSum = timetable.getSections() * (timetable.getColumnSize() - 1);
                    //使用null进行填充list
                    for (int i = 0; i < timetable.weeks; i++) {
                        List<LessonModel> list = new ArrayList<>(timetable.getItemCount());
                        List<Lesson> list1 = new ArrayList<>(sheetSum);
                        // 使用null进行填充
                        for (int j = 0; j < timetable.getItemCount(); j++) {
                            list.add(null);
                            if (j < sheetSum) {
                                list1.add(null);
                            }
                        }
                        weekLessonsList.add(list);
                        weekMapLesson.put(i, list1);
                    }

                    return Single.zip(
                            // 处理日期
                            Single.just(o).map(o12 -> {
                                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.CHINA);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.CHINA);

                                //获取开学日期所在周的一周起始日
                                long startDate = TimeUtils.calDateWeekStartTime(timetable.startDay, timetable.isSunday);

                                //设置每周的日期
                                for (int i = 0; i < timetable.weeks; i++) {
                                    //获取该周的一周的开始时间
                                    long weekStartDate = startDate + i * TimeUtils.WEEK;
                                    //设置年份
                                    weekLessonsList.get(i).set(0, new LessonModel(
                                            new WeekTimetableDateModel(yearFormat.format(weekStartDate))));

                                    //设置每天的日期
                                    for (int j = 1; j < timetable.getColumnSize(); j++) {
                                        long dayStartTime = weekStartDate + (j - 1) * TimeUtils.DAY;
                                        //获取星期
                                        String day = TimeUtils.getDayOfWeek(dayStartTime);
                                        //获取日期
                                        String date = dateFormat.format(dayStartTime);
                                        //如果恰好跨年,则显示年
                                        if (date.equals("01/01")) date =
                                                new SimpleDateFormat("yy/MM/dd", Locale.CHINA)
                                                        .format(dayStartTime);

                                        //设置日期并判断是否为今天
                                        weekLessonsList.get(i).set(j, new LessonModel(
                                                new WeekTimetableDateModel(day, date,
                                                        dayStartTime == TimeUtils.getTodayStartTime())));
                                    }
                                }

                                return o12;
                            }).subscribeOn(Schedulers.computation()),
                            // 处理上课时间
                            Single.just(o).map(o12 -> {
                                // 如果设置了不显示时间,则仅设置课节数
                                List<LessonModel> list = new ArrayList<>(timetable.getSections());
                                // 显示时间
                                if (timetable.checkShowTime()) {
                                    for (int i = 0; i < timetable.getSections(); i++) {
                                        list.add(new LessonModel(new WeekTimetableTimeModel(String.valueOf(i + 1),
                                                timetable.startTime.get(i), timetable.endTime.get(i))));
                                    }
                                }
                                // 不显示时间
                                else {
                                    for (int i = 0; i < timetable.getSections(); i++) {
                                        list.add(new LessonModel(new WeekTimetableTimeModel(String.valueOf(i + 1))));
                                    }

                                }

                                //写入weekLessonsList中
                                for (int j = 0; j < timetable.weeks; j++) {
                                    int tmp = 0;

                                    //上午
                                    for (int i = timetable.getColumnSize();
                                         i < timetable.getColumnSize() + timetable.section[MORNING];
                                         i++) {
                                        weekLessonsList.get(j).set(i, list.get(tmp++));
                                    }

                                    //下午
                                    for (int i = timetable.getNoonIndex() + 1;
                                         i < timetable.getNoonIndex() + 1 + timetable.section[AFTERNOON]; i++) {
                                        weekLessonsList.get(j).set(i, list.get(tmp++));
                                    }

                                    //晚上
                                    for (int i = timetable.getDuskIndex() + 1;
                                         i < timetable.getDuskIndex() + 1 + timetable.section[EVENING]; i++) {
                                        weekLessonsList.get(j).set(i, list.get(tmp++));
                                    }

                                    //修改午休 晚休
                                    weekLessonsList.get(j).set(timetable.getNoonIndex(), noon);
                                    if (timetable.section[EVENING] != 0) {
                                        weekLessonsList.get(j).set(timetable.getDuskIndex(), dusk);
                                    }
                                }
                                return o;
                            }).subscribeOn(Schedulers.computation()),
                            // 处理课程
                            Single.just(o).map(o12 -> {
                                boolean needLongSheet = false;
                                for (Lesson lesson : lessonList) {
                                    // 获取经拆分跨时段(如果需要的话)获取到的课程
                                    List<int[]> timeList = lesson.spiltPeriod(timetable.section);
                                    // 如果为空,则忽略该课程
                                    if (timeList == null) continue;
                                    for (int[] time : timeList) {
                                        // 遍历课程所在的所有周
                                        for (int week : lesson.weeks) {
                                            // 设置课程,小于总周数的才进行设置
                                            if (week < weekLessonsList.size()) {
                                                weekLessonsList.get(week).set(
                                                        timetable.getPositionInRecyclerView(lesson.weekDay, time[0]),
                                                        new LessonModel(lesson, time[1]));
                                                // 放入map中
                                                weekMapLesson.computeIfPresent(week, (integer, lessons) -> {
                                                    lessons.set(timetable.getPositionInList(lesson.weekDay, time[0]), lesson);
                                                    return lessons;
                                                });

                                                // 设置占位符
                                                for (int j = time[0] + 1; j < time[0] + time[1]; j++) {
                                                    weekLessonsList.get(week).set(
                                                            timetable.getPositionInRecyclerView(lesson.weekDay, j),
                                                            new LessonModel());

                                                    // 放入map中
                                                    int finalJ = j;
                                                    weekMapLesson.computeIfPresent(week, (integer, lessons) -> {
                                                        lessons.set(timetable.getPositionInList(lesson.weekDay, finalJ), lesson);
                                                        return lessons;
                                                    });
                                                }

                                            }
                                        }

                                        //如果有课程仅有一节课,且课程名长度大于4个字符, 课程地点和老师不为空 ,则高度为125dp
                                        if (time[1] == 1 && lesson.lessonName.length() > 4 &&
                                                !(TextUtils.isEmpty(lesson.teacher) && TextUtils.isEmpty(lesson.location))) {
                                            needLongSheet = true;
                                        }
                                    }
                                }

                                return needLongSheet;
                            }).subscribeOn(Schedulers.computation()),
                            (o1, o2, needLongSheet) -> {
                                //从list移除占位符,并为空课程创建LessonModel,记录午休和晚休的位置
                                for (List<LessonModel> lessonModels : weekLessonsList) {
                                    lessonModels.removeAll(Collections.singletonList(placeHolder));

                                    //替换空课程的null
                                    lessonModels.replaceAll(lessonModel -> lessonModel != null ? lessonModel :
                                            new LessonModel(WeekTimetableFragment.BLANK_LESSON_VIEW_TYPE));

                                    int noonIndex = lessonModels.indexOf(noon);
                                    int duskIndex = lessonModels.indexOf(dusk);
                                    //在末尾添加LessonModel,用于记录午休和晚休的位置
                                    lessonModels.add(new LessonModel(noonIndex));
                                    lessonModels.add(new LessonModel(duskIndex));
                                }

                                //在末尾添加该课程表的单元格高度
                                List<LessonModel> list = new ArrayList<>();
                                list.add(new LessonModel(ResourcesUtils.getDensityPx(needLongSheet ? LONGER : SHORTER)));
                                weekLessonsList.add(list);

                                return weekLessonsList;
                            });
                })
                .onErrorReturn(throwable -> {
                    ToastUtils.makeToast(com.teleostnacl.common.android.R.string.unknown_error);
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 检查课程表是否为默认选择
     */
    public boolean isTimetableDefault(Timetable timetable) {
        return timetableRepository.isTimetableDefault(timetable);
    }

    /**
     * 将Timetable设为默认选择
     */
    public void setTimetableDefault(@NonNull Timetable timetable) {
        timetableRepository.setTimetableDefault(timetable);
    }

    /**
     * 删除课程
     */
    public void deleteLesson(Lesson lesson) {
        timetableRepository.deleteLesson(lesson);
    }

    /**
     * 保存课程
     */
    public void saveLesson(Lesson lesson) {
        timetableRepository.saveLesson(lesson);
    }

    /**
     * 删除课程
     */
    public void deleteTimetable(Timetable timetable) {
        setCurrentTimetable(null);
        timetables = null;
        timetableRepository.deleteTimetable(timetable);
    }

    /**
     * 保存课程表
     */
    public void saveTimetable(Timetable timetable) {
        setCurrentTimetable(null);
        timetables = null;
        timetableRepository.saveTimetable(timetable);
    }

    public Timetable getCurrentModifyTimetable() {
        return currentModifyTimetable;
    }

    public void setCurrentModifyTimetable(Timetable currentModifyTimetable) {
        this.currentModifyTimetable = currentModifyTimetable;
    }

    public Lesson getCurrentModifyLesson() {
        return currentModifyLesson;
    }

    public void setCurrentModifyLesson(Lesson currentModifyLesson) {
        this.currentModifyLesson = currentModifyLesson;
    }
}
