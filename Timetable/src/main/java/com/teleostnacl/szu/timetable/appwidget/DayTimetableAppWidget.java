package com.teleostnacl.szu.timetable.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.teleostnacl.common.android.utils.HtmlUtils;
import com.teleostnacl.common.android.utils.TimeUtils;
import com.teleostnacl.szu.timetable.R;
import com.teleostnacl.szu.timetable.model.Timetable;
import com.teleostnacl.szu.timetable.viewmodel.TimetableAppWidgetViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 每日课表的桌面组件
 */
public class DayTimetableAppWidget extends AppWidgetProvider {
    private final TimetableAppWidgetViewModel mTimetableAppWidgetViewModel = new TimetableAppWidgetViewModel();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        String packageName = context.getPackageName();

        // 获取默认显示的课程表
        Timetable timetable = mTimetableAppWidgetViewModel.getSerialIdMaxTimetableFromTimetable();

        RemoteViews remoteViews = new RemoteViews(packageName, R.layout.layout_app_widget_day_timetable);

        remoteViews.removeAllViews(R.id.main_layout);

        // 标题
        RemoteViews title = new RemoteViews(packageName, R.layout.layout_app_widget_day_timetable_title);
        long date = new Date().getTime();

        // 日期 + 星期 + 今日课程
        title.setTextViewText(R.id.app_widget_day_timetable_title,
                HtmlUtils.fromHtml(R.string.timetable_app_widget_day_timetable_title,
                        new SimpleDateFormat("MM/dd", Locale.CHINA).format(date),
                        TimeUtils.getDayOfWeek(date)));

        // 周数
        title.setTextViewText(R.id.app_widget_day_timetable_week, timetable.getCurrentWeek());

        remoteViews.addView(R.id.main_layout, title);

        for (int i = 0; i < 5; i++) {
            RemoteViews textView = new RemoteViews(packageName, R.layout.item_app_widget_day_timetable);
            remoteViews.addView(R.id.main_layout, textView);
        }

        ComponentName componentName = new ComponentName(context, DayTimetableAppWidget.class);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }
}
