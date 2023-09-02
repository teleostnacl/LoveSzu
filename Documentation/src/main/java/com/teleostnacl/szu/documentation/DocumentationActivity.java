package com.teleostnacl.szu.documentation;

import static com.teleostnacl.szu.documentation.viewmodel.DocumentationViewModel.CANCEL;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.download.DownloadBroadcastReceiver;
import com.teleostnacl.common.android.download.DownloadDefaultCallback;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.szu.documentation.databinding.ActivityDocumentationBinding;
import com.teleostnacl.szu.documentation.model.DownloadFileModel;
import com.teleostnacl.szu.documentation.model.ItemModel;
import com.teleostnacl.szu.documentation.viewmodel.DocumentationViewModel;
import com.teleostnacl.szu.libs.activity.BaseLoadingActivity;
import com.teleostnacl.szu.libs.model.NeumorphCardViewTextWithIconModel;
import com.teleostnacl.szu.libs.view.recyclerview.adapter.NeumorphCardViewTextViewIconListAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subjects.SingleSubject;

public class DocumentationActivity extends BaseLoadingActivity {

    private ActivityDocumentationBinding binding;

    private DocumentationViewModel documentationViewModel;

    private ObservableEmitter<ItemModel> observableEmitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_documentation);

        documentationViewModel = new ViewModelProvider(this).get(DocumentationViewModel.class);

        showLoadingView(com.teleostnacl.szu.libs.R.color.neumorphism_main_background_color, R.string.documentation_loading);
        disposable.add(documentationViewModel.init().subscribe(aBoolean -> {
            if (aBoolean) {
                initView();
            } else {
                finish();
            }
        }));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
        // 设置标题为带学号
        binding.toolbar.setTitle(getString(R.string.item_documentation) + " - " + documentationViewModel.getDocumentationModel().userId);

        initObservable();

        TypedArray typedArray = getResources().obtainTypedArray(R.array.drawable_icon);
        Drawable[] drawableIcons = new Drawable[typedArray.length()];
        for (int i = 0; i < drawableIcons.length; i++) {
            drawableIcons[i] = typedArray.getDrawable(i);
        }
        typedArray.recycle();

        List<NeumorphCardViewTextWithIconModel> modelList = new ArrayList<>();

        List<ItemModel> itemModels = documentationViewModel.getDocumentationModel().itemModels;
        for (int i = 0; i < itemModels.size(); i++) {
            final int finalI = i;
            modelList.add(new NeumorphCardViewTextWithIconModel(
                    itemModels.get(finalI).name, drawableIcons[finalI % drawableIcons.length],
                    () -> observableEmitter.onNext(itemModels.get(finalI))));
        }

        NeumorphCardViewTextViewIconListAdapter listAdapter;
        binding.recyclerView.setAdapter(listAdapter = new NeumorphCardViewTextViewIconListAdapter());
        listAdapter.submitList(modelList);

        binding.getRoot().setVisibility(View.VISIBLE);
        binding.getRoot().post(this::hideLoadingView);
    }

    @Override
    public void onBackPressed() {
        // 处于正在获取证明文件下载连接的动画 返回取消
        if (observableEmitter != null && loadingPopupWindow != null && loadingPopupWindow.isShowing()) {
            observableEmitter.onNext(CANCEL);
            hideLoadingView();
            return;
        }
        super.onBackPressed();
    }

    /**
     * 初始化单击事件观察者
     */
    private void initObservable() {
        disposable.add(Observable.create((ObservableOnSubscribe<ItemModel>) emitter -> observableEmitter = emitter)
                .switchMap(model -> {
                    if (CANCEL == model) {
                        return Observable.empty();
                    } else {
                        // 是否下载历史生成的证明文件
                        SingleSubject<Boolean> historySubject = SingleSubject.create();

                        // 无历史生成的文件 使用Dialog提示生成并下载
                        new AlertDialog.Builder(DocumentationActivity.this)
                                .setTitle(getString(R.string.documentation_download_title, model.name))
                                .setMessage(R.string.documentation_create_tips)
                                // 生成并下载证明文件
                                .setPositiveButton(R.string.documentation_download_auth,
                                        (dialog, which) -> historySubject.onSuccess(false))
                                // 不下载文件
                                .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                                .show();

                        return historySubject.flatMap((Function<Boolean, SingleSource<DownloadFileModel>>) aBoolean -> {
                            showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.documentation_creating);
                            return documentationViewModel.getDownloadLink(model);
                        }).toObservable();
                    }
                })
                .subscribe(downloadFileModel -> {
                    hideLoadingView();

                    // 请求成功才下载
                    if (downloadFileModel.success) {
                        // 创建下载器
                        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadFileModel.getUrl()))
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadFileModel.fileName)
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                        // 开始下载
                        ToastUtils.makeToast(com.teleostnacl.common.android.R.string.download_start, downloadFileModel.fileName);
                        long id = downloadManager.enqueue(request);

                        // 注册监听器
                        DownloadBroadcastReceiver.addDownloadCallback(id, new DownloadDefaultCallback(downloadFileModel.fileName, downloadManager, id));
                    }
                }));
    }
}