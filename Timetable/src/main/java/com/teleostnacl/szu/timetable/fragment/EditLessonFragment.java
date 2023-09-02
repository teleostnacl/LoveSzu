package com.teleostnacl.szu.timetable.fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.fragment.BaseLogFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.utils.TimeUtils;
import com.teleostnacl.szu.timetable.R;
import com.teleostnacl.szu.timetable.databinding.FragmentEditLessonBinding;
import com.teleostnacl.szu.timetable.model.Lesson;
import com.teleostnacl.szu.timetable.model.Timetable;
import com.teleostnacl.szu.timetable.viewmodel.TimetableViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EditLessonFragment extends BaseLogFragment {

    //定义记录周数的选择状态
    private final static int CAN_CHOOSE = 0;     //可选
    private final static int CHOSEN = 1;         //已选
    private final static int NOT_CHOOSE = 2;     //不可选

    //定义记录周数的选择是否有规律
    private final static int IS_ALL = 4;         //全选
    private final static int IS_EVEN = 3;        //双周
    private final static int IS_ODD = 2;         //单周
    private final static int IS_SECOND = 1;      //后半学期
    private final static int IS_FIRST = 0;       //前半学期

    private FragmentEditLessonBinding binding;
    private Lesson lesson;

    private TimetableViewModel timetableViewModel;

    private Timetable timetable;

    //记录上课时间
    private int[] time;
    //记录上课的星期
    private String dayStr;

    //记录周数是否可选的map
    private final Map<Integer, Integer> weekMap = new HashMap<>();

    private final WeekAdapter weekAdapter = new WeekAdapter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timetableViewModel = new ViewModelProvider(requireActivity()).get(TimetableViewModel.class);

        lesson = timetableViewModel.getCurrentModifyLesson();
        timetable = timetableViewModel.getTimetable();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_lesson, container, false);
        binding.setLesson(lesson);

        //保存按钮
        binding.save.setOnClickListener(v -> save());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (TextUtils.isEmpty(lesson.lessonName))
            binding.fragmentEditLessonToolbar.setTitle(R.string.edit_lesson_fragment_new_lesson);
        else {
            binding.fragmentEditLessonToolbar.setTitle(R.string.edit_lesson_fragment_edit_lesson);
            initDeleteButton();
        }

        dayStr = TimeUtils.getDaySum()[lesson.weekDay];
        time = new int[lesson.periodTime];
        for (int i = 0; i < lesson.periodTime; i++) {
            time[i] = lesson.startTime + i;
        }

        //设置显示的时间
        String text = dayStr + " " + (time[0] + 1) + "-" + (time[time.length - 1] + 1) + " >";
        binding.editLessonTime.setText(text);
        binding.editLessonTime.setOnClickListener(v -> initTimeChooseView());

        binding.fragmentEditLessonToolbarSubtitle.setText(timetable.timetableName);

        //初始化选择颜色的RecyclerView
        binding.editLessonColorRecyclerView.setHasFixedSize(true);
        binding.editLessonColorRecyclerView.setNestedScrollingEnabled(false);
        binding.editLessonColorRecyclerView.setLayoutManager(new GridLayoutManager(
                requireContext(), ColorResourcesUtils.getColorSum() / 2,
                LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.editLessonColorRecyclerView.setAdapter(new ColorAdapter());

        //初始化显示上课周数
        judgeWeekAvailable(false);
        binding.editLessonWeekPickerRecyclerView.setHasFixedSize(true);
        binding.editLessonWeekPickerRecyclerView.setNestedScrollingEnabled(false);
        binding.editLessonWeekPickerRecyclerView.setAdapter(weekAdapter);
        binding.editLessonWeekPickerRecyclerView.setLayoutManager(new GridLayoutManager(
                requireContext(), 6,
                LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.editLessonWeekPickerRecyclerView.setItemAnimator(null);

        initRadioGroup(binding.editLessonWeekSwitcherRadioGroup);
        judgeWeekSelected();

        // 初始化toolbar
        NavigationUtils.navPopBackForToolbar(binding.fragmentEditLessonToolbar);
    }

    /**
     * 设置上课时间
     */
    private void initTimeChooseView() {
        //两个数值选择器的AlertDialog
        LinearLayout view = (LinearLayout) LayoutInflater.from(requireContext()).inflate(
                R.layout.layout_lesson_time_settings, null);
        //获取numberPicker
        NumberPicker[] numberPicker = new NumberPicker[2];
        numberPicker[0] = view.getChildAt(0).findViewById(R.id.number_picker_with_unit_view);
        numberPicker[1] = view.getChildAt(1).findViewById(R.id.number_picker_with_unit_view);
        numberPicker[0].setMinValue(1);
        numberPicker[0].setMaxValue(Arrays.stream(timetable.section).sum());
        numberPicker[0].setValue(time[0] + 1);
        numberPicker[1].setMinValue(numberPicker[0].getValue());
        numberPicker[1].setMaxValue(Arrays.stream(timetable.section).sum());
        numberPicker[1].setValue(time[time.length - 1] + 1);
        //记录所选的最大值和最小值
        AtomicInteger minValue = new AtomicInteger(time[0]);
        AtomicInteger maxValue = new AtomicInteger(time[time.length - 1]);
        //设置最小值
        numberPicker[0].setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (numberPicker[1].getValue() < newVal + 1) {
                //如果在numberPicker1选择的值比numberPicker2大,则增大numberPicker2
                numberPicker[1].setValue(newVal);
                maxValue.set(newVal - 1);
            }
            //设置numberPicker2的最小值
            numberPicker[1].setMinValue(newVal);
            //设置最小值
            minValue.set(newVal - 1);
        });
        //设置最大值
        numberPicker[1].setOnValueChangedListener((picker, oldVal, newVal) -> maxValue.set(newVal - 1));

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_lesson_fragment_time)
                .setView(view)
                .setPositiveButton(com.teleostnacl.common.android.R.string.save, (dialog, which) -> {
                    //设置上课的时间,即节数
                    time = new int[maxValue.get() - minValue.get() + 1];
                    for (int i = 0; i < time.length; i++) time[i] = i + minValue.get();
                    //设置显示的时间
                    String text = dayStr + " " + (time[0] + 1) + "-" + (time[time.length - 1] + 1) + " >";
                    binding.editLessonTime.setText(text);
                    judgeWeekAvailable(true);
                    judgeWeekSelected();
                    weekAdapter.notifyItemRangeChanged(0, weekAdapter.getItemCount());
                })
                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                .show();
    }

    /**
     * 初始化规律记录选择的week的RadioGroup
     *
     * @param radioGroup binding.editLessonWeekSwitcherRadioGroup
     */
    private void initRadioGroup(RadioGroup radioGroup) {
        final String[] text = getResources().getStringArray(R.array.edit_lesson_switch_list);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //各RadioButton水平间的距离
        int margin = 4;
        layoutParams.setMargins(margin, 0, margin, 0);
        //居中显示
        layoutParams.gravity = Gravity.CENTER;
        for (int i = 0; i < 5; i++) {
            RadioButton radioButton = new RadioButton(requireContext());
            //设置索引
            radioButton.setId(i);
            //设置显示的文字
            radioButton.setText(text[i]);
            //文字大小
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            //设置layoutParams
            radioButton.setLayoutParams(layoutParams);
            //设置默认为不选
            radioButton.setChecked(false);
            //添加进radioGroup中
            radioGroup.addView(radioButton);
        }
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton button = group.findViewById(checkedId);
            if (button != null && button.isChecked()) {
                int chosenStart = 0, chosenEnd = weekMap.size(), chosenFoot = 1;
                int canChooseStart = 0, canChooseEnd = weekMap.size(), canChooseFoot = 1;
                switch (checkedId) {
                    //前半学期
                    case IS_FIRST:
                        chosenEnd = canChooseStart = weekMap.size() / 2;
                        break;
                    //后半学期
                    case IS_SECOND:
                        chosenStart = canChooseEnd = weekMap.size() / 2;
                        break;
                    //单周
                    case IS_ODD:
                        chosenFoot = canChooseFoot = 2;
                        canChooseStart = 1;
                        break;
                    //双周
                    case IS_EVEN:
                        chosenStart = 1;
                        chosenFoot = canChooseFoot = 2;
                        break;
                    //全选
                    case IS_ALL:
                        canChooseEnd = 0;
                        break;
                }

                //修改选择状态为被选择
                for (int i = chosenStart; i < chosenEnd; i += chosenFoot) {
                    // noinspection ConstantConditions
                    if (weekMap.get(i) != NOT_CHOOSE) weekMap.put(i, CHOSEN);
                    else radioGroup.clearCheck();
                }
                //修改选择状态为可选
                for (int i = canChooseStart; i < canChooseEnd; i += canChooseFoot) {
                    // noinspection ConstantConditions
                    if (weekMap.get(i) != NOT_CHOOSE) weekMap.put(i, CAN_CHOOSE);
                }
                weekAdapter.notifyItemRangeChanged(
                        0, weekAdapter.getItemCount());
            }
        });
    }

    /**
     * 判断周数是否可选
     *
     * @param isChange 是否为改变课程节数时
     */
    private void judgeWeekAvailable(boolean isChange) {
        //如果是改变课程节数,则清空map记录不可选的周数
        if (isChange) {
            for (int i = 0; i < weekMap.size(); i++)
                // noinspection ConstantConditions
                if (weekMap.containsKey(i) && weekMap.get(i) == NOT_CHOOSE)
                    weekMap.put(i, CAN_CHOOSE);
        }

        //判断是否可选
        for (int i = 0; i < timetable.weeks; i++) {
            // 获取记录当前周的list
            List<Lesson> lessons = timetable.weekMapLesson.get(i);
            if (lessons == null) {
                continue;
            }

            for (int pos : time) {
                //如果该处未被判断为不可选状态
                // noinspection ConstantConditions
                if (!weekMap.containsKey(i) || weekMap.get(i) != NOT_CHOOSE) {
                    //如果传递过来的是新建课程
                    if (lesson.id == 0) {
                        if (lessons.get(timetable.getPositionInList(lesson.weekDay, pos)) == null) {
                            //该时间为空,将该周设为1,即默认为选中
                            weekMap.put(i, CHOSEN);
                        } else {
                            //设为2,为不可选
                            weekMap.put(i, NOT_CHOOSE);
                        }
                    }
                    //传递过来的课程为非空,则需判断是否为同一课程
                    else {
                        Lesson oldLesson = lessons.get(timetable.getPositionInList(lesson.weekDay, pos));

                        //空课程不影响判断结果
                        if (oldLesson == null && !weekMap.containsKey(i)) {
                            weekMap.put(i, CAN_CHOOSE);
                        } else if (oldLesson != null && lesson.id == oldLesson.id) {
                            // 相同课程
                            // noinspection ConstantConditions
                            if (!weekMap.containsKey(i) || weekMap.get(i) != NOT_CHOOSE)
                                // 其它周该时间段的课程为空,且该时段还未被标记为可选,
                                // 判断课程id是否相同,相同则为同一课程,设为选中状态
                                weekMap.put(i, CHOSEN);
                        } else if (oldLesson != null)
                            //非相同课程,设为不可选中状态
                            weekMap.put(i, NOT_CHOOSE);
                    }
                }
            }
        }
    }

    /**
     * 判断周数是否有规律
     */
    private void judgeWeekSelected() {
        //前半学期,后半学期,单数,双数,全部
        boolean isFirst = true, isSecond = true, isEven = true, isOdd = true, isAll = true;
        for (int i = 0; i < weekMap.size(); i++) {
            //如果该项未被选中
            //noinspection ConstantConditions
            if (!(weekMap.get(i) == CHOSEN)) {
                //根据序号相对应的标记为false
                isAll = false;
                if (i % 2 == 0) {
                    isOdd = false;
                } else {
                    isEven = false;
                }
                if (i < weekMap.size() / 2) {
                    isFirst = false;
                } else {
                    isSecond = false;
                }
            } else {
                if (i < weekMap.size() / 2) {
                    isSecond = false;
                } else {
                    isFirst = false;
                }
                if (i % 2 == 0) {
                    isEven = false;
                } else {
                    isOdd = false;
                }
            }
        }
        //去掉之前所选项
        binding.editLessonWeekSwitcherRadioGroup.clearCheck();
        //根据判断结果进行选择绘制
        if (isAll) binding.editLessonWeekSwitcherRadioGroup.check(IS_ALL);
        else if (isOdd) binding.editLessonWeekSwitcherRadioGroup.check(IS_ODD);
        else if (isEven) binding.editLessonWeekSwitcherRadioGroup.check(IS_EVEN);
        else if (isSecond) binding.editLessonWeekSwitcherRadioGroup.check(IS_SECOND);
        else if (isFirst) binding.editLessonWeekSwitcherRadioGroup.check(IS_FIRST);
            //都不是,清除选项
        else weekAdapter.notifyItemRangeChanged(0, weekAdapter.getItemCount());
    }

    /**
     * 初始化删除课程按钮
     */
    private void initDeleteButton() {
        //使其可见
        binding.editLessonDeleteLessonButton.setVisibility(View.VISIBLE);

        //设置监听
        binding.editLessonDeleteLessonButton.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setMessage(R.string.edit_lesson_fragment_delete_lesson_sure)
                        .setPositiveButton(com.teleostnacl.common.android.R.string.yes, (dialog, which) -> {
                            //从数据库删除该课程
                            timetableViewModel.deleteLesson(lesson);
                            NavigationUtils.popBackStack(binding.editLessonDeleteLessonButton);
                        })
                        .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                        .show());
    }

    /**
     * 保存
     */
    private void save() {
        lesson.startTime = time[0];
        lesson.periodTime = time.length;

        List<Integer> weekLists = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : weekMap.entrySet()) {
            if (entry.getValue() == CHOSEN) weekLists.add(entry.getKey());
        }

        lesson.weeks.clear();
        lesson.weeks.addAll(weekLists);

        if (lesson.weeks.size() == 0) {
            Toast.makeText(requireContext(), R.string.edit_lesson_need_weeks, Toast.LENGTH_SHORT).show();
            return;
        }

        if (lesson.lessonName == null || lesson.lessonName.equals("")) {
            Toast.makeText(requireContext(), R.string.edit_lesson_need_name, Toast.LENGTH_SHORT).show();
            return;
        }

        timetableViewModel.saveLesson(lesson);
        NavigationUtils.popBackStack(requireView());
    }

    /**
     * 用于显示颜色的Adapter
     */
    private class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {

        private final int[] color = ColorResourcesUtils.getLightColors();
        private final int selectedColor = ColorResourcesUtils.getColor(R.color.edit_lesson_item_selected);

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ColorAdapter.ViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_edit_lesson_color, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.init(position);
        }

        @Override
        public int getItemCount() {
            return ColorResourcesUtils.getColorSum();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            public void init(int position) {
                //记录颜色的Drawable
                ImageView imageView = itemView.findViewById(R.id.edit_lesson_color_image_view);
                //如果记录被选择的颜色为该item
                if (position == lesson.color) {
                    imageView.setImageDrawable(createDrawable(color[position], true));
                } else imageView.setImageDrawable(createDrawable(color[position], false));
                imageView.setOnClickListener(v -> {
                    int lastColor = lesson.color;

                    lesson.color = position;

                    ColorAdapter.this.notifyItemChanged(lastColor);
                    ColorAdapter.this.notifyItemChanged(lesson.color);
                });
            }

            public Drawable createDrawable(int color, boolean isSelected) {
                return new Drawable() {

                    final Paint paint = new Paint();

                    @Override
                    public void draw(@NonNull Canvas canvas) {
                        float width = getBounds().width();
                        paint.setAntiAlias(true);
                        //绘制选择的框框
                        if (isSelected) {
                            paint.setColor(selectedColor);
                            canvas.drawCircle(width / 2, width / 2, width / 2, paint);
                        }
                        //绘制主体颜色
                        paint.setColor(color);
                        canvas.drawCircle(width / 2, width / 2, width / 2 - 5, paint);
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
                };
            }
        }
    }

    /**
     * 用于显示上课周数的Adapter
     */
    private class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(requireContext());
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = ResourcesUtils.dp_int_4;
            layoutParams.setMargins(margin, margin, margin, margin);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);
            int padding = 2 * margin;
            textView.setPadding(padding, padding, padding, padding);
            textView.setTextSize(16);

            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.init(position);
        }

        @Override
        public int getItemCount() {
            return weekMap.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            @SuppressWarnings("ALL")
            public void init(int position) {
                ((TextView) itemView).setText(String.valueOf(position + 1));
                if (weekMap.get(position) != null)
                    switch (weekMap.get(position)) {
                        case CAN_CHOOSE: {
                            itemView.setBackground(AppCompatResources.getDrawable(requireContext(),
                                    R.drawable.drawable_shape_edit_lesson_week_background));
                            ((TextView) itemView).setTextColor(
                                    getResources().getColor(R.color.edit_lesson_item_text, null));
                        }
                        break;
                        case CHOSEN: {
                            itemView.setBackground(AppCompatResources.getDrawable(requireContext(),
                                    R.drawable.drawable_shape_edit_lesson_week_selected_background));
                            ((TextView) itemView).setTextColor(Color.WHITE);
                        }
                        break;
                        case NOT_CHOOSE: {
                            itemView.setBackground(AppCompatResources.getDrawable(requireContext(),
                                    R.drawable.drawable_shape_edit_lesson_week_not_selected_background));
                            ((TextView) itemView).setTextColor(
                                    getResources().getColor(R.color.edit_lesson_item_text_not_selected, null));
                        }
                        break;
                    }

                itemView.setOnClickListener(v -> {
                    //对选择的周数进行更改,即取反
                    if (weekMap.get(position) != NOT_CHOOSE) {
                        if (weekMap.get(position) == CAN_CHOOSE) weekMap.put(position, CHOSEN);
                        else weekMap.put(position, CAN_CHOOSE);
                    }
                    WeekAdapter.this.notifyItemChanged(position);
                    //判断所选周数
                    judgeWeekSelected();
                });
            }
        }
    }
}
