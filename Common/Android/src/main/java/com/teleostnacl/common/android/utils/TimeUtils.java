package com.teleostnacl.common.android.utils;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.R;
import com.teleostnacl.common.android.context.ResourcesUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 */
public class TimeUtils {

    private static String[] day_sum;

    public final static long SECOND = 1000L;
    public final static long MINUTE = 60 * SECOND;
    public final static long HOUR = 60 * MINUTE;
    public final static long DAY = 24 * HOUR;
    public final static long WEEK = 7 * DAY;

    /**
     * 计算所给的时间戳所在的周的起始日的时间戳
     *
     * @param date     需要计算的时间戳
     * @param isSunday 一周的起始日是否为周日
     * @return 返回该周起始日的时间戳
     */
    public static long calDateWeekStartTime(long date, boolean isSunday) {
        //记录这周开始日期
        long weekStartDay;
        //获取Calendar实例并且设置时间
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(date));
        if (c.get(Calendar.DAY_OF_WEEK) == (isSunday ? Calendar.SUNDAY : Calendar.MONDAY))
            //如果日期恰好是一周起始日,则返回该日期
            weekStartDay = date;
        else {
            //不是则需要进行换算
            if (isSunday)
                //一周起始为周日时,减去所过的天数,得到日期
                weekStartDay = date - (c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY) * DAY;
            else {
                //一周起始为周一时
                if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                    //如果日期为周日,则设置为-6,即一个轮回
                    weekStartDay = date - 6 * DAY;
                else
                    weekStartDay = date - (c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) * DAY;
            }
        }
        c.setTime(new Date(weekStartDay));
        //将时间调整为0时0分0秒
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /**
     * 获取今天开始的时间戳
     *
     * @return 今天开始的时间戳
     */
    public static long getTodayStartTime() {
        //获取Calendar实例并且设置为现在时间
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        //将时间调整为0时0分0秒
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /**
     * 获取指定月份的上一月的最后一天
     *
     * @param year  年
     * @param month 月
     * @return 获取指定月份的上一月的最后一天 格式为yyyyMMdd
     */
    @NonNull
    public static String getLastMonthLastDay(int year, int month, String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        calendar.add(Calendar.DATE, -1);

        return String.format(Locale.CHINA, format,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE));
    }

    /**
     * 获取当前月的最后一天
     *
     * @param year  年
     * @param month 月
     * @return 获取当前月的最后一天, 格式为yyyyMMdd
     */
    @NonNull
    public static String getMonthLastDay(int year, int month, String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DATE, -1);

        return String.format(Locale.CHINA, format,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE));
    }

    public static String[] getDaySum() {
        if (day_sum == null) {
            day_sum = ResourcesUtils.getResources().getStringArray(R.array.week_sun);
        }
        return day_sum;
    }

    /**
     * @param date 时间
     * @return 返回是周几的字符串
     */
    public static String getDayOfWeek(long date) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(date));
        return getDaySum()[c.get(Calendar.DAY_OF_WEEK) - 1];
    }
}
