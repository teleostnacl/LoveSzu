package com.teleostnacl.szu.timetable.repository;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.retrofit.RetrofitUtils;
import com.teleostnacl.common.android.retrofit.cookies.Cookie;
import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.ExecutorServiceUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.libs.ehall.model.EHallModel;
import com.teleostnacl.szu.libs.ehall.repository.EHallRepository;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;
import com.teleostnacl.szu.timetable.R;
import com.teleostnacl.szu.timetable.database.LessonDao;
import com.teleostnacl.szu.timetable.database.TimetableDao;
import com.teleostnacl.szu.timetable.database.TimetableDatabase;
import com.teleostnacl.szu.timetable.model.Lesson;
import com.teleostnacl.szu.timetable.model.Timetable;
import com.teleostnacl.szu.timetable.model.json.Xnxqdm;
import com.teleostnacl.szu.timetable.retrofit.TimetableApi;
import com.teleostnacl.szu.timetable.retrofit.TimetableRetrofit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.Single;

public class TimetableRepository {
    private final TimetableApi timetableApi = TimetableRetrofit.getInstance().timetableApi();
    private final TimetableDao timetableDao = TimetableDatabase.getInstance().timetableDao();
    private final LessonDao lessonDao = TimetableDatabase.getInstance().lessonDao();

    // 使用单线程对数据库进行操作
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * @return 从数据库中获取课程表
     */
    public List<Timetable> getTimetablesFromDatabase() {
        return ExecutorServiceUtils.submitByExecutor(executor, timetableDao::getAllTimetables, ArrayList::new);
    }

    /**
     * @return 从数据库中获取指定课程表的课程信息
     */
    public List<Lesson> getLessonsByTimetableFromDatabase(@NonNull Timetable timetable) {
        return lessonDao.getLessonsByTimetable(timetable.id);
    }

    /**
     * 获取与指定课程具有相同课程表id,课程名,课程简称,上课地点,授课教师,课程号,课序号的课程(去除自身)
     *
     * @param lesson  指定课程
     * @param lessons 课程集
     * @return 相同的课程的集合
     */
    private List<Lesson> getSameLesson(Lesson lesson, @NonNull List<Lesson> lessons) {
        // 遍历lessons, 获取相同的课程
        List<Lesson> sameLessons = new ArrayList<>();
        for (Lesson temp : lessons) {
            if (lesson.isSameLesson(temp)) {
                sameLessons.add(temp);
            }
        }

        return sameLessons;
    }

    /**
     * 导入到数据库中
     *
     * @param timetables 需要导入的课程表
     */
    public void importTimetables(@NonNull Set<Timetable> timetables) {
        for (Timetable timetable : timetables) {
            List<Lesson> lessons = timetable.lessonsFromImport;
            // 如果课程为空, 则不进行处理
            if (lessons.size() == 0) {
                continue;
            }

            // 判断数据库中是否有相同的id的课程表
            if (ExecutorServiceUtils.submitByExecutor(executor, () -> timetableDao.getByID(timetable.id) != null, () -> true)) {
                // 删除旧课程表
                executor.execute(() -> timetableDao.delete(timetable));
            }

            // 导入到数据库中
            executor.execute(() -> timetableDao.insert(timetable));
            for (Lesson lesson : lessons) {
                executor.execute(() -> lessonDao.insert(lesson));
            }
        }
    }

    // region 从深圳大学网上办事大厅导入课程表信息

    /**
     * 从网上办事大厅导入课程表信息
     */
    public Single<List<Timetable>> importFromEHall(boolean fromOther) {
        ImportFromEHallModel model = new ImportFromEHallModel();

        // 从其他账号导入 则临时切换cookies信息
        Map<String, Cookie> originCookies = null;
        if (fromOther) {
            originCookies = SzuCookiesDatabase.getInstance().getCookieJar().setMap(new HashMap<>(), false);
        }
        Map<String, Cookie> finalOriginCookies = originCookies;
        // 先请求办事大厅 获取cookies信息
        return EHallRepository.getEHallApp(timetableApi)
                .flatMap(responseBodyResponse -> {
                    model.referer = RetrofitUtils.getUrlFromResponse(responseBodyResponse);

                    return Single.zip(
                            // 获取当前学年学期
                            timetableApi.getDQXNXQ(),
                            // 获取用户入学学年和学号
                            timetableApi.getXZNJ(), (responseBody, responseBody1) -> {
                                // 获取当前学年学期信息
                                model.dqxnxq = responseBody.string().split("\"DM\":\"")[1].split("\"")[0];

                                String bodyString = responseBody1.string();
                                // 获取用户入学学年和学号信息
                                model.year = Integer.parseInt(bodyString.split("\"XZNJ\":\"")[1].split("\"")[0]);
                                model.no = bodyString.split("\"XH\":\"")[1].split("\"")[0];

                                return model;
                            });
                })
                .flatMap(o -> importFromEHall(model.no, model.year, model.dqxnxq))
                // 发生错误时也需要还原cookies
                .doOnError(throwable -> {
                    // 还原cookies
                    if (fromOther && finalOriginCookies != null) {
                        SzuCookiesDatabase.getInstance().getCookieJar().setMap(finalOriginCookies, true);
                    }
                })
                .map(timetables -> {
                    // 还原cookies
                    if (fromOther && finalOriginCookies != null) {
                        SzuCookiesDatabase.getInstance().getCookieJar().setMap(finalOriginCookies, true);
                    }
                    return timetables;
                });
    }

    /**
     * 遍历各学年学期获取课程表
     *
     * @param xh     学号
     * @param year   入学学年
     * @param dqxnxq 当前学年学期
     */
    private Single<List<Timetable>> importFromEHall(String xh, int year, String dqxnxq) {
        // 需要导入的学年学期的字符串
        List<Single<Timetable>> list = new ArrayList<>();

        //获取课表中序号的最大值,并由此产生导入的序列值
        int serial = ExecutorServiceUtils.submitByExecutor(executor, timetableDao::getSerialIdMax, () -> 0) + 1;

        //学期
        int xq = 1;
        String xnxq;
        do {
            xnxq = year + "-" + (year + 1) + "-" + xq;
            // 添加需要导入的课程表
            list.add(importXnxq(xh, xnxq, serial++));
            if (++xq == 3) {
                xq = 1;
                year++;
            }
        } while (!xnxq.equals(dqxnxq));

        return Single.zip(list, objects -> {
            List<Timetable> list1 = new ArrayList<>();
            for (Object timetable : objects) {
                if (timetable instanceof Timetable) {
                    list1.add((Timetable) timetable);
                }
            }
            return list1;
        });
    }

    /**
     * 导入指定学年学期的课程表
     */
    private Single<Timetable> importXnxq(String xh, @NonNull String xnxq, int serial) {
        String[] tmp = xnxq.split("-");
        String xn = tmp[0] + "-" + tmp[1];
        int xq = Integer.parseInt(tmp[2]);

        // 创建课程表
        return createXnxqTimetable(xh, xn, xq, serial).flatMap(timetable ->
                getLessonsByXnxq(timetable, xn, xq).map(lessons -> {
                    timetable.lessonsFromImport = lessons;
                    return timetable;
                }));
    }

