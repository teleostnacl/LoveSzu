package com.teleostnacl.loveszu;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.context.PmAmUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.SPUtils;
import com.teleostnacl.loveszu.databinding.ActivityMainBinding;
import com.teleostnacl.szu.electricity.ElectricityActivity;
import com.teleostnacl.szu.libs.activity.BaseActivity;
import com.teleostnacl.szu.libs.model.NeumorphCardViewTextWithIconModel;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;
import com.teleostnacl.szu.libs.utils.activity.ActivityUtil;
import com.teleostnacl.szu.libs.utils.activity.BulletinUtil;
import com.teleostnacl.szu.libs.utils.activity.DocumentationUtil;
import com.teleostnacl.szu.libs.utils.activity.ElectricityUtil;
import com.teleostnacl.szu.libs.utils.activity.ExaminationUtil;
import com.teleostnacl.szu.libs.utils.activity.FileUtil;
import com.teleostnacl.szu.libs.utils.activity.GradeUtil;
import com.teleostnacl.szu.libs.utils.activity.GrowthRecordUtil;
import com.teleostnacl.szu.libs.utils.activity.LibraryUtil;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;
import com.teleostnacl.szu.libs.utils.activity.PaperUtil;
import com.teleostnacl.szu.libs.utils.activity.SchemeUtil;
import com.teleostnacl.szu.libs.utils.activity.TimetableUtil;
import com.teleostnacl.szu.libs.view.recyclerview.adapter.NeumorphCardViewTextViewIconListAdapter;
import com.teleostnacl.szu.login.LoginActivity;
import com.teleostnacl.szu.timetable.TimetableActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity {

    static {
        // 静态注册广播
        ContextUtils.registerReceiver(ActivityBroadcastReceiver.class, ActivityUtil.getActivityIntentFilter());
    }

    private ActivityMainBinding binding;

    private final List<NeumorphCardViewTextWithIconModel> models = new ArrayList<>();

    private final NeumorphCardViewTextViewIconListAdapter listAdapter = new NeumorphCardViewTextViewIconListAdapter();

    // 记录隐私声明的版本 即 更新一次需要显示一次
    private final static int PRIVACY_VERSION = ResourcesUtils.getInteger(R.integer.privacy_version);

    private final static String SP_FILE_PRIVACY_VERSION = "privacy";
    private final static String SP_KEY_PRIVACY_VERSION = "privacy_version";

    // 深圳大学师生疫情防控信息平台的网址
    private static final String CAN_OUT_URL = "https://www1.szu.edu.cn/NCP/iQRcode";

    // 深大校历的网址
    private static final String SCHOOL_CALENDAR_URL = "https://www.szu.edu.cn/xxgk/xl.htm";

    private SharedPreferences privacySP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取是否已经同意了隐私申明
        privacySP = SPUtils.getSP(SP_FILE_PRIVACY_VERSION);
        if (SPUtils.getInt(privacySP, SP_KEY_PRIVACY_VERSION, 0) != PRIVACY_VERSION) {
            // 更新后还未同意隐私声明 则展示隐私声明
            showPrivacyDialog(true);
        } else {
            // 还未登录 则进行登录
            if (!LoginUtil.isLoggedIn()) {
                // 有自动登录的账户则启动登录Activity进行自动登录
                if (LoginUtil.hasAutoLoginUser()) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra(LoginUtil.ARG_AUTO_FLAG, true);
                    startActivity(intent);
                } else {
                    // 未设置自动登录 清空cookies
                    SzuCookiesDatabase.getInstance().getCookieJar().clearCookies();
                }
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name) + " ver." + PmAmUtils.getVersionCode());

        // 配置显示的model
        configModels();
        listAdapter.submitList(models);
        binding.recyclerView.setAdapter(listAdapter);

        // 设置toolbar subtitle 样式
        binding.toolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubtitleStyle);

        // 注册LiveData观察者
        LoginUtil.getUserModelLiveData().observe(this, userModel -> {
            // 处理登录的逻辑
            models.get(0).setTitle(LoginUtil.isLoggedIn() ?
                    ResourcesUtils.getString(com.teleostnacl.szu.login.R.string.item_switch_account, Objects.requireNonNull(LoginUtil.getUserModel()).username) :
                    getString(com.teleostnacl.szu.login.R.string.item_login));
            binding.toolbar.setSubtitle(LoginUtil.getLoginInformation());

            // 处理入校审核状态
            models.get(1).setTitle(getString(com.teleostnacl.szu.login.R.string.item_can_out, LoginUtil.getVerify()));
        });

        // 动态注册shortcut
        addDynamicShortcuts();
    }


    /**
     * 配置显示的model
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void configModels() {
        models.clear();

        // 登录或切换账号
        models.add(new NeumorphCardViewTextWithIconModel("", getDrawable(com.teleostnacl.szu.login.R.drawable.drawable_login), () -> {
            // 已经登录成功 设置为切换账号
            LoginUtil.login(!LoginUtil.isLoggedIn());
        }));

        // 出入校审核状态
        models.add(new NeumorphCardViewTextWithIconModel("", getDrawable(com.teleostnacl.szu.login.R.drawable.drawable_can_out), () -> {
            // 跳转打开深圳大学师生疫情防控信息平台
            ContextUtils.startBrowserActivity(CAN_OUT_URL);
        }));

        // 深大校历
        models.add(new NeumorphCardViewTextWithIconModel(getString(R.string.item_school_calendar), getDrawable(R.drawable.drawable_school_calendar), () -> {
            // 打开深大校历的网站
            ContextUtils.startBrowserActivity(SCHOOL_CALENDAR_URL);
        }));

        // 公文通
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.bulletin.R.string.item_bulletin), getDrawable(com.teleostnacl.szu.bulletin.R.drawable.drawable_bulletin), BulletinUtil::startBulletin));

        // 课程表
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.timetable.R.string.item_timetable), getDrawable(com.teleostnacl.szu.timetable.R.drawable.drawable_timetable), TimetableUtil::startTimetable));

        // 图书馆
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.library.R.string.item_library), getDrawable(com.teleostnacl.szu.library.R.drawable.drawable_library), LibraryUtil::startLibrary));

        // 宿舍用电查询
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.electricity.R.string.item_electricity), getDrawable(com.teleostnacl.szu.electricity.R.drawable.drawable_electricity), ElectricityUtil::startElectricity));

        // 我的考试安排
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.examination.R.string.item_examination), getDrawable(com.teleostnacl.szu.examination.R.drawable.drawable_examination), ExaminationUtil::startExamination));

        // 成绩查询
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.grade.R.string.item_grade), getDrawable(com.teleostnacl.szu.grade.R.drawable.drawable_grade), GradeUtil::startGrade));

        // 成长记录
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.record.R.string.item_growth_record), getDrawable(com.teleostnacl.szu.record.R.drawable.drawable_growth_record), GrowthRecordUtil::startGrowthRecord));

        // 培养方案查询
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.scheme.R.string.item_scheme), getDrawable(com.teleostnacl.szu.scheme.R.drawable.drawable_scheme), SchemeUtil::startScheme));

        // 学业完成查询
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.scheme.R.string.item_academic), getDrawable(com.teleostnacl.szu.scheme.R.drawable.drawable_academic), SchemeUtil::startAcademic));

        // 本科论文工具
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.paper.R.string.item_paper), getDrawable(com.teleostnacl.szu.paper.R.drawable.drawable_paper), PaperUtil::startPaper));

        // 证明文件下载
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.documentation.R.string.item_documentation), getDrawable(com.teleostnacl.szu.documentation.R.drawable.drawable_documentation), DocumentationUtil::startDocument));

        // 常用文件下载
        models.add(new NeumorphCardViewTextWithIconModel(getString(com.teleostnacl.szu.file.R.string.item_file_download), getDrawable(com.teleostnacl.szu.file.R.drawable.drawable_file_download), FileUtil::startFile));

        // 隐私声明
        models.add(new NeumorphCardViewTextWithIconModel(getString(R.string.privacy_title), getDrawable(R.drawable.drawable_pravicy), () -> showPrivacyDialog(false)));
    }

    /**
     * 动态注册shortcut
     */
    private void addDynamicShortcuts() {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        if (shortcutManager == null) {
            return;
        }

        shortcutManager.addDynamicShortcuts(Arrays.asList(
                createShortcutInfo(ResourcesUtils.getString(com.teleostnacl.szu.login.R.string.item_can_out, ""), com.teleostnacl.szu.login.R.drawable.drawable_can_out, ContextUtils.getStartBrowserActivityIntent(CAN_OUT_URL)),
                createShortcutInfo(ResourcesUtils.getString(R.string.item_school_calendar), R.drawable.drawable_school_calendar, ContextUtils.getStartBrowserActivityIntent(SCHOOL_CALENDAR_URL)),
                createShortcutInfo(ResourcesUtils.getString(com.teleostnacl.szu.timetable.R.string.item_timetable), com.teleostnacl.szu.timetable.R.drawable.icon_shortcut_timetable, ContextUtils.getShortcutIntent(TimetableActivity.class)),
                createShortcutInfo(ResourcesUtils.getString(com.teleostnacl.szu.electricity.R.string.item_electricity), com.teleostnacl.szu.electricity.R.drawable.drawable_electricity, ContextUtils.getShortcutIntent(ElectricityActivity.class))));
    }

    private ShortcutInfo createShortcutInfo(String s, int iconResId, Intent intent) {
        return new ShortcutInfo.Builder(this, s)
                .setLongLabel(s)
                .setShortLabel(s)
                .setIcon(Icon.createWithResource(this, iconResId))
                .setIntent(intent)
                .build();
    }

    /**
     * 展示隐私申明
     *
     * @param needTime 是否需要倒计时
     */
    private void showPrivacyDialog(boolean needTime) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.privacy_title)
                .setView(R.layout.layout_privacy)
                .setNeutralButton(R.string.privacy_home, (dialog, which) ->
                        ContextUtils.startBrowserActivity("https://github.com/teleostnacl/LoveSzu"))
                .setNegativeButton(" ", null)
                .setPositiveButton(" ", null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            Button positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

            // 需要倒计时 则需显示同意或不同意
            if (needTime) {
                alertDialog.setCancelable(false);

                positive.setVisibility(View.VISIBLE);
                negative.setVisibility(View.VISIBLE);

                // 同意 写入SP 并跳转登录页面
                positive.setOnClickListener(v -> {
                    SPUtils.putInt(privacySP.edit(), SP_KEY_PRIVACY_VERSION, PRIVACY_VERSION).apply();
                    LoginUtil.login(!LoginUtil.isLoggedIn());
                    dialog.dismiss();
                });
                positive.setClickable(false);
                positive.setAllCaps(false);

                Timer timer = new Timer();
                // 1s减少1
                timer.scheduleAtFixedRate(new TimerTask() {
                    private int time = 30;

                    @Override
                    public void run() {
                        if (time == 0) {
                            timer.cancel();
                            positive.post(() -> {
                                positive.setText(R.string.privacy_agree);
                                TypedValue value = new TypedValue();
                                getTheme().resolveAttribute(android.R.attr.colorPrimary, value, true);
                                positive.setTextColor(value.data);
                                positive.setClickable(true);
                            });
                        } else {
                            positive.post(() -> {
                                positive.setTextColor(Color.GRAY);
                                positive.setText(getString(R.string.privacy_agree_time, time));
                            });
                        }

                        time--;
                    }
                }, 0, 1000);

                // 不同意 则退出应用
                negative.setText(R.string.privacy_disagree);
                negative.setOnClickListener(v -> finish());
            } else {
                // 隐藏positive按钮
                negative.setVisibility(View.GONE);

                // 设置negative按钮
                positive.setText(R.string.privacy_undo);
                // 撤销同意并清除数据
                positive.setOnClickListener(v -> {
                    finish();
                    PmAmUtils.getAm().clearApplicationUserData();
                });
            }
        });

        alertDialog.show();
    }
}