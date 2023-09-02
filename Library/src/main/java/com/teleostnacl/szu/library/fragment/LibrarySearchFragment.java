package com.teleostnacl.szu.library.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.teleostnacl.common.android.paging.PagingRecyclerView;
import com.teleostnacl.common.android.paging.adapter.ClickOncePagingDataAdapter;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.fragment.BaseDisposableFragment;
import com.teleostnacl.common.android.model.KeyValueModel;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.recyclerview.ClickedOnceListAdapter;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.databinding.FragmentLibrarySearchBinding;
import com.teleostnacl.szu.library.databinding.ItemFragmentLibrarySearchResultBookBinding;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.search.SearchModel;
import com.teleostnacl.szu.library.viewmodel.LibraryBookDetailViewModel;
import com.teleostnacl.szu.library.viewmodel.LibrarySearchViewModel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import kotlin.Unit;

public class LibrarySearchFragment extends BaseDisposableFragment {
    // 需要查询的字符串信息
    public static final String ARG_QUERY = "2";

    private FragmentLibrarySearchBinding binding;

    private LibrarySearchViewModel librarySearchViewModel;

    // 搜索建议的Emitter
    private ObservableEmitter<String> suggestionEmitter;

    // 搜索范围的Emitter
    private ObservableEmitter<Object> searchFieldsEmitter;

    private SuggestionPopupWindow suggestionPopupWindow;

    private final ResultAdapter resultAdapter = new ResultAdapter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        librarySearchViewModel = new ViewModelProvider(requireActivity()).get(LibrarySearchViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library_search, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setSearchModel(librarySearchViewModel.searchModel);

        initView();

        // 搜索关键字为空,表示刚进入该页面, 尚未初始化
        if (TextUtils.isEmpty(librarySearchViewModel.searchModel.keyword)) {
            Bundle bundle = getArguments();
            String tmp = null;
            if (bundle != null) {
                tmp = bundle.getString(ARG_QUERY);
            }
            // 传递的待搜索的字符串不为空, 则表示需要直接搜索该字符串
            if (!TextUtils.isEmpty(tmp)) {
                binding.fragmentLibrarySearchToolbar.setTitle(tmp);
                // 更新SearchModel的Keyword
                librarySearchViewModel.searchModel.keyword = tmp;

                submit();
            } else {
                // 否则使搜索框获得焦点
                binding.getRoot().post(() -> binding.fragmentLibrarySearchSearchView.setIconified(false));
            }
        }
    }

    /**
     * 初始化视图
     */
    private void initView() {
        initActionBar();
        initTabLayout();
        initSearchSuggestion();
        initSearchResult();
        initDrawerLayout();
        initSearchView();
    }

    /**
     * ActionBar
     */
    private void initActionBar() {
        // 移除toolbar padding
        binding.fragmentLibrarySearchToolbar.setContentInsetStartWithNavigation(0);

        // 菜单键
        binding.fragmentLibrarySearchToolbar.setNavigationIcon(R.drawable.ic_fragment_search_menu);
        binding.fragmentLibrarySearchToolbar.setNavigationOnClickListener(v -> {
            // 搜索框处于关闭状态, 为menu键
            if (binding.fragmentLibrarySearchSearchView.isIconified()) {
                if (binding.fragmentLibrarySearchDrawerLayout.isOpen()) {
                    binding.fragmentLibrarySearchDrawerLayout.close();
                } else {
                    binding.fragmentLibrarySearchDrawerLayout.open();
                }
            }
            // 打开状态为返回键, 关闭menu
            else {
                // 搜索字符为空时, 则退出fragment
                if (librarySearchViewModel.searchModel.keyword == null) {
                    NavigationUtils.popBackStack(v);
                } else {
                    // 关闭搜索框
                    binding.fragmentLibrarySearchSearchView.setQuery("", false);
                    binding.fragmentLibrarySearchSearchView.setIconified(true);
                }
            }
        });

        // 关键字不为空, 则设置标题为关键字
        if (!TextUtils.isEmpty(librarySearchViewModel.searchModel.keyword)) {
            binding.fragmentLibrarySearchToolbar.setTitle(librarySearchViewModel.searchModel.keyword);
        }
    }