    /**
     * 创建指定学年学期和学号的课程表
     *
     * @param xh 学号
     * @param xn 学年
     * @param xq 学期
     * @return 创建的课程表
     */
    @NonNull
    private Single<Timetable> createXnxqTimetable(String xh, @NonNull String xn, int xq, int serial) {
        // 创建该学年学期的课程表, 由学校代码+学号加学年学期组成id
        Timetable timetable = new Timetable(xh + xn.replace("-", "") + xq);

        // 第一学期
        if (xq == 1) {
            timetable.timetableName = ResourcesUtils.getString(R.string.timetable_import_from_ehall_xnxq,
                    xn, ResourcesUtils.getString(R.string.timetable_import_from_ehall_first));
        }
        // 第二学期
        else {
            timetable.timetableName = ResourcesUtils.getString(R.string.timetable_import_from_ehall_xnxq,
                    xn, ResourcesUtils.getString(R.string.timetable_import_from_ehall_second));
        }

        // 如果学年是大于2020年的,则使用新的上课下课时间表
        if (Integer.parseInt(xn.substring(0, 4)) >= 2020) {
            timetable.setSection(5, 5, 4);
            timetable.startTime.addAll(Arrays.asList(ResourcesUtils.getResources().getStringArray(R.array.szu_start_time_new)));
            timetable.endTime.addAll(Arrays.asList(ResourcesUtils.getResources().getStringArray(R.array.szu_end_time_new)));
            timetable.period = 40;
            timetable.restPeriod = 5;
        }
        // 旧课表需要考虑冬令时和夏令时,不进行设置时间
        else {
            timetable.showTime = false;
            timetable.setSection(4, 4, 4);
            timetable.period = 40;
            timetable.restPeriod = 10;
        }

        timetable.serialID = serial;

        // 请求服务器获取该学期的课程表信息
        return timetableApi.getCXJCS(xn, xq).map(responseBody -> {
            String bodyString = responseBody.string();
            // 获取开学日期
            String time = bodyString.split("\"XQKSRQ\":\"")[1].split("\"")[0];
            // 格式例如: 2021-03-01 00:00:00
            timetable.startDay = Objects.requireNonNull(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()).parse(time)).getTime();

            // 获取总周数
            timetable.weeks = NumberUtils.parseInt(bodyString.split("\"ZJXZC\":")[1].split("\\}")[0], 18);

            return timetable;
        });
    }

    /**
     * 导入指定学年学期的课程
     *
     * @param timetable 所属的课程表
     * @param xn        学年
     * @param xq        学期
     */
    private Single<List<Lesson>> getLessonsByXnxq(Timetable timetable, String xn, int xq) {
        return timetableApi.getXSKCB(xn + "-" + xq).map(xnxqdm -> {
                    List<Xnxqdm.Datas.Xskcb.Row> lessonList = xnxqdm.datas.xskcb.rows;
                    //如果所获得的课程数量为0,则将课程表删除
                    if (lessonList.size() == 0) {
                        return new ArrayList<Lesson>();
                    }

                    //用于记录是否存在重复项
                    List<String> list = new ArrayList<>();
                    // 记录已存储的课程
                    List<Lesson> lessons = new ArrayList<>();

                    //随机数生成颜色标识
                    int color = new Random().nextInt(ColorResourcesUtils.getColorSum());
                    //遍历lessonList,将Row转化为Lesson并存入数据库
                    for (Xnxqdm.Datas.Xskcb.Row row : lessonList) {
                        //课程名 + 上课老师 + 上课地点 + 上课时间 组成标识课程的原始字符串
                        String temp = row.KCM + row.SKJS + row.YPSJDD;
                        //如果list记录中含有该原始字符串,则是重复课程,跳过此次循环
                        if (list.contains(temp)) {
                            continue;
                        }
                        //否则加入list中
                        else {
                            list.add(temp);
                        }

                        //新建课程
                        Lesson lesson = new Lesson();
                        //课程表id
                        lesson.timetableID = timetable.id;
                        //课程名
                        lesson.lessonName = row.KCM;
                        //上课老师
                        lesson.teacher = row.SKJS;
                        //课序号
                        lesson.lessonSerialNumber = row.KXH;
                        //课程编号
                        lesson.lessonNumber = row.KCH;

                        //先进行拆分字符串,拆分成多个时间和地点的组合,然后再设置时间地点
                        String[] tmp = spitSzuLessonTimeAndLocation(row.YPSJDD);
                        for (String s : tmp) {
                            setSzuLessonTimeAndLocation(lessons, new Lesson(lesson), s, color);
                            //随机增加,切换颜色,为下一课程做准备
                            color += new Random().nextInt(ColorResourcesUtils.getColorSum());
                            if (color >= ColorResourcesUtils.getColorSum()) {
                                color -= ColorResourcesUtils.getColorSum();
                            }
                        }
                    }

                    return lessons;
                })
                // 发生错误 返回空列表
                .onErrorReturn(throwable -> {
                    Logger.e(throwable);
                    return new ArrayList<>();
                });
    }

    /**
     * 自定义拆分规则拆分课程的时间和上课地点
     *
     * @param s 可能含有多个时间段的上课时间和地点
     * @return 进行拆分
     */
    @NonNull
    private String[] spitSzuLessonTimeAndLocation(@NonNull String s) {
        //如果字符串不含有逗号,则无需拆分
        if (!s.contains(",")) {
            return new String[]{s};
        }
        List<String> subStringList = new ArrayList<>();
        //分割标识位
        int index = 0;
        for (int i = 0; i < s.length(); i++) {
            //如果此处为逗号,则考虑进行分割
            if (s.charAt(i) == ',') {
                //上一个字符为'周',则无需进行分割
                if (s.charAt(i - 1) == '周') continue;
                //进行分割
                subStringList.add(s.substring(index, i));
                index = i + 1;
            }
        }
        //截取最后一个字符串
        subStringList.add(s.substring(index));

        String[] subStrings = new String[subStringList.size()];
        subStringList.toArray(subStrings);
        //返回拆分完后的字符串
        return subStrings;
    }

    /**
     * 设置课程的时间和上课地点
     *
     * @param lessons 已经录入的课程表
     * @param lesson  课程
     * @param s       含单个上课时间段 和 地点的字符串
     * @param color   颜色
     */
    private void setSzuLessonTimeAndLocation(List<Lesson> lessons, Lesson lesson, @NonNull String s, int color) {
        //空格分割,形成四个子字符串,分别为,上课的周,上课的天,上课节数,上课地点
        String[] tmp = s.split(" ");
        //记录上课周数
        Integer[] weeks;
        Matcher m = Pattern.compile("\\d*-\\d*").matcher(tmp[0]);
        //诸如1-17周,2-16周
        if (m.find()) {
            int index = 0;
            //截取前面的数字
            for (int i = 0; i < tmp[0].length(); i++) {
                if (Character.isDigit(tmp[0].charAt(i))) index++;
                else break;
            }
            //获取开始的周数
            int start = Integer.parseInt(tmp[0].substring(0, index++));
            //截取后一个数字
            int index_tmp = index;
            for (int i = index; i < tmp[0].length(); i++) {
                if (Character.isDigit(tmp[0].charAt(i))) index++;
                else break;
            }
            int end = Integer.parseInt(tmp[0].substring(index_tmp, index));

            //单双周课程
            if (tmp[0].contains("单") || tmp[0].contains("双")) {
                weeks = new Integer[(end - start) / 2 + 1];
                for (int i = 0; i < weeks.length; i++)
                    weeks[i] = start + i * 2 - 1;
            }
            //非单双周课程
            else {
                weeks = new Integer[end - start + 1];
                for (int i = 0; i < weeks.length; i++)
                    weeks[i] = start + i - 1;
            }
        }
        //一周一周显示的
        else {
            String[] weeksStr = tmp[0].split(",");
            weeks = new Integer[weeksStr.length];
            for (int i = 0; i < weeks.length; i++) {
                weeks[i] = Integer.parseInt(weeksStr[i].replace("周", "")) - 1;
            }
        }
        lesson.weeks.addAll(Arrays.asList(weeks));

        //匹配字符串设置上课在星期几
        String[] daysArray = new String[]{"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        for (int i = 0; i < daysArray.length; i++) {
            if (tmp[1].equals(daysArray[i])) {
                lesson.weekDay = i;
                break;
            }
        }

        //含有"第"字,索引值起始值为1.否则为0
        int start = 0;
        if (tmp[2].contains("第")) start = 1;
        //记录上课节数
        int indexPeriod = start;
        //截取前面的数字
        for (int i = start; i < tmp[2].length(); i++) {
            if (Character.isDigit(tmp[2].charAt(i))) indexPeriod++;
            else break;
        }
        //获取开始的节数
        int startPeriod = Integer.parseInt(tmp[2].substring(start, indexPeriod++)) - 1;
        //截取后一个数字
        int indexPeriod_tmp = indexPeriod;
        for (int i = indexPeriod; i < tmp[2].length(); i++) {
            if (Character.isDigit(tmp[2].charAt(i))) indexPeriod++;
            else break;
        }
        //获取结束的节数
        int endPeriod = Integer.parseInt(tmp[2].substring(indexPeriod_tmp, indexPeriod));
        //设置开始上课的节数与上课的总节数
        lesson.startTime = startPeriod;
        lesson.periodTime = endPeriod - startPeriod;

        //记录上课地点
        lesson.location = tmp[3];
        // 记录颜色
        lesson.color = color;

        //检查是否存在与之为相同课程,且是否仅相差一节课
        for (Lesson sameLesson : getSameLesson(lesson, lessons)) {
            //相同课程使用相同的颜色
            lesson.color = sameLesson.color;
            //上课时间仅差一节课,为同一次课
            if (sameLesson.weekDay == lesson.weekDay &&
                    sameLesson.weeks.equals(lesson.weeks) &&
                    Math.abs(sameLesson.startTime - lesson.startTime) ==
                            //如果新课程的上课时间晚于旧课程,则用旧课程的持续时间和差值作比较,否则拿新课程
                            (lesson.startTime > sameLesson.startTime ? sameLesson.periodTime : lesson.periodTime)) {
                //合并课程
                lesson.startTime = Math.min(lesson.startTime, sameLesson.startTime);
                lesson.periodTime = lesson.periodTime + sameLesson.periodTime;

                lessons.remove(sameLesson);
                break;
            }
        }

        // 将课程存进lessons中
        lessons.add(lesson);
    }

    // endregion

    /**
     * 判断课程表是否为默认选择
     */
    public boolean isTimetableDefault(@NonNull Timetable timetable) {
        return ExecutorServiceUtils.submitByExecutor(executor,
                () -> timetableDao.getSerialIdMax() == timetable.serialID, () -> false);
    }

    /**
     * 将Timetable设为默认选择
     */
    public void setTimetableDefault(@NonNull Timetable timetable) {
        timetable.serialID = ExecutorServiceUtils.submitByExecutor(executor, timetableDao::getSerialIdMax, () -> 0) + 1;
    }

    /**
     * 获取serial id 最大的课表 即默认显示的课表
     */
    public Timetable getSerialIdMaxTimetableFromTimetable() {
        return ExecutorServiceUtils.submitByExecutor(executor, timetableDao::getSerialIdMaxTimetable, () -> null);
    }

    /**
     * 删除课程
     */
    public void deleteLesson(Lesson lesson) {
        executor.execute(() -> lessonDao.delete(lesson));
    }

    /**
     * 保存课程
     */
    public void saveLesson(Lesson lesson) {
        executor.execute(() -> lessonDao.insertOrUpdate(lesson));
    }

    /**
     * 删除课程
     */
    public void deleteTimetable(Timetable timetable) {
        executor.execute(() -> timetableDao.delete(timetable));
    }

    /**
     * 保存课程
     */
    public void saveTimetable(Timetable timetable) {
        if (ExecutorServiceUtils.submitByExecutor(executor,
                () -> timetableDao.getByID(timetable.id), () -> null) == null) {
            executor.execute(() -> timetableDao.insert(timetable));
        } else {
            executor.execute(() -> timetableDao.update(timetable));
        }
    }

    /**
     * 导入课程表时使用的模型
     */
    private static class ImportFromEHallModel extends EHallModel {
        // 当前学年学期
        public String dqxnxq;

        // 入学年份
        public int year;
    }
}
