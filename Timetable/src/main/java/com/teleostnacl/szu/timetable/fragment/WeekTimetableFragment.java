package com.teleostnacl.szu.timetable.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.teleostnacl.szu.timetable.model.Timetable.AFTERNOON;
import static com.teleostnacl.szu.timetable.model.Timetable.EVENING;
import static com.teleostnacl.szu.timetable.model.Timetable.MORNING;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.view.MenuCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.divider.MaterialDivider;
import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseDisposableFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.utils.TimeUtils;
import com.teleostnacl.common.android.view.ViewUtils;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;
import com.teleostnacl.szu.timetable.R;
import com.teleostnacl.szu.timetable.TimetableActivity;
import com.teleostnacl.szu.timetable.databinding.FragmentWeekTimetableBinding;
import com.teleostnacl.szu.timetable.databinding.ItemWeekTimetableDateBinding;
import com.teleostnacl.szu.timetable.databinding.ItemWeekTimetableLessonBinding;
import com.teleostnacl.szu.timetable.databinding.ItemWeekTimetableTimeBinding;
import com.teleostnacl.szu.timetable.databinding.LayoutLessonDetailBinding;
import com.teleostnacl.szu.timetable.model.Lesson;
import com.teleostnacl.szu.timetable.model.Timetable;
import com.teleostnacl.szu.timetable.model.WeekTimetableModel.WeekTimetableDateModel;
import com.teleostnacl.szu.timetable.model.WeekTimetableModel.WeekTimetableTimeModel;
import com.teleostnacl.szu.timetable.viewmodel.TimetableViewModel;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import io.reactivex.rxjava3.core.Single;

public class WeekTimetableFragment extends BaseDisposableFragment {

    /**
     * 如果存在一次课只上一堂课时, 为了展示完整, 单个sheetHeight的高度要相对较大
     * 特定义两个变量来表示这一差别
     */
    public static final float LONGER = 110;
    public static final float SHORTER = 70;

    private TimetableViewModel timetableViewModel;

    //region RecyclerView 各个视图的ViewType
    //空课程使用的ViewType
    public final static int BLANK_LESSON_VIEW_TYPE = 0;
    //时间列使用的ViewType
    public final static int TIME_VIEW_TYPE = -3;
    //日期行使用的ViewType
    public final static int DATE_VIEW_TYPE = -4;
    //endregion

    // 具体课程单元格所使用的RecyclerViewPool
    private RecyclerView.RecycledViewPool recycledViewPool;

    // 定义课程表中一格的高度
    private int sheetHeight;
    // 定义课程表中一个span的宽度
    private int sheetWidth;

    // 显示的Timetable
    private Timetable timetable;
    // 记录所有周课表内容的List
    private List<List<LessonModel>> weekLessonsList;

    // Binding
    private FragmentWeekTimetableBinding binding;

