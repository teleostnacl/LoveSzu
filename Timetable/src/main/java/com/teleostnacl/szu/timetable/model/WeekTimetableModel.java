package com.teleostnacl.szu.timetable.model;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.teleostnacl.szu.timetable.R;

/**
 * 显示周课表视图时 与DataBinding相关的模型
 */
public class WeekTimetableModel {

    /**
     * 周课表日期行所使用的Model
     */
    public static class WeekTimetableDateModel {

        //保存TextView默认的颜色
        private static ColorStateList defaultTextColor;

        //是否可见
        public int weekVisibility = View.VISIBLE;
        public int dateVisibility = View.VISIBLE;

        //显示的内容
        public String weekText;
        public String dateText;

        public WeekTimetableDateModel(String year) {
            this.dateText = year;
            this.weekVisibility = View.GONE;
        }

        public WeekTimetableDateModel(String weekText, String dateText, boolean isToday) {
            this.weekText = weekText;
            this.dateText = dateText;
            this.isToday = isToday;
        }

        public boolean isToday;

        @BindingAdapter("today")
        public static void judgeToday(TextView textView, boolean isToday) {
            if (isToday) {
                if (defaultTextColor == null) defaultTextColor = textView.getTextColors();
                //如果该项是今天,更改显示样式
                textView.setTextColor(textView.getContext().getColor(R.color.timetable_week_today_color));
                textView.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                if (defaultTextColor != null) {
                    textView.setTextColor(defaultTextColor);
                    textView.setTypeface(Typeface.DEFAULT);
                }
            }
        }

    }

    /**
     * 周课表时间列所使用的Model
     */
    public static class WeekTimetableTimeModel {
        //上课节次
        public String period;
        //该节次上课时间
        public String startTime;
        //该节次下课时间
        public String endTime;

        //是否显示上下课时间
        public final boolean showTime;

        public WeekTimetableTimeModel(String period) {
            this.period = period;
            showTime = false;
        }

        public WeekTimetableTimeModel(String period, String startTime, String endTime) {
            this.period = period;
            this.startTime = startTime;
            this.endTime = endTime;
            this.showTime = true;
        }

        public int isShowTime() {
            return showTime ? View.VISIBLE : View.GONE;
        }
    }
}
