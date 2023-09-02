package com.teleostnacl.common.android.database.converter;

import androidx.room.TypeConverter;

import com.teleostnacl.common.java.util.NumberUtils;

//分割符为" "的TypeConverter
public class StringSpiltSpaceConverter {
    //String[] 转 String, 分隔符为" "
    @TypeConverter
    public static String stringsCovertToString(String[] strings) {
        if (strings == null || strings.length == 0) return null;
        StringBuilder stringBuilder = new StringBuilder();
        for (String j : strings) {
            //对字符串中的空格进行处理
            stringBuilder.append(j.replaceAll("%", "%25")
                    .replaceAll(" ", "%32")).append(" ");
        }

        //去掉最后一个空格
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    //String 转 String [], 分隔符为" "
    @TypeConverter
    public static String[] stringCovertToStrings(String string) {
        if (string == null || string.equals("")) return null;

        String[] result = string.split(" ");

        for (int i = 0; i < result.length; i++) {
            //还原空格
            if (result[i].contains("%32")) {
                result[i] = result[i].replaceAll("%32", " ");
            }
            //还原百分号
            if (result[i].contains("%25"))
                result[i] = result[i].replaceAll("%25", "%");
        }

        return result;
    }

    //Integer [] 转 String, 分隔符为" "
    @TypeConverter
    public static String IntegersCovertToString(Integer[] integers) {
        if (integers == null || integers.length == 0) return null;

        String[] tmp = new String[integers.length];
        for (int i = 0; i < integers.length; i++) {
            tmp[i] = String.valueOf(integers[i]);
        }

        //去掉最后一个空格
        return stringsCovertToString(tmp);
    }

    //String 转 Integer [], 分隔符为" "
    @TypeConverter
    public static Integer[] StringCovertToIntegers(String string) {
        if (string == null || string.equals("")) return null;

        String[] tmp = stringCovertToStrings(string);

        Integer[] result = new Integer[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = NumberUtils.parseInt(tmp[i], 0);
        }

        return result;
    }

    //int[] 转 String, 分隔符为" "
    @TypeConverter
    public static String intsCovertToString(int[] integers) {
        if (integers == null || integers.length == 0) return null;

        String[] tmp = new String[integers.length];
        for (int i = 0; i < integers.length; i++) {
            tmp[i] = String.valueOf(integers[i]);
        }

        //去掉最后一个空格
        return stringsCovertToString(tmp);
    }

    //String 转 String [], 分隔符为" "
    @TypeConverter
    public static int[] stringCovertToInts(String string) {
        if (string == null || string.equals("")) return null;

        String[] tmp = stringCovertToStrings(string);

        int[] result = new int[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = NumberUtils.parseInt(tmp[i], 0);
        }

        return result;
    }
}