    // 显示周课表的ViewPager2的Adapter
    private final RecyclerView.Adapter<RecyclerView.ViewHolder> viewPager2Adapter = new RecyclerView.Adapter<>() {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LinearLayout linearLayout = new LinearLayout(requireContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(new RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            // 日期
            linearLayout.addView(new WeekTimetableDateRecyclerView(requireContext()));
            // 分界限
            MaterialDivider divider = new MaterialDivider(requireContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, ResourcesUtils.getDensityPx()));
            linearLayout.addView(divider);

            // 使用ScrollView包裹课程表 可进行滑动
            ScrollView scrollView = new ScrollView(requireContext());
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            scrollView.setVerticalScrollBarEnabled(false);

            LinearLayout linearLayout1 = new LinearLayout(requireContext());
            linearLayout1.setOrientation(LinearLayout.VERTICAL);
            linearLayout1.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            scrollView.addView(linearLayout1);

            //上午课程
            linearLayout1.addView(new WeekTimetablePeriodRecyclerView(requireContext(), MORNING));

            //添加午休
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            textView.setPadding(ResourcesUtils.dp_int_4, ResourcesUtils.dp_int_4,
                    ResourcesUtils.dp_int_4, ResourcesUtils.dp_int_4);
            textView.setTextSize(14);
            textView.setText(R.string.timetable_week_timetable_fragment_noon);
            linearLayout1.addView(textView);

            //下午课程
            linearLayout1.addView(new WeekTimetablePeriodRecyclerView(requireContext(), AFTERNOON));

            if (timetable.section[EVENING] != 0) {
                //添加晚休
                TextView textView1 = new TextView(getContext());
                textView1.setGravity(Gravity.CENTER);
                textView1.setLayoutParams(new RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                textView1.setPadding(ResourcesUtils.dp_int_4, ResourcesUtils.dp_int_4,
                        ResourcesUtils.dp_int_4, ResourcesUtils.dp_int_4);
                textView1.setTextSize(14);
                textView1.setText(R.string.timetable_week_timetable_fragment_dusk);
                linearLayout1.addView(textView1);

                //晚上课程
                linearLayout1.addView(new WeekTimetablePeriodRecyclerView(requireContext(), EVENING));
            }

            linearLayout.addView(scrollView);

            return new RecyclerView.ViewHolder(linearLayout) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            List<LessonModel> list = weekLessonsList.get(position);
            ((WeekTimetableDateRecyclerView) ((LinearLayout) holder.itemView).getChildAt(0))
                    .submitList(list.subList(0, timetable.getColumnSize()));

            int noonIndex = list.get(list.size() - 2).viewType;
            int duskIndex = list.get(list.size() - 1).viewType;

            LinearLayout linearLayout =
                    (LinearLayout) ((ScrollView) ((LinearLayout) holder.itemView).getChildAt(2)).getChildAt(0);

            //上午课程
            ((WeekTimetablePeriodRecyclerView) linearLayout.getChildAt(0))
                    .submitList(list.subList(timetable.getColumnSize(), noonIndex));
            //下午课程
            ((WeekTimetablePeriodRecyclerView) linearLayout.getChildAt(2))
                    .submitList(list.subList(noonIndex + 1, duskIndex == -1 ? list.size() : duskIndex));

            //晚上课程
            if (timetable.section[EVENING] != 0) {
                ((WeekTimetablePeriodRecyclerView) linearLayout.getChildAt(4))
                        .submitList(list.subList(duskIndex + 1, list.size() - 2));
            }
        }

        @Override
        public int getItemCount() {
            // 返回有多少周课表
            if (weekLessonsList != null) {
                return weekLessonsList.size();
            }

            return 0;
        }

        // region 禁用复用
        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        // endregion
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timetableViewModel = new ViewModelProvider(requireActivity()).get(TimetableViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_week_timetable,
                container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initToolbar();

        //去除标题栏的文字,并设置为正在加载
        binding.fragmentWeekTimetableToolbarTitle.setText(com.teleostnacl.common.android.R.string.loading);
        binding.fragmentWeekTimetableToolbarSubtitle.setText("");

        // 注册监听ViewPager2页面更改时的回调
        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // 更新Toolbar显示的周数
                binding.fragmentWeekTimetableToolbarTitle.setText(ResourcesUtils.getString(R.string.timetable_week_timetable_week_current, position + 1));
                // 更新记录当前周的LiveData
                timetableViewModel.viewPage2CurrentPage = position;
            }
        });

        showTimetable();
    }

    /**
     * 获取展示的timetable 并获取显示在课程表中的ModelList集合
     */
    private void showTimetable() {
        disposable.add(timetableViewModel.getCurrentTimetable()
                .flatMap(timetableOptional -> {
                    timetable = timetableOptional.orElse(null);
                    if (timetable == null) {
                        showBlankTimetableView();
                        return Single.never();
                    }

                    return timetableViewModel.getLessonModel(timetable);
                })
                .subscribe(this::showWeekTimetable));
    }

    /**
     * 展示暂无课程表的视图
     */
    private void showBlankTimetableView() {
        Menu menu = binding.fragmentWeekTimetableToolbar.getMenu();
        // 隐藏编辑课程表选项
        menu.getItem(1).setVisible(false);
        // 移除所有课程表
        menu.removeGroup(R.id.menu_week_timetable_group);

        binding.fragmentWeekTimetableToolbarTitle.setText("");
        binding.fragmentWeekTimetableToolbarSubtitle.setText("");
        binding.noneText.setText(R.string.timetable_week_timetable_empty);
        ViewUtils.setVisibleWithAnimation(binding.timetableEmpty);
        ViewUtils.setGoneWithAnimation(binding.loading);


        // 展开菜单
        binding.fragmentWeekTimetableToolbar.showOverflowMenu();
    }

    /**
     * 展示加载课程表出错的视图
     */
    private void showErrorTimetableView() {
        binding.fragmentWeekTimetableToolbarTitle.setText("");

        binding.noneText.setText(R.string.timetable_week_timetable_error);
        ViewUtils.setVisibleWithAnimation(binding.timetableEmpty);
        ViewUtils.setGoneWithAnimation(binding.loading);

        // 展开菜单
        binding.fragmentWeekTimetableToolbar.showOverflowMenu();
    }

    /**
     * 显示周课表
     *
     * @param lists 每周课表的List
     */
    private void showWeekTimetable(@NonNull List<List<LessonModel>> lists) {
        // 设置课程表名
        binding.fragmentWeekTimetableToolbarSubtitle.setText(timetable.timetableName);

        // 设置menu中显示所有课程表
        Menu menu = binding.fragmentWeekTimetableToolbar.getMenu();
        menu.getItem(1).setVisible(true);
        menu.removeGroup(R.id.menu_week_timetable_group);
        List<Timetable> list = timetableViewModel.getTimetables();
        for (int i = 0; i < list.size(); i++) {
            menu.add(R.id.menu_week_timetable_group, i, Menu.NONE, list.get(i).timetableName);
        }

        // lists的大小为0 则表示视图加载失败
        if (lists.size() == 0) {
            showErrorTimetableView();
            return;
        }

        // 设置缓存策略
        setRecycledViewPool();
        weekLessonsList = lists.subList(0, lists.size() - 1);

        // 获取单元格高度
        sheetHeight = lists.get(lists.size() - 1).get(0).viewType;
        // 获取单元格宽度
        sheetWidth = (int) (ResourcesUtils.getWidthPixels() / timetable.getSpanCount());

        // 通过设置Adapter进行刷新视图
        binding.viewPager2.setAdapter(null);
        binding.viewPager2.setAdapter(viewPager2Adapter);

        // 获取当前记录的页数
        int page = timetableViewModel.viewPage2CurrentPage;

        // 缓存所有周
        binding.viewPager2.beginFakeDrag();
        for (int i = 0; i < viewPager2Adapter.getItemCount(); i++) {
            //向右拖动为负,向左拖动为正
            binding.viewPager2.fakeDragBy(-ResourcesUtils.getWidthPixels());
        }
        binding.viewPager2.endFakeDrag();

        // 若为恢复数据,则恢复到指定页
        if (page != -1) {
            setPage(page);
        }
        // 设置显示当前周
        else {
            setCurrentWeek();
        }

        // 使用动画显示ViewPager2和隐藏加载动画
        ViewUtils.setVisibleWithAnimation(binding.viewPager2);
        ViewUtils.setGoneWithAnimation(binding.loading);
    }

    /**
     * 初始化Toolbar
     */
    private void initToolbar() {
        // 设置返回键
        NavigationUtils.navigationForToolbar(binding.fragmentWeekTimetableToolbar, v -> requireActivity().finish());

        //双击回到当前周
        binding.fragmentWeekTimetableToolbarTitle.setOnClickListener(new View.OnClickListener() {
            private boolean firstClick = true;

            @Override
            public void onClick(View v) {
                if (timetable != null) {
                    if (firstClick) {
                        firstClick = false;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> firstClick = true, 300);
                    } else {
                        setCurrentWeek();
                    }
                }
            }
        });

        Menu menu = binding.fragmentWeekTimetableToolbar.getMenu();
        // 设置menu组的分割线
        MenuCompat.setGroupDividerEnabled(menu, true);
        // 设置menu的点击事件
        binding.fragmentWeekTimetableToolbar.setOnMenuItemClickListener(item -> {
            // 新建课程表
            if (item.getItemId() == R.id.menu_add_timetable) {
                timetableViewModel.setCurrentModifyTimetable(new Timetable());
                NavigationUtils.navigate(requireView(), R.id.action_weekTimetableFragment_to_editTimetableFragment);
            }
            // 编辑课程表
            else if (item.getItemId() == R.id.menu_edit_timetable) {
                timetableViewModel.setCurrentModifyTimetable(new Timetable(timetable));
                NavigationUtils.navigate(requireView(), R.id.action_weekTimetableFragment_to_editTimetableFragment);
            }
            // 导入课程表
            else if (item.getItemId() == R.id.menu_import_timetable) {
                // 已经登录了, 则需要提示是否导入其它账号的课程表
                if (LoginUtil.isLoggedIn()) {
                    new AlertDialog.Builder(requireContext())
                            .setMessage(getString(R.string.timetable_import_from_other_tips, Objects.requireNonNull(LoginUtil.getUserModel()).username))
                            .setPositiveButton(R.string.timetable_import_from_other_yes, (dialog, which) -> importFromEHall(false))
                            .setNegativeButton(R.string.timetable_import_from_other_no, (dialog, which) -> importFromEHall(true))
                            .show();
                } else {
                    importFromEHall(false);
                }

            }
            // 创建桌面快捷方式
            else if (item.getItemId() == R.id.menu_create_timetable_shortcut) {
                ShortcutManager shortcutManager = requireContext().getSystemService(ShortcutManager.class);

                boolean result = false;

                if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
                    ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(requireContext(), ResourcesUtils.getString(R.string.item_timetable))
                            .setLongLabel(ResourcesUtils.getString(R.string.item_timetable))
                            .setShortLabel(ResourcesUtils.getString(R.string.item_timetable))
                            .setIcon(Icon.createWithResource(requireContext(), R.drawable.icon_shortcut_timetable))
                            .setIntent(new Intent(requireContext().getApplicationContext(), TimetableActivity.class).setAction(Intent.ACTION_VIEW))
                            .build();

                    Intent shortcutIntent = shortcutManager.createShortcutResultIntent(shortcutInfo);

                    //在此完善你接收到广播之后的动作
                    PendingIntent successCallback = PendingIntent.getBroadcast(
                            requireContext(), 0, shortcutIntent, PendingIntent.FLAG_IMMUTABLE);

                    result = shortcutManager.requestPinShortcut(shortcutInfo, successCallback.getIntentSender());
                }

                ToastUtils.makeToast(result ? R.string.timetable_week_timetable_create_timetable_shortcut_success : R.string.timetable_week_timetable_create_timetable_shortcut_fail);
            }
            // 切换课程表
            else if (item.getGroupId() == R.id.menu_week_timetable_group) {
                binding.loading.setVisibility(View.VISIBLE);
                binding.timetableEmpty.setVisibility(View.GONE);
                binding.viewPager2.setVisibility(View.GONE);
                timetable = timetableViewModel.getTimetables().get(item.getItemId());
                timetableViewModel.setCurrentTimetable(timetable);
                disposable.add(timetableViewModel.getLessonModel(timetable).subscribe(this::showWeekTimetable));
            }
            return true;
        });
    }

    /**
     * 设置当前周显示
     */
    private void setCurrentWeek() {
        setPage((int) ((new Date().getTime() -
                TimeUtils.calDateWeekStartTime(timetable.startDay, timetable.isSunday))
                / TimeUtils.WEEK));
    }

    /**
     * 设置ViewPager当前显示页
     *
     * @param position 需要显示的页
     */
    private void setPage(int position) {
        binding.viewPager2.beginFakeDrag();
        //向右拖动为负,向左拖动为正
        binding.viewPager2.fakeDragBy(
                -(position - binding.viewPager2.getCurrentItem()) * ResourcesUtils.getWidthPixels());
        binding.viewPager2.endFakeDrag();
    }

    /**
     * 设置缓存策略
     */
    private void setRecycledViewPool() {
        recycledViewPool = new RecyclerView.RecycledViewPool();

        //日期单元格的总缓存数为总数的三倍
        recycledViewPool.setMaxRecycledViews(DATE_VIEW_TYPE, timetable.getColumnSize() * 5);

        //时间单元格的总缓存数为总数的三倍
        recycledViewPool.setMaxRecycledViews(TIME_VIEW_TYPE, timetable.getSections() * 5);

        //空课程使用的ViewType
        recycledViewPool.setMaxRecycledViews(BLANK_LESSON_VIEW_TYPE, timetable.getItemCount() * 3);

        //课程使用的ViewType
        for (int i = 1; i < timetable.getMaxSection(); i++) {
            recycledViewPool.setMaxRecycledViews(i, timetable.getItemCount() * 3);
        }
    }

    /**
     * 从办事大厅导入课程表
     *
     * @param fromOther 是否导入其他账号的课程表
     */
    private void importFromEHall(boolean fromOther) {
        binding.loading.setVisibility(View.VISIBLE);
        binding.timetableEmpty.setVisibility(View.GONE);
        binding.viewPager2.setVisibility(View.GONE);
        binding.fragmentWeekTimetableToolbarTitle.setText(R.string.timetable_importing);
        binding.fragmentWeekTimetableToolbarSubtitle.setText("");
        disposable.add(timetableViewModel.importFromEHall(fromOther)
                .subscribe(timetables -> {
                    if (timetables.size() == 0) {
                        showTimetable();
                    } else {
                        showImportDialog(timetables);
                    }
                }));
    }

    private void showImportDialog(final List<Timetable> timetables) {
        // 最终需导入的课程表
        final Set<Timetable> importTimetable = new HashSet<>();

        // 创建alertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.timetable_import_choose_timetable_title)
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                .setPositiveButton(R.string.timetable_import, null)
                .setNeutralButton(com.teleostnacl.common.android.R.string.select_all, null)
                .setCancelable(false)
                .create();

        // 创建RecyclerView
        RecyclerView recyclerView = new RecyclerView(requireContext());
        // 设置水平padding
        int padding = ResourcesUtils.getDensityPx(12);
        recyclerView.setPadding(padding, 0, padding, 0);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ListAdapter<Timetable, RecyclerView.ViewHolder> listAdapter = new ListAdapter<>(new DefaultItemCallback<>()) {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // 创建Switch
                AppCompatCheckBox checkBox = new AppCompatCheckBox(requireContext());
                // 使文字仅显示在一行
                checkBox.setMaxLines(1);
                // 勾选添加, 取消勾选移除
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        importTimetable.add((Timetable) buttonView.getTag());
                    } else {
                        importTimetable.remove((Timetable) buttonView.getTag());
                    }
                });

                return new RecyclerView.ViewHolder(checkBox) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                AppCompatCheckBox checkBox = (AppCompatCheckBox) holder.itemView;
                Timetable timetable = getItem(position);

                // 设置Tag
                checkBox.setTag(timetable);
                // 设置标题
                checkBox.setText(timetable.timetableName);
                // 设置是否勾选
                checkBox.setChecked(importTimetable.contains(timetable));
            }
        };
        recyclerView.setAdapter(listAdapter);

        listAdapter.submitList(timetables);

        alertDialog.setView(recyclerView);

        alertDialog.show();

        // 导入按钮
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            alertDialog.dismiss();
            timetableViewModel.importTimetables(new HashSet<>(importTimetable));
            showTimetable();
        });
        // 取消按钮
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
            alertDialog.dismiss();
            showTimetable();
        });
        // 全选按钮
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
            importTimetable.addAll(timetables);
            listAdapter.notifyItemRangeChanged(0, listAdapter.getItemCount());
        });
    }

    /**
     * 用于在课程表中RecyclerView的项显示具体视图时用到的数据模型
     */
    public static class LessonModel {
        //午休ViewType
        public final static int NOON_VIEW_TYPE = -1;
        //晚休ViewType
        public final static int DUSK_VIEW_TYPE = -2;
        //课程占位符使用的ViewType
        public final static int LESSON_PLACEHOLDER_VIEW_TYPE = -5;

        public int viewType;

        public Lesson lesson;

        public WeekTimetableDateModel dateModel;

        public WeekTimetableTimeModel timeModel;

        public LessonModel(boolean noon) {
            if (noon) this.viewType = NOON_VIEW_TYPE;
            else this.viewType = DUSK_VIEW_TYPE;
        }

        public LessonModel() {
            viewType = LESSON_PLACEHOLDER_VIEW_TYPE;
        }

        //用于储存单元格的高度或午休晚休的索引
        public LessonModel(int aInt) {
            viewType = aInt;
        }

        public LessonModel(Lesson lesson, int viewType) {
            this.lesson = lesson;
            this.viewType = viewType;
        }

        public LessonModel(WeekTimetableDateModel dateModel) {
            this.dateModel = dateModel;
            this.viewType = DATE_VIEW_TYPE;
        }

        public LessonModel(WeekTimetableTimeModel timeModel) {
            this.timeModel = timeModel;
            this.viewType = TIME_VIEW_TYPE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LessonModel that = (LessonModel) o;
            return viewType == that.viewType && Objects.equals(lesson, that.lesson) && Objects.equals(dateModel, that.dateModel) && Objects.equals(timeModel, that.timeModel);
        }

        @Override
        public int hashCode() {
            return Objects.hash(viewType, lesson, dateModel, timeModel);
        }
    }

    /**
     * 用于在课程表中 日期显示的RecyclerView
     */
    private class WeekTimetableDateRecyclerView extends RecyclerView {
        private final ListAdapter<LessonModel, WeekTimetableViewHolder> listAdapter = new ListAdapter<>(new DefaultItemCallback<>()) {
            @NonNull
            @Override
            public WeekTimetableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ItemWeekTimetableDateBinding binding = DataBindingUtil.inflate(
                        LayoutInflater.from(getContext()), R.layout.item_week_timetable_date,
                        parent, false);
                binding.getRoot().setLayoutParams(new RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                return new WeekTimetableViewHolder(binding);
            }

            @Override
            public void onBindViewHolder(@NonNull WeekTimetableViewHolder holder, int position) {
                ((ItemWeekTimetableDateBinding) holder.binding).setDateModel(
                        getCurrentList().get(position).dateModel);
            }

            @Override
            public int getItemViewType(int position) {
                //具体课程使用list提供的数据模型进行设置
                return DATE_VIEW_TYPE;
            }

            @Override
            public int getItemCount() {
                return getCurrentList().size();
            }
        };

        public WeekTimetableDateRecyclerView(@NonNull Context context) {
            super(context);
            init(context);
        }

        private void init(@NonNull Context context) {
            setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            setNestedScrollingEnabled(false);

            setRecycledViewPool(recycledViewPool);

            setAdapter(listAdapter);

            //禁止滑动的layoutManager
            GridLayoutManager layoutManager = new GridLayoutManager(context,
                    timetable.getSpanCount(), RecyclerView.VERTICAL, false) {
                @Override
                public boolean canScrollHorizontally() {
                    return false;
                }

                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            //设置spanSize, 时间列占1, 课程列占2
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    //第一列的spanSize为1
                    if (position == 0) return 1;
                    //其余列为2
                    return 2;
                }
            });
            layoutManager.setRecycleChildrenOnDetach(true);
            setLayoutManager(layoutManager);

        }

        public void submitList(List<LessonModel> list) {
            listAdapter.submitList(list);
        }

    }

    /**
     * 各时段所使用的RecyclerView
     */
    private class WeekTimetablePeriodRecyclerView extends RecyclerView {

        private final Drawable mDivider;

        private final LayoutManager layoutManager;

        //记录当前时段的
        private final int section;

        private final ListAdapter listAdapter = new ListAdapter();

        public WeekTimetablePeriodRecyclerView(@NonNull Context context, int section) {
            super(context);

            this.section = section;
            layoutManager = new LayoutManager(getContext());

            final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.listDivider});
            mDivider = a.getDrawable(0);
            a.recycle();

            init();
        }

        private void init() {
            setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

            setRecycledViewPool(recycledViewPool);

            setAdapter(listAdapter);

            setLayoutManager(layoutManager);

            setItemAnimator(null);

            addItemDecoration(new ItemDecoration() {
                @Override
                public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
                    //指定宽度为2px
                    int size = 1;

                    for (int i = 0; i < parent.getChildCount(); i++) {
                        View child = parent.getChildAt(i);

                        //右边
                        mDivider.setBounds(child.getRight() - size, child.getTop(),
                                child.getRight(), child.getBottom());
                        mDivider.draw(c);

                        //左边
                        mDivider.setBounds(child.getLeft(), child.getTop(),
                                child.getLeft() + size, child.getBottom());
                        mDivider.draw(c);

                        //上边
                        mDivider.setBounds(child.getLeft(), child.getTop(),
                                child.getRight(), child.getTop() + size);
                        mDivider.draw(c);


                        //下边
                        mDivider.setBounds(child.getLeft(), child.getBottom() - size,
                                child.getRight(), child.getBottom());
                        mDivider.draw(c);
                    }
                }

            });
        }

        public void submitList(List<LessonModel> list) {
            listAdapter.submitList(list);
        }

        private class LayoutManager extends GridLayoutManager {

            public LayoutManager(Context context) {
                super(context, timetable.section[section], RecyclerView.HORIZONTAL, false);
                //设置spanSize, 时间列占1, 课程列占2
                setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        switch (listAdapter.getItemViewType(position)) {
                            //时间列的spanSize为1
                            case TIME_VIEW_TYPE:
                                return 1;
                            //空课程为1
                            case BLANK_LESSON_VIEW_TYPE:
                                return 1;
                            //具体课程为ViewType
                            default:
                                return listAdapter.getItemViewType(position);
                        }
                    }
                });

                setRecycleChildrenOnDetach(true);

                getSpanSizeLookup().setSpanGroupIndexCacheEnabled(true);
                getSpanSizeLookup().setSpanIndexCacheEnabled(true);
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        }

        private class ListAdapter extends androidx.recyclerview.widget.ListAdapter<LessonModel, WeekTimetableViewHolder> {

            private final int LOCATION_NULL = -1;

            //记录上一次点击空白课程的位置
            private int location = LOCATION_NULL;

            // 点击空白课程的事件
            private final OnClickListener blankClickListener = v -> {
                int tag = (int) v.getTag();
                if (tag != location) {
                    changePosition(tag);
                    return;
                }

                Lesson lesson = new Lesson();

                // 根据时段设置开始上课的时间
                switch (section) {
                    case MORNING:
                        lesson.startTime =
                                layoutManager.getSpanSizeLookup().getSpanIndex(tag, timetable.section[section]);
                        break;
                    case AFTERNOON:
                        lesson.startTime = timetable.section[MORNING] +
                                layoutManager.getSpanSizeLookup().getSpanIndex(tag, timetable.section[section]);
                        break;
                    case EVENING:
                        lesson.startTime = timetable.section[MORNING] + timetable.section[AFTERNOON] +
                                layoutManager.getSpanSizeLookup().getSpanIndex(tag, timetable.section[section]);
                        break;
                }

                // 默认上课时间为1间课
                lesson.periodTime = 1;

                // 由SpanSizeLookup获取周几
                /*
                此处getSpanGroupIndex, tag为当前的格子的位置索引, timetable.section[section]表示该时段一列有多少格子
                方法所返回的值为当前格子在课程表中的第几列, 包括第一列显示的时间列, 故此值有以下对应关系
                在显示周日的课表中, 且一周的开始日为周日, 则
                周日 - 周一 - 周二 --- 周六: 1 - 2 - 3 --- 7
                在显示周日的课表中, 且一周的开始日为周一, 则
                周一 - 周二 --- 周六 - 周日: 1 - 2 - 3 --- 7
                在不显示周末的课表中
                周一 - 周二 --- 周日: 1 - 2 --- 5
                 */
                lesson.weekDay = layoutManager.getSpanSizeLookup().getSpanGroupIndex(tag, timetable.section[section]);
                //不显示周末,或者一周开始为周一
                if (!timetable.showWeekend || !timetable.isSunday) {
                    if (lesson.weekDay == 7) {
                        lesson.weekDay = 0;
                    }
                }
                //一周起始为周日,且显示周末
                else {
                    lesson.weekDay--;
                }

                // 随机生成颜色
                lesson.color = new Random().nextInt(ColorResourcesUtils.getColorSum());

                // 默认选全部周
                for (int i = 0; i < timetable.weeks; i++) {
                    lesson.weeks.add(i);
                }

                // 赋予当前的课程表id
                lesson.timetableID = timetable.id;

                // 给ViewModel设置当前修改的课程
                timetableViewModel.setCurrentModifyLesson(lesson);
                // 进行跳转
                NavigationUtils.navigate(requireView(), R.id.action_weekTimetableFragment_to_editLessonFragment);

                changePosition(LOCATION_NULL);
            };

            // 点击课程的事件
            private final OnClickListener editLessonOnClickListener = v -> {
                LayoutLessonDetailBinding detailBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()),
                        R.layout.layout_lesson_detail, null, false);

                Lesson lesson = getCurrentList().get((Integer) v.getTag()).lesson;

                detailBinding.setLesson(lesson);

                AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                        .setView(detailBinding.getRoot())
                        .create();

                detailBinding.layoutLessonInformationEdit.setOnClickListener(view -> {
                    timetableViewModel.setCurrentModifyLesson(new Lesson(lesson));
                    alertDialog.dismiss();
                    NavigationUtils.navigate(requireView(), R.id.action_weekTimetableFragment_to_editLessonFragment);
                });

                // 设置背景色
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(lesson.getBackgroundColor()));

                alertDialog.show();
            };

            public ListAdapter() {
                super(new DefaultItemCallback<>());
            }

            @NonNull
            public WeekTimetableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                switch (viewType) {
                    //空课程的单元格
                    case BLANK_LESSON_VIEW_TYPE: {
                        View view = new View(getContext());
                        view.setLayoutParams(new RecyclerView.LayoutParams(2 * sheetWidth, sheetHeight));
                        return new WeekTimetableViewHolder(view);
                    }

                    //时间的单元格
                    case TIME_VIEW_TYPE: {
                        ItemWeekTimetableTimeBinding binding = DataBindingUtil.inflate(
                                LayoutInflater.from(getContext()), R.layout.item_week_timetable_time,
                                parent, false);
                        binding.getRoot().setLayoutParams(new RecyclerView.LayoutParams(sheetWidth, sheetHeight));
                        return new WeekTimetableViewHolder(binding);
                    }

                    //显示具体课程
                    default: {
                        ItemWeekTimetableLessonBinding binding = DataBindingUtil.inflate(
                                LayoutInflater.from(getContext()), R.layout.item_week_timetable_lesson,
                                parent, false);
                        binding.getRoot().getLayoutParams().height = sheetHeight * viewType;
                        binding.getRoot().getLayoutParams().width = 2 * sheetWidth;
                        return new WeekTimetableViewHolder(binding);
                    }
                }
            }

            @Override
            public void onBindViewHolder(@NonNull WeekTimetableViewHolder holder, int position) {
                int viewType = getItemViewType(position);
                switch (viewType) {
                    //对于空课程的View,使用Tag携带其位置信息
                    case BLANK_LESSON_VIEW_TYPE: {
                        holder.itemView.setTag(position);
                        holder.itemView.setOnClickListener(blankClickListener);
                        //绘制加号
                        if (position == location) holder.itemView.setBackground(new Drawable() {
                            //绘制add的icon
                            final Paint paint = new Paint();
                            final int margin = 5;

                            @Override
                            public void draw(@NonNull Canvas canvas) {
                                float height = getBounds().height();
                                float width = getBounds().width();

                                //抗锯齿
                                paint.setAntiAlias(true);
                                //绘制add图标的背景色
                                paint.setColor(getContext().
                                        getColor(R.color.timetable_icon_add_background_color));
                                canvas.drawRoundRect(margin, margin, width - margin,
                                        height - margin, 15, 15, paint);

                                //绘制add的加号(横竖的长度占组件的三分之一,宽度占三十分之一)
                                paint.setColor(getContext()
                                        .getColor(R.color.timetable_icon_add_color));
                                //画横
                                canvas.drawRoundRect(width / 2 - width / 6,
                                        height / 2 - height / 80,
                                        width / 2 + width / 6,
                                        height / 2 + height / 80,
                                        8, 8, paint);
                                //画竖
                                canvas.drawRoundRect(width / 2 - height / 80,
                                        height / 2 - width / 6,
                                        width / 2 + height / 80,
                                        height / 2 + width / 6,
                                        8, 8, paint);
                            }

                            @Override
                            public void setAlpha(int alpha) {
                            }

                            @Override
                            public void setColorFilter(@Nullable ColorFilter colorFilter) {
                            }

                            @Override
                            public int getOpacity() {
                                return PixelFormat.TRANSPARENT;
                            }
                        });
                        else holder.itemView.setBackground(null);
                    }
                    break;
                    //显示时间
                    case TIME_VIEW_TYPE:
                        ((ItemWeekTimetableTimeBinding) holder.binding).setWeekTimetableTimeModel(
                                getCurrentList().get(position).timeModel);
                        break;

                    //显示具体课程
                    default: {
                        ItemWeekTimetableLessonBinding binding = (ItemWeekTimetableLessonBinding) holder.binding;

                        holder.binding.getRoot().setTag(position);
                        binding.setLesson(getCurrentList().get(position).lesson);

                        if (viewType == 1) {
                            if (sheetHeight == ResourcesUtils.getDensityPx(LONGER)) {
                                binding.lessonName.setMaxLines(2);
                            } else {
                                binding.lessonName.setMaxLines(3);
                            }
                        } else {
                            binding.lessonName.setMaxLines(4);
                        }

                        // 如果课程长度大于等于3 则显示三行地点 两行授课教师, 否则为两行地点 一行授课教师
                        binding.lessonLocation.setMaxLines(viewType >= 3 ? 3 : 2);
                        // 如果课程长度大于等于3 则显示三行地点 两行授课教师, 否则为两行地点 一行授课教师
                        binding.lessonTeacher.setMaxLines(viewType >= 3 ? 2 : 1);
                        holder.binding.getRoot().setOnClickListener(editLessonOnClickListener);
                    }
                }
            }

            @Override
            public int getItemViewType(int position) {
                return getCurrentList().get(position).viewType;
            }

            @Override
            public int getItemCount() {
                return getCurrentList().size();
            }

            //更改上次点击的位置
            private void changePosition(int position) {
                int oldPosition = location;
                location = position;
                this.notifyItemChanged(oldPosition);
                if (position != LOCATION_NULL) {
                    this.notifyItemChanged(position);
                }
            }
        }
    }

    /**
     * ViewHolder
     */
    private static class WeekTimetableViewHolder extends RecyclerView.ViewHolder {

        public ViewDataBinding binding;

        public WeekTimetableViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public WeekTimetableViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
