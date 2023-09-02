package com.teleostnacl.szu.library.fragment.user;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.teleostnacl.common.android.download.DownloadBroadcastReceiver;
import com.teleostnacl.common.android.download.DownloadDefaultCallback;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.FragmentMyLibraryMainBinding;
import com.teleostnacl.szu.library.viewmodel.MyLibraryMainViewModel;
import com.teleostnacl.szu.libs.retrofit.SzuCookiesDatabase;

import java.util.Objects;

public class MyLibraryMainFragment extends BaseLoadingFragment {
    private FragmentMyLibraryMainBinding binding;

    private MyLibraryMainViewModel myLibraryMainViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myLibraryMainViewModel = new ViewModelProvider(requireActivity()).get(MyLibraryMainViewModel.class);

        showLoadingView(com.teleostnacl.common.android.R.color.white, R.string.my_library_loading);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_library_main, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        disposable.add(myLibraryMainViewModel.getUserInfo(false).subscribe(aBoolean -> {
            if (aBoolean) {
                initView();
            } else {
                NavigationUtils.popBackStack(requireView());
            }
        }));
    }

    private void initView() {
        initActionBar();

        binding.rootView.setVisibility(View.VISIBLE);
        binding.getRoot().post(this::hideLoadingView);
    }

    private void initActionBar() {
        // 显示返回键
        NavigationUtils.navPopBackForToolbar(binding.fragmentMyLibraryMainToolbar);

        binding.fragmentMyLibraryMainToolbar.inflateMenu(R.menu.menu_my_library_fragment);

        binding.fragmentMyLibraryMainToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.my_library_fragment_menu_help) {
                // 根据不同的位置显示不同的帮助内容
                switch (binding.fragmentMyLibraryMainTabLayout.getSelectedTabPosition()) {
                    case 3:
                        ToastUtils.makeToast(R.string.my_library_reservation_reserve_help);
                        break;
                    case 4:
                        ToastUtils.makeToast(R.string.my_library_reservation_borrow_advance_help);
                        break;
                    case 5:
                        ToastUtils.makeToast(R.string.my_library_collection_help);
                        break;
                }
            } else if (item.getItemId() == R.id.my_library_fragment_menu_download) {
                // 荔园读书印记下载
                new AlertDialog.Builder(requireContext())
                        .setMessage(R.string.my_library_borrow_history_download_sure)
                        .setPositiveButton(com.teleostnacl.common.android.R.string.yes,
                                (dialog, which) -> downloadBorrowHistory())
                        .setNegativeButton(com.teleostnacl.common.android.R.string.cancel, null)
                        .show();
            }

            return true;
        });

        initTabLayout();
    }

    private void initTabLayout() {
        // 设置标签值
        String[] tabs = requireContext().getResources().getStringArray(R.array.my_library_main_fragment_tabs);

        for (String tab : tabs) {
            binding.fragmentMyLibraryMainTabLayout.addTab(
                    binding.fragmentMyLibraryMainTabLayout.newTab().setText(tab));
        }

        requireView().post(() -> {
            // 对借书历史增加本数的观察
            myLibraryMainViewModel.myLibraryModel.historyBorrowBooks.observe(getViewLifecycleOwner(), integer ->
                    Objects.requireNonNull(binding.fragmentMyLibraryMainTabLayout.getTabAt(2)).setText(tabs[2] + "(" + integer + ")"));

            // 对当前借阅数量的观察
            myLibraryMainViewModel.currentBorrowBooks.observe(getViewLifecycleOwner(), s ->
                    Objects.requireNonNull(binding.fragmentMyLibraryMainTabLayout.getTabAt(1)).setText(tabs[1] + "(" + s + ")"));
        });

        // 初始化ViewPager2
        binding.fragmentMyLibraryMainViewPage2.setAdapter(new FragmentStateAdapter(requireActivity()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    // 个人信息
                    case 0: {
                        return new PersonalInformationFragment();
                    }
                    // 当前借阅
                    case 1: {
                        return new CurrentBorrowFragment();
                    }
                    // 借书历史
                    case 2: {
                        return new BorrowHistoryFragment();
                    }
                    // 预约
                    case 3: {
                        return ReservationFragment.getInstance(true);
                    }
                    // 预借
                    case 4: {
                        return ReservationFragment.getInstance(false);
                    }
                    // 收藏图书
                    case 5: {
                        return new CollectionBookFragment();
                    }
                    default:
                        throw new IndexOutOfBoundsException();
                }
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        // 去除边界水波纹效果
        binding.fragmentMyLibraryMainViewPage2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        // 与TabLayout进行绑定
        new TabLayoutMediator(binding.fragmentMyLibraryMainTabLayout, binding.fragmentMyLibraryMainViewPage2,
                (tab, position) -> tab.setText(tabs[position])).attach();

        // 设置不同页面对toolbar的影响
        binding.fragmentMyLibraryMainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 控制帮助图标是否显示
                binding.fragmentMyLibraryMainToolbar.getMenu().getItem(0).setVisible(
                        binding.fragmentMyLibraryMainTabLayout.getSelectedTabPosition() >= 3);

                // 控制下载图标是否显示
                binding.fragmentMyLibraryMainToolbar.getMenu().getItem(1).setVisible(
                        binding.fragmentMyLibraryMainTabLayout.getSelectedTabPosition() == 2);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // 默认隐藏帮助和下载图标
        binding.fragmentMyLibraryMainToolbar.getMenu().getItem(0).setVisible(false);
        binding.fragmentMyLibraryMainToolbar.getMenu().getItem(1).setVisible(false);
    }

    /**
     * 下载荔园读书印记
     */
    private void downloadBorrowHistory() {
        // 下载的文件名
        String fileName = myLibraryMainViewModel.myLibraryModel.userInfoModel.name +
                "(" + myLibraryMainViewModel.myLibraryModel.userInfoModel.no + ")荔园读书印记.pdf";

        // 荔园读书印记下载地址
        String url = "http://www.lib.szu.edu.cn/opac/user/downloadbookborrowedhistory.aspx";

        // 创建下载器
        DownloadManager downloadManager =
                (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 设置请求的参数
        request.addRequestHeader("Cookie",
                SzuCookiesDatabase.getInstance().getCookieJar().getCookiesString(url));
        request.addRequestHeader("Referer", "http://www.lib.szu.edu.cn/opac/user/userinfo.aspx");
        request.addRequestHeader("Connection", "keep-alive");

        // 开始下载
        ToastUtils.makeToast(com.teleostnacl.common.android.R.string.download_start, fileName);
        long id = downloadManager.enqueue(request);

        // 注册监听器
        DownloadBroadcastReceiver.addDownloadCallback(
                id, new DownloadDefaultCallback(fileName, downloadManager, id));
    }
}
