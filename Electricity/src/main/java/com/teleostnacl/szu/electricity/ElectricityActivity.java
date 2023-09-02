package com.teleostnacl.szu.electricity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.teleostnacl.common.android.paging.adapter.ClickOncePagingDataAdapter;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.context.SPUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.electricity.databinding.ActivityElectricityBinding;
import com.teleostnacl.szu.electricity.databinding.ItemElectricityDateBinding;
import com.teleostnacl.szu.electricity.databinding.ItemElectricityDateLihuSecondBinding;
import com.teleostnacl.szu.electricity.databinding.LayoutElectricityBuyingDetailBinding;
import com.teleostnacl.szu.electricity.databinding.LayoutElectricityDateDetailBinding;
import com.teleostnacl.szu.electricity.databinding.LayoutElectricityLihuRemainBinding;
import com.teleostnacl.szu.electricity.model.ElectricityBuyingModel;
import com.teleostnacl.szu.electricity.model.ElectricityDateModel;
import com.teleostnacl.szu.electricity.model.LiHuRemainModel;
import com.teleostnacl.szu.electricity.viewmodel.ElectricityViewModel;
import com.teleostnacl.szu.libs.activity.BaseLoadingActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ElectricityActivity extends BaseLoadingActivity {

    private ElectricityViewModel electricityViewModel;

    private ActivityElectricityBinding binding;

    private static final String SP_FILE_ELECTRICITY = "electricity";
    private static final String SP_KEY_CLIENT = "client";
    private static final String SP_KEY_BUILDING_NAME = "buildingName";
    private static final String SP_KEY_BUILDING_ID = "buildingId";
    private static final String SP_KEY_ROOM_NAME = "roomName";
    private static final String SP_KEY_ROOM_ID = "roomId";
    private static final String SP_KEY_LOCAL_DATE = "localDate";

    // region 丽湖二期宿舍剩余电费相关的参数
    private static final String SP_KEY_LIHU_SECOND_REMAIN_BUYING = "li_hu_second_remain_buying";
    private static final String SP_KEY_LIHU_SECOND_REMAIN_SENDING = "remain_sending";
    private static final String SP_KEY_LIHU_SECOND_REMAIN = "remain";
    private static final String SP_KEY_LIHU_SECOND_DATE = "date";
    // endregion

    private SharedPreferences sharedPreferences;

    private final PagingDataAdapter<ElectricityDateModel, DataBindingVH<ViewDataBinding>> adapter = new ClickOncePagingDataAdapter<>(new DefaultItemCallback<>()) {
        private final int LIHU_VIEW_TYPE = -1;

        @NonNull
        @Override
        public DataBindingVH<ViewDataBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DataBindingVH<ViewDataBinding> viewHolder = new DataBindingVH<>(parent,
                    viewType == LIHU_VIEW_TYPE ? R.layout.item_electricity_date_lihu_second : R.layout.item_electricity_date);

            setOnClickListener(viewHolder.itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ViewDataBinding> holder, int position) {
            holder.itemView.setTag(getItem(position));

            if (holder.binding instanceof ItemElectricityDateBinding) {
                ((ItemElectricityDateBinding) holder.binding).setModel(getItem(position));
            } else if (holder.binding instanceof ItemElectricityDateLihuSecondBinding) {
                ((ItemElectricityDateLihuSecondBinding) holder.binding).setModel(getItem(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return electricityViewModel.electricityModel.isLiHuSecond() ? LIHU_VIEW_TYPE :
                    super.getItemViewType(position);
        }

        @Override
        public void onClick(@NonNull View v) {
            ElectricityDateModel dateModel = (ElectricityDateModel) v.getTag();

            if (electricityViewModel.electricityModel.isLiHuSecond() &&
                    (dateModel.buyingModelList == null || dateModel.buyingModelList.size() == 0)) {
                return;
            }

            ListAdapter<ElectricityBuyingModel, DataBindingVH<?>> listAdapter = electricityViewModel.electricityModel.isLiHuSecond() ?
                    new ListAdapter<>(new DefaultItemCallback<>()) {
                        @NonNull
                        @Override
                        public DataBindingVH<?> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            // 展示购电记录
                            return new DataBindingVH<>(parent, R.layout.layout_electricity_buying_detail);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull DataBindingVH<?> holder, int position) {
                            if (holder.binding instanceof LayoutElectricityBuyingDetailBinding) {
                                ((LayoutElectricityBuyingDetailBinding) holder.binding).setModel(getItem(position));
                            }
                        }
                    } :
                    new ListAdapter<>(new DefaultItemCallback<>()) {
                        @NonNull
                        @Override
                        public DataBindingVH<?> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            // 第一个显示详细信息
                            if (viewType == 0) {
                                LayoutElectricityDateDetailBinding binding = DataBindingUtil.inflate(
                                        LayoutInflater.from(ElectricityActivity.this), R.layout.layout_electricity_date_detail,
                                        parent, false);

                                binding.setModel(dateModel);

                                return new DataBindingVH<>(binding);
                            }

                            // 展示购电记录
                            return new DataBindingVH<>(parent, R.layout.layout_electricity_buying_detail);
                        }

                        @Override
                        public void onBindViewHolder(@NonNull DataBindingVH<?> holder, int position) {
                            if (holder.binding instanceof LayoutElectricityBuyingDetailBinding) {
                                ((LayoutElectricityBuyingDetailBinding) holder.binding).setModel(getItem(position - 1));
                            }
                        }

                        @Override
                        public int getItemCount() {
                            // 第一个为显示详细信息
                            return super.getItemCount() + 1;
                        }

                        @Override
                        public int getItemViewType(int position) {
                            return position;
                        }
                    };

            listAdapter.submitList(dateModel.buyingModelList);

            RecyclerView recyclerView = new RecyclerView(v.getContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
            recyclerView.setAdapter(listAdapter);

            new AlertDialog.Builder(ElectricityActivity.this)
                    .setTitle(getString(R.string.electricity_date_detail_title,
                            dateModel.getYear() + "/" + dateModel.getDate()))
                    .setView(recyclerView)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_electricity);

        electricityViewModel = new ViewModelProvider(this).get(ElectricityViewModel.class);

        showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color, R.string.item_electricity_loading);

        sharedPreferences = SPUtils.getSP(SP_FILE_ELECTRICITY);
        // 检查是否可连接至电费系统
        disposable.add(electricityViewModel.check().subscribe(aBoolean -> {
            // 获取已经查询过的电费信息(楼栋)
            getFromSharedPreferences();

            boolean hasHistory = electricityViewModel.electricityModel.checkValid();

            // 可连接至电费系统 则进行查询
            if (aBoolean) {
                initView();
                if (hasHistory) {
                    setToolbar();
                }
                // 当前未有历史查询信息, 则弹出Dialog进行设置
                else {
                    binding.getRoot().setVisibility(View.VISIBLE);
                    binding.getRoot().post(() -> {
                        hideLoadingView();
                        showSetRoomChooseSchoolDialog(false);
                    });
                }
            }
            // 不可连接至电费系统 检查当前是否已经有查询记录
            else if (hasHistory) {
                electricityViewModel.electricityModel.useLocal = true;
                initView();
                setToolbar();
                ToastUtils.makeToast(getString(R.string.electricity_not_in_school) + "\n" + getString(R.string.electricity_have_history));
            } else {
                ToastUtils.makeToast(getString(R.string.electricity_not_in_school) + "\n" + getString(R.string.electricity_no_history));
                finish();
            }

        }));
    }

    /**
     * 初始化视图
     */
    private void initView() {
        // 观察本地时间, 若有更新, 则同时更新进sharedPreference
        electricityViewModel.electricityModel.localDate.observe(this, integer ->
                SPUtils.putInt(sharedPreferences.edit(), SP_KEY_LOCAL_DATE, integer).apply());

        // 观察丽湖剩余电费模型 若有更新 则同时更新进sharedPreference
        electricityViewModel.electricityModel.lihuRemain.observe(this,
                liHuRemainModel -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    SPUtils.putString(editor, SP_KEY_LIHU_SECOND_REMAIN, liHuRemainModel.remain);
                    SPUtils.putString(editor, SP_KEY_LIHU_SECOND_REMAIN_SENDING, liHuRemainModel.remainSending);
                    SPUtils.putString(editor, SP_KEY_LIHU_SECOND_REMAIN_BUYING, liHuRemainModel.remainBuying);
                    SPUtils.putString(editor, SP_KEY_LIHU_SECOND_DATE, liHuRemainModel.date);
                    editor.apply();
                });

        binding.pagingRecyclerView.setAdapter(adapter);
        disposable.add(electricityViewModel.getElectricityFlowable().subscribe(electricityDateModelPagingData ->
                adapter.submitData(getLifecycle(), electricityDateModelPagingData)));

        // 刷新视图
        binding.pagingRecyclerView.getSwipeRefreshLayout().setOnRefreshListener(() -> {
            showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color, R.string.item_electricity_loading);
            disposable.add(electricityViewModel.check().subscribe(aBoolean -> {
                // 更新视图显示
                electricityViewModel.electricityModel.useLocal = !aBoolean;
                if (!aBoolean) {
                    ToastUtils.makeToast(getString(R.string.electricity_not_in_school) + "\n" + getString(R.string.electricity_have_history));
                }
                setToolbar();
                adapter.refresh();
                binding.getRoot().post(this::hideLoadingView);
            }));
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.set_room) {
                showSetRoomChooseSchoolDialog(true);
            } else if (item.getItemId() == R.id.lihu_detail) {
                LayoutElectricityLihuRemainBinding binding = DataBindingUtil.inflate(
                        LayoutInflater.from(ElectricityActivity.this),
                        R.layout.layout_electricity_lihu_remain, null, false);

                binding.setModel(electricityViewModel.electricityModel.lihuRemain.getValue());

                new AlertDialog.Builder(ElectricityActivity.this)
                        .setTitle(R.string.electricity_lihu_details)
                        .setView(binding.getRoot())
                        .show();
            }
            return true;
        });

        binding.getRoot().setVisibility(View.VISIBLE);
        binding.getRoot().post(this::hideLoadingView);
    }

    private void setToolbar() {
        binding.toolbar.setTitle(getString(R.string.item_electricity) + " - " +
                electricityViewModel.electricityModel.buildingName + " " +
                electricityViewModel.electricityModel.roomName);
        // 根据是否使用本地状态决定是否显示编辑
        binding.toolbar.getMenu().getItem(1).setVisible(!electricityViewModel.electricityModel.useLocal);
        // 根据是否是丽湖校区决定是否显示剩余电费详细信息的内容
        binding.toolbar.getMenu().getItem(0).setVisible(electricityViewModel.electricityModel.isLiHuSecond());
    }

    // region 设置查询房间号的视图

    /**
     * 展示选择校区的Dialog
     *
     * @param cancelable 是否可返回取消
     */
    private void showSetRoomChooseSchoolDialog(boolean cancelable) {
        // 选择校区的Spinner
        Spinner spinner = createSetRoomSpinner(electricityViewModel.electricityModel.clientMap.keySet());

        // 展示选择校区的Dialog
        AlertDialog alertDialog = createSetRoomDialog(cancelable,
                getString(R.string.electricity_set_room_choose_school_title), 0, null, spinner);

        // 设置回调
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    // 选择校区后获取楼栋信息
                    alertDialog.dismiss();
                    showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.electricity_set_room_choose_building_loading);
                    disposable.add(electricityViewModel.getBuilding((String) parent.getItemAtPosition(position)).subscribe(aBoolean -> {
                        hideLoadingView();
                        if (aBoolean) {
                            showSetRoomChooseBuildingDialog(cancelable);
                        } else {
                            showSetRoomChooseSchoolDialog(cancelable);
                        }
                    }));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        alertDialog.show();
    }

    /**
     * 展示选择楼栋的Dialog
     *
     * @param cancelable 是否可返回取消
     */
    private void showSetRoomChooseBuildingDialog(boolean cancelable) {
        // 选择楼栋的Spinner
        Spinner spinner = createSetRoomSpinner(electricityViewModel.electricityModel.buildingIdMap.keySet());

        // 选择楼栋的Dialog
        AlertDialog alertDialog = createSetRoomDialog(cancelable,
                getString(R.string.electricity_set_room_choose_building_title) + " - " + electricityViewModel.electricityModel.clientName,
                R.string.electricity_set_room_rechoose_school,
                () -> showSetRoomChooseSchoolDialog(cancelable), spinner);

        // 回调
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    // 选择楼栋之后 展示设置房间号的Dialog
                    alertDialog.dismiss();
                    electricityViewModel.electricityModel.setBuildingName((String) parent.getItemAtPosition(position));
                    showSetRoomInputRoom(cancelable);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        alertDialog.show();
    }

    /**
     * 展示输入房间号的Dialog
     *
     * @param cancelable 是否可返回取消
     */
    private void showSetRoomInputRoom(boolean cancelable) {
        // 输入房间号的EditText
        EditText editText = new EditText(this);
        editText.setSingleLine();
        // 只允许输入数字
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        // 默认值为当前的roomName
        if (electricityViewModel.electricityModel.roomName != 0) {
            editText.setText(String.valueOf(electricityViewModel.electricityModel.roomName));
        }

        AlertDialog alertDialog = createSetRoomDialog(cancelable,
                getString(R.string.electricity_set_room_input_room_title) + " - " + electricityViewModel.electricityModel.buildingName,
                0, null, editText);


        // 确认按钮
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(com.teleostnacl.common.android.R.string.yes), (dialog, which) -> {
            Editable s = editText.getText();
            if (s == null || TextUtils.isEmpty(s.toString())) {
                // 输入为空 重新输入
                ToastUtils.makeToast(R.string.electricity_set_room_input_room_error);
                showSetRoomInputRoom(cancelable);
            } else {
                // 请求服务器检查是否房间号输入正确
                alertDialog.dismiss();
                showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.electricity_set_room_check_room_loading);
                disposable.add(electricityViewModel.checkRoomId(NumberUtils.parseInt(s.toString(), 0)).subscribe(aBoolean -> {
                    if (aBoolean) {
                        // 检查正确 初始化显示
                        setToolbar();
                        // 清除数据库
                        electricityViewModel.deleteAll();
                        // 更新SharedPreferences记录
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        SPUtils.putString(editor, SP_KEY_CLIENT, electricityViewModel.electricityModel.client);
                        SPUtils.putString(editor, SP_KEY_BUILDING_NAME, electricityViewModel.electricityModel.buildingName);
                        SPUtils.putString(editor, SP_KEY_BUILDING_ID, electricityViewModel.electricityModel.buildingId);
                        SPUtils.putInt(editor, SP_KEY_ROOM_NAME, electricityViewModel.electricityModel.roomName);
                        SPUtils.putString(editor, SP_KEY_ROOM_ID, electricityViewModel.electricityModel.roomId);
                        editor.apply();
                        adapter.refresh();
                    } else {
                        // 错误 重新输入房间号
                        ToastUtils.makeToast(R.string.electricity_set_room_input_room_error);
                        showSetRoomInputRoom(cancelable);
                    }
                    hideLoadingView();
                }));
            }
        });

        // 重新选择楼栋按钮
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.electricity_set_room_rechoose_building),
                (dialog, which) -> showSetRoomChooseBuildingDialog(cancelable));

        alertDialog.show();
    }

    /**
     * 创建设置房间的的dialog的统一配置
     *
     * @param cancelable       是否可返回取消
     * @param title            标题
     * @param positiveString   取消按键的文字
     * @param positiveRunnable 取消按键的回调
     * @param view             展示的视图
     * @return AlertDialog
     */
    @NonNull
    private AlertDialog createSetRoomDialog(boolean cancelable, String title, int positiveString, Runnable positiveRunnable, @NonNull View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setNegativeButton(cancelable ? com.teleostnacl.common.android.R.string.cancel : R.string.electricity_set_room_exit, (dialog, which) -> {
                    if (!cancelable) {
                        finish();
                    } else {
                        // 取消 还原配置
                        getFromSharedPreferences();
                    }
                })
                .create();

        if (positiveString != 0 && positiveRunnable != null) {
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(positiveString),
                    (dialog, which) -> positiveRunnable.run());
        }

        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        int padding = ResourcesUtils.getDensityPx(12);
        alertDialog.setView(view, padding, padding, padding, padding);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(cancelable);

        return alertDialog;
    }

    /**
     * 创建选择Spinner
     *
     * @param set 数据源
     * @return Spinner
     */
    @NonNull
    private Spinner createSetRoomSpinner(Set<String> set) {
        Spinner spinner = new Spinner(this);
        List<String> list = new ArrayList<>(set);
        list.add(0, getString(R.string.electricity_set_room_choose));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        return spinner;
    }
    // endregion

    /**
     * 从SharedPreferences中获取数据
     */
    private void getFromSharedPreferences() {
        electricityViewModel.electricityModel.client =
                SPUtils.getString(sharedPreferences, SP_KEY_CLIENT, "");
        electricityViewModel.electricityModel.buildingName =
                SPUtils.getString(sharedPreferences, SP_KEY_BUILDING_NAME, "");
        electricityViewModel.electricityModel.buildingId =
                SPUtils.getString(sharedPreferences, SP_KEY_BUILDING_ID, "");
        electricityViewModel.electricityModel.roomName =
                SPUtils.getInt(sharedPreferences, SP_KEY_ROOM_NAME, 0);
        electricityViewModel.electricityModel.roomId =
                SPUtils.getString(sharedPreferences, SP_KEY_ROOM_ID, "");
        electricityViewModel.electricityModel.localDate.setValue(
                SPUtils.getInt(sharedPreferences, SP_KEY_LOCAL_DATE, 0));

        // 记录丽湖二期宿舍区的电费使用详细
        LiHuRemainModel liHuRemainModel = new LiHuRemainModel();
        liHuRemainModel.remain = SPUtils.getString(sharedPreferences, SP_KEY_LIHU_SECOND_REMAIN, "");
        liHuRemainModel.remainBuying = SPUtils.getString(sharedPreferences, SP_KEY_LIHU_SECOND_REMAIN_BUYING, "");
        liHuRemainModel.remainSending = SPUtils.getString(sharedPreferences, SP_KEY_LIHU_SECOND_REMAIN_SENDING, "");
        liHuRemainModel.date = SPUtils.getString(sharedPreferences, SP_KEY_LIHU_SECOND_DATE, "");
        electricityViewModel.electricityModel.lihuRemain.postValue(liHuRemainModel);
    }
}