package com.teleostnacl.common.java.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NumberUtils {

    /**
     * 生成一串不重复的顺序的随机数list
     * 如 start = 1, sum = 5 则一种可能的情况为 5 2 1 4 3
     *
     * @param start 最小的随机数
     * @param sum   生成随机数的数量
     * @return 一串随机数
     */
    public static List<Integer> getOrderDifferentRandom(int start, int sum) {
        return getDifferentRandom(start, start + sum, sum);
    }

    /**
     * 生成一串不重复的随机数list
     *
     * @param start 生成随机数的最小值(包括)
     * @param end   生成随机数的最大值(不包括)
     * @param sum   生成随机数的总数
     * @return 一串随机数
     */
    public static List<Integer> getDifferentRandom(int start, int end, int sum) {
        int delta = end - start;
        if (sum > delta) sum = delta;
        List<Integer> list = new ArrayList<>(sum);
        Random random = new Random();
        do {
            //新生成的随机数
            int tmp = start + random.nextInt(delta);
            //如果list中不包含该数,则将其放入list中
            if (!list.contains(tmp)) {
                list.add(tmp);
            }
            //如果list的大小还未达到限制,即所需要的随机数个数,则继续循环
        } while (list.size() != sum);
        return list;
    }

    public static int parseInt(String s, int defVal) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ignored) {
        }

        return defVal;
    }

    public static float parseFloat(String s, float defVal) {
        try {
            return Float.parseFloat(s);
        } catch (Exception ignored) {
        }

        return defVal;
    }

    public static double parseDouble(String s, double defVal) {
        try {
            return Double.parseDouble(s);
        } catch (Exception ignored) {
        }

        return defVal;
    }

    public static long parseLong(String s, long defVal) {
        try {
            return Long.parseLong(s);
        } catch (Exception ignored) {
        }

        return defVal;
    }
}
