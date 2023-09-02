package com.teleostnacl.common.java.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    /**
     * 判断所给的时间是否为今天
     * @param time 给定的时间
     * @return 所给的时间是否为今天
     */
    public static boolean isToday(long time) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        String today = "" + calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DATE);

        calendar.setTimeInMillis(time);
        String day = "" + calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DATE);

        return today.equals(day);
    }

    /**
     * 判断所给的时间是否在这个月
     * @param time 给定的时间
     * @return 所给的时间是否为这个月
     */
    public static boolean isThisMonth(long time) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        String today = "" + calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH);

        calendar.setTimeInMillis(time);
        String day = "" + calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH);

        return today.equals(day);
    }
}
