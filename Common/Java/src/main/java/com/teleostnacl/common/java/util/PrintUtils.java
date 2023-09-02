package com.teleostnacl.common.java.util;

/**
 * 对部分数据类型打印
 */
public class PrintUtils {
    /**
     * 打印一维数组
     */
    public static String printArray(Object[] objects) {
        if (objects == null) {
            return "null";
        }

        StringBuilder stringBuilder = new StringBuilder("[");

        for (Object o : objects) {
            stringBuilder.append(o).append(", ");
        }

        int start = stringBuilder.lastIndexOf(", ");
        if (start != -1) {
            stringBuilder.delete(start, stringBuilder.length());
        }

        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    /**
     * 打印二维数组
     */
    public static String printArray(Object[][] objects) {
        if (objects == null) {
            return "null";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Object[] objects1 : objects) {
            stringBuilder.append(printArray(objects1)).append("\n");
        }

        return stringBuilder.toString();
    }

    /**
     * 打印int型一维数组
     */
    public static String printIntArray(int[] ints) {
        if (ints == null) {
            return "null";
        }

        Integer[] integers = new Integer[ints.length];
        for (int i = 0; i < ints.length; i++) {
            integers[i] = ints[i];
        }

        return printArray(integers);
    }


    /**
     * 打印int型二维数组
     */
    public static String printIntArray(int[][] ints) {
        if (ints == null) {
            return "null";
        }

        if (ints.length < 1) {
            return "[]";
        }

        Integer[][] integers = new Integer[ints.length][ints[0].length];
        for (int i = 0; i < ints.length; i++) {
            for (int j = 0; j < ints[i].length; j++) {
                integers[i][j] = ints[i][j];
            }
        }

        return printArray(integers);
    }
}