    /**
     * TabLayout
     */
    private void initTabLayout() {
        // 添加tab
        for (SearchModel.SearchModeModel searchMode : SearchModel.searchModeModels) {
            binding.fragmentLibrarySearchTabLayout.addTab(
                    binding.fragmentLibrarySearchTabLayout.newTab().setText(searchMode.modeName));
        }
        // 切换tab时的回调
        binding.fragmentLibrarySearchTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                librarySearchViewModel.searchModel.setSearchModeModel(
                        SearchModel.searchModeModels.get(tab.getPosition()));

                submit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 重新选择相同的tab时, 是recyclerview快速回到顶部
                binding.fragmentLibrarySearchResult.getRecyclerView().smoothScrollToPosition(0);
            }
        });
    }

    /**
     * 搜索框
     */
    private void initSearchView() {
        // 监听搜索框焦点改变事件
        binding.fragmentLibrarySearchSearchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            // 得到焦点时
            if (hasFocus) {
                // 显示提示字符串
                binding.fragmentLibrarySearchModeTips.setVisibility(View.VISIBLE);
            }
            // 失去焦点时
            else {
                // 隐藏提示字符串
                binding.fragmentLibrarySearchModeTips.setVisibility(View.GONE);
                // 隐藏搜索建议框
                suggestionPopupWindow.dismiss();
            }
        });

        // 监听搜索文字改变和提交搜索事件
        binding.fragmentLibrarySearchSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 更新SearchModel的Keyword
                librarySearchViewModel.searchModel.keyword = query;

                // 清除搜索框焦点
                binding.fragmentLibrarySearchSearchView.setQuery("", false);
                binding.fragmentLibrarySearchSearchView.setIconified(true);

                submit();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 更新搜索建议
                suggestionEmitter.onNext(newText);
                return true;
            }
        });

        // 展开搜索框的回调方法R.drawable.ic_fragment_search_menu
        binding.fragmentLibrarySearchSearchView.setOnSearchClickListener(v -> {
            // 调整SearchView布局方式为MATCH_PARENT, 使标题不显示
            ViewGroup.LayoutParams layoutParams = binding.fragmentLibrarySearchSearchView.getLayoutParams();
            layoutParams.width = MATCH_PARENT;
            binding.fragmentLibrarySearchSearchView.setLayoutParams(layoutParams);

            // 显示返回图标
            binding.fragmentLibrarySearchToolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            // 使侧滑栏关闭
            binding.fragmentLibrarySearchDrawerLayout.close();

            // 显示提示字符串
            binding.fragmentLibrarySearchModeTips.setVisibility(View.VISIBLE);
            // 设置默认搜索文字
            binding.fragmentLibrarySearchSearchView.setQuery(librarySearchViewModel.searchModel.keyword, false);
        });
        // 隐藏搜索框的回调方法
        binding.fragmentLibrarySearchSearchView.setOnCloseListener(() -> {
            // 搜索内容为空时, 不隐藏搜索框
            if (TextUtils.isEmpty(librarySearchViewModel.searchModel.keyword)) {
                return true;
            }

            // 设置显示标题为搜索的字符串
            binding.fragmentLibrarySearchToolbar.setTitle(librarySearchViewModel.searchModel.keyword);

            // 调整SearchView布局方式为WRAP_CONTENT, 使标题显示
            ViewGroup.LayoutParams layoutParams = binding.fragmentLibrarySearchSearchView.getLayoutParams();
            layoutParams.width = WRAP_CONTENT;
            binding.fragmentLibrarySearchSearchView.setLayoutParams(layoutParams);

            // 显示menu图标
            binding.fragmentLibrarySearchToolbar.setNavigationIcon(R.drawable.ic_fragment_search_menu);

            return false;
        });
    }

    /**
     * 搜索建议
     */
    private void initSearchSuggestion() {
        suggestionPopupWindow = new SuggestionPopupWindow();
        // 搜索建议
        disposable.add(Observable.create((ObservableOnSubscribe<String>) emitter -> suggestionEmitter = emitter)
                .sample(2, TimeUnit.SECONDS)
                .switchMap(string -> librarySearchViewModel.showSearchSuggestion(string))
                .subscribe(strings -> {
                    if (strings.size() == 0) {
                        suggestionPopupWindow.dismiss();
                    } else if (binding.fragmentLibrarySearchSearchView.hasFocus()) {
                        suggestionPopupWindow.show(strings);
                    }
                }));
    }

    /**
     * 搜索结果
     */
    private void initSearchResult() {
        // 数据加载时, 显示提示语, 加载成功后 隐藏提示语
        resultAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState loadState = combinedLoadStates.getRefresh();
            if (loadState instanceof LoadState.NotLoading) {
                // 数据获取成功后, 将提示语去除
                binding.fragmentLibrarySearchModeTips.setVisibility(View.GONE);
            } else if (loadState instanceof LoadState.Loading) {
                // 数据获取成功中, 显示提示语
                binding.fragmentLibrarySearchModeTips.setVisibility(View.VISIBLE);
            }
            return Unit.INSTANCE;
        });

        binding.fragmentLibrarySearchResult.setAdapter(resultAdapter);

        disposable.add(librarySearchViewModel.getSearchResultFlowable()
                .subscribe(bookModelPagingData -> resultAdapter.submitData(getLifecycle(), bookModelPagingData)));
    }

    /**
     * 侧滑栏
     */
    private void initDrawerLayout() {
        // 提交搜索范围
        disposable.add(Observable.create(emitter -> searchFieldsEmitter = emitter)
                .switchMap(o -> {
                    // 折叠所有组使其数据更新
                    for (int i = 0; i < 4; i++) {
                        binding.fragmentLibrarySearchNavigationViewExpandableListView.collapseGroup(i);
                    }
                    binding.fragmentLibrarySearchNavigationViewLoading.setVisibility(View.VISIBLE);
                    return librarySearchViewModel.getSearchFields();
                })
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        // 隐藏加载动画
                        binding.fragmentLibrarySearchNavigationViewLoading.setVisibility(View.GONE);
                    }
                }));

        // 仅显示借图书的switch状态监听
        binding.navigationViewOnlyShowCanLendSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            librarySearchViewModel.searchModel.onlyShownCanLend = isChecked;

            refreshResult();
        });

        // 不可侧滑打开
        binding.fragmentLibrarySearchDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // 监听状态, 动态调整是否可以侧滑 实现打开时可以侧滑关闭 不可侧滑打开
        binding.fragmentLibrarySearchDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                // 可侧滑关闭
                binding.fragmentLibrarySearchDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                // 不可侧滑打开
                binding.fragmentLibrarySearchDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        initSortWay();

        final int padding = ResourcesUtils.getDensityPx(8);

        // 类别
        binding.fragmentLibrarySearchNavigationViewExpandableListView.setAdapter(new BaseExpandableListAdapter() {
            //四个组(分类, 文献类别, 典藏地点, 出版日期)
            @Override
            public int getGroupCount() {
                return 4;
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                switch (groupPosition) {
                    case 0:
                        return librarySearchViewModel.searchModel.cfModels.size();
                    case 1:
                        return librarySearchViewModel.searchModel.documentTypeModels.size();
                    case 2:
                        return librarySearchViewModel.searchModel.deptModels.size();
                    case 3:
                        return librarySearchViewModel.searchModel.pyfModels.size();
                    default:
                        return 0;
                }
            }

            @NonNull
            @Override
            public Object getGroup(int groupPosition) {
                switch (groupPosition) {
                    case 0:
                        return getString(R.string.library_search_result_category);
                    case 1:
                        return getString(R.string.library_search_result_document_type);
                    case 2:
                        return getString(R.string.library_search_result_collection_site);
                    case 3:
                        return getString(R.string.library_search_result_publication_date);
                    default:
                        return "";
                }
            }

            @Nullable
            @Override
            public KeyValueModel getChild(int groupPosition, int childPosition) {
                switch (groupPosition) {
                    case 0:
                        return librarySearchViewModel.searchModel.cfModels.get(childPosition);
                    case 1:
                        return librarySearchViewModel.searchModel.documentTypeModels.get(childPosition);
                    case 2:
                        return librarySearchViewModel.searchModel.deptModels.get(childPosition);
                    case 3:
                        return librarySearchViewModel.searchModel.pyfModels.get(childPosition);
                    default:
                        return null;
                }
            }

            @Override
            public long getGroupId(int groupPosition) {
                return groupPosition;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return childPosition;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                TextView textView;
                if (convertView == null) {
                    textView = new TextView(requireContext());
                    textView.setPadding(
                            2 * padding, 2 * padding, 2 * padding, 2 * padding);
                    textView.setTextColor(Color.BLACK);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                    textView.setTextSize(18);

                } else {
                    textView = (TextView) convertView;
                }
                textView.setText(getGroup(groupPosition).toString());
                return textView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                     View convertView, ViewGroup parent) {
                TextView textView;
                if (convertView == null) {
                    textView = new TextView(requireContext());
                    textView.setPadding(4 * padding, (int) (1.5 * padding),
                            4 * padding, (int) (1.5 * padding));
                    textView.setClickable(true);
                    textView.setForeground(ResourcesUtils.getSelectableItemBackground());
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(15);
                    textView.setLines(1);
                    textView.setEllipsize(TextUtils.TruncateAt.END);

                    textView.setOnClickListener(v -> {
                        KeyValueModel object = (KeyValueModel) v.getTag();

                        if (object instanceof SearchModel.CfModel) {
                            librarySearchViewModel.searchModel.cf =
                                    textView.isSelected() ? null : (SearchModel.CfModel) object;
                        } else if (object instanceof SearchModel.DocumentTypeModel) {
                            librarySearchViewModel.searchModel.documentTypeModel =
                                    textView.isSelected() ? null : (SearchModel.DocumentTypeModel) object;
                        } else if (object instanceof SearchModel.DeptModel) {
                            librarySearchViewModel.searchModel.dept =
                                    textView.isSelected() ? null : (SearchModel.DeptModel) object;
                        } else if (object instanceof SearchModel.PyfModel) {
                            librarySearchViewModel.searchModel.pyf =
                                    textView.isSelected() ? null : (SearchModel.PyfModel) object;
                        }

                        // 关闭侧滑栏并显示加载动画
                        binding.fragmentLibrarySearchDrawerLayout.close();
                        binding.fragmentLibrarySearchNavigationViewLoading.setVisibility(View.VISIBLE);

                        submit();
                    });
                } else {
                    textView = (TextView) convertView;
                }
                KeyValueModel object = getChild(groupPosition, childPosition);

                if (object == null) return textView;

                textView.setTag(object);
                textView.setText(object.toString());

                // 被选中项
                if ((groupPosition == 0 && Objects.equals(object, librarySearchViewModel.searchModel.cf)) ||
                        (groupPosition == 1 && Objects.equals(object, librarySearchViewModel.searchModel.documentTypeModel)) ||
                        (groupPosition == 2 && Objects.equals(object, librarySearchViewModel.searchModel.dept)) ||
                        (groupPosition == 3 && Objects.equals(object, librarySearchViewModel.searchModel.pyf))) {
                    textView.setSelected(true);
                    textView.setBackground(new Drawable() {
                        final float margin = 2.5f * padding;
                        final Paint paint = new Paint();

                        @Override
                        public void draw(@NonNull Canvas canvas) {
                            paint.setStyle(Paint.Style.FILL_AND_STROKE);
                            paint.setColor(0xffc6e6e8);

                            int width = getBounds().width();
                            int height = getBounds().height();

                            float radius = height / 2f;
                            canvas.drawCircle(radius + margin, radius, radius, paint);
                            canvas.drawRect(radius + margin, 0, width - radius - margin, height, paint);
                            canvas.drawCircle(width - radius - margin, radius, radius, paint);
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
                } else {
                    textView.setBackground(null);
                }

                return textView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return true;
            }
        });

        // 调整箭头的位置
        binding.fragmentLibrarySearchNavigationViewExpandableListView.post(() -> {
            int width = binding.fragmentLibrarySearchNavigationViewExpandableListView.getWidth();
            binding.fragmentLibrarySearchNavigationViewExpandableListView.setIndicatorBoundsRelative(
                    width - 5 * padding, width - 2 * padding);
        });
    }

    /**
     * 初始化选择排序方式和排序方向的Spinner视图
     */
    private void initSortWay() {
        // 排序方式
        ArrayAdapter<KeyValueModel> sortWayAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, SearchModel.sortWayModels);
        sortWayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fragmentLibrarySearchNavigationViewSortWaySpinner.setAdapter(sortWayAdapter);
        // 默认选择
        binding.fragmentLibrarySearchNavigationViewSortWaySpinner.setSelection(
                SearchModel.sortWayModels.indexOf(librarySearchViewModel.searchModel.sf));
        binding.fragmentLibrarySearchNavigationViewSortWaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueModel sf = sortWayAdapter.getItem(position);

                // 选择不同的才进行刷新
                if (sf != librarySearchViewModel.searchModel.sf) {
                    // 选择之后替换排序方式
                    librarySearchViewModel.searchModel.sf = sf;

                    refreshResult();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //排序方向
        ArrayAdapter<KeyValueModel> sortWayObAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, SearchModel.sortOrientationModels);
        sortWayObAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fragmentLibrarySearchNavigationViewSortWayObSpinner.setAdapter(sortWayObAdapter);
        // 默认选择
        binding.fragmentLibrarySearchNavigationViewSortWayObSpinner.setSelection(
                SearchModel.sortOrientationModels.indexOf(librarySearchViewModel.searchModel.ob));
        binding.fragmentLibrarySearchNavigationViewSortWayObSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueModel ob = sortWayObAdapter.getItem(position);

                // 选择不同的才进行刷新
                if (ob != librarySearchViewModel.searchModel.ob) {
                    // 选择之后替换排序方式
                    librarySearchViewModel.searchModel.ob = ob;

                    refreshResult();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 提交搜索
     */
    private void submit() {
        searchFieldsEmitter.onNext(new Object());

        refreshResult();
    }

    /**
     * 刷新搜索结果
     */
    private void refreshResult() {
        resultAdapter.refresh();
        binding.fragmentLibrarySearchResult.getRecyclerView().smoothScrollToPosition(0);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (suggestionPopupWindow != null) {
            suggestionPopupWindow.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        librarySearchViewModel.searchModel.clear();
        submit();
    }

    /**
     * 显示搜索建议的PopupWindow
     */
    private class SuggestionPopupWindow extends PopupWindow {
        private final int PADDING_HORIZONTAL_ITEM = ResourcesUtils.getDensityPx(16);
        private final int PADDING_VERTICAL_ITEM = ResourcesUtils.getDensityPx(12);

        // 搜索建议使用的Adapter
        private final ListAdapter<String, RecyclerView.ViewHolder> suggestionAdapter =
                new ClickedOnceListAdapter<>(new DefaultItemCallback<>()) {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        TextView textView = new TextView(parent.getContext());
                        textView.setClickable(true);
                        textView.setBackground(ResourcesUtils.getSelectableItemBackground());
                        textView.setLayoutParams(new RecyclerView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT));
                        textView.setTextSize(16);
                        textView.setPadding(PADDING_HORIZONTAL_ITEM, PADDING_VERTICAL_ITEM,
                                PADDING_HORIZONTAL_ITEM, PADDING_VERTICAL_ITEM);
                        setOnClickListener(textView);
                        return new RecyclerView.ViewHolder(textView) {
                        };
                    }

                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        ((TextView) holder.itemView).setText(getItem(position));
                    }

                    @Override
                    public void onClick(@NonNull View v) {
                        binding.fragmentLibrarySearchSearchView.setQuery(((TextView) v).getText(), false);
                    }
                };

        public SuggestionPopupWindow() {
            RecyclerView recyclerView = new RecyclerView(requireContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(suggestionAdapter);
            recyclerView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            recyclerView.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
            recyclerView.setBackgroundColor(Color.WHITE);
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

            this.setContentView(recyclerView);
            setWidth(ResourcesUtils.getDensityPx(256));
            setHeight(ResourcesUtils.getDensityPx(200));
        }

        public void show(List<String> list) {
            suggestionAdapter.submitList(list);
            showAsDropDown(binding.fragmentLibrarySearchSearchView,
                    Gravity.getAbsoluteGravity(Gravity.START, View.LAYOUT_DIRECTION_LOCALE), 0, 0);
        }

        @Override
        public void dismiss() {
            super.dismiss();
            suggestionAdapter.submitList(null);
        }
    }

    /**
     * 展示搜索结果的Adapter
     */
    private class ResultAdapter extends ClickOncePagingDataAdapter<
            BookModel, DataBindingVH<ItemFragmentLibrarySearchResultBookBinding>>
            implements PagingRecyclerView.ICheckShowNone {

        public ResultAdapter() {
            super(new DefaultItemCallback<>());
        }

        @Override
        public boolean isShowNone() {
            // 关键字不为空时才显示数据为空的视图
            return librarySearchViewModel.searchModel.keyword != null;
        }

        @NonNull
        @Override
        public DataBindingVH<ItemFragmentLibrarySearchResultBookBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DataBindingVH<ItemFragmentLibrarySearchResultBookBinding> viewHolder =
                    new DataBindingVH<>(parent, R.layout.item_fragment_library_search_result_book);

            setOnClickListener(viewHolder.itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ItemFragmentLibrarySearchResultBookBinding> holder, int position) {
            BookModel bookModel = getItem(position);
            holder.binding.setBookModel(bookModel);
            holder.itemView.setTag(bookModel);
        }

        @Override
        public void onClick(@NonNull View v) {
            new ViewModelProvider(requireActivity()).get(LibraryBookDetailViewModel.class).setBook((BookModel) v.getTag());
            NavigationUtils.navigate(v, R.id.action_librarySearchFragment_to_bookDetailFragment);
        }
    }
}
