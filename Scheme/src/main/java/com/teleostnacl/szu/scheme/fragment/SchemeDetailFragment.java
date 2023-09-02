package com.teleostnacl.szu.scheme.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.view.recyclerview.ClickedOnceListAdapter;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.scheme.R;
import com.teleostnacl.szu.scheme.databinding.ItemSchemeDetailLessonBinding;
import com.teleostnacl.szu.scheme.model.SchemeDetailLesson;
import com.teleostnacl.szu.scheme.model.SchemeDetailModel;
import com.teleostnacl.szu.scheme.viewmodel.SchemeViewModel;

/**
 * 培养方案详细页展示各课程的页面
 */
public class SchemeDetailFragment extends BaseLoadingFragment {

    private static final String ARG_MODULE_INDEX = "1";
    private static final String ARG_GROUP_INDEX = "2";

    private SchemeViewModel schemeViewModel;

    private SchemeDetailModel mSchemeDetailModel;

    private final SchemeDetailAdapter schemeDetailAdapter = new SchemeDetailAdapter();


    // 记录该Fragment是第几个课程模块, 第几个课程组
    private int mModuleIndex;
    private int mGroupIndex;

    private SchemeDetailFragment() {
    }

    @NonNull
    public static SchemeDetailFragment getInstance(int moduleIndex, int groupIndex) {
        SchemeDetailFragment fragment = new SchemeDetailFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(ARG_MODULE_INDEX, moduleIndex);
        bundle.putInt(ARG_GROUP_INDEX, groupIndex);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        schemeViewModel = new ViewModelProvider(requireActivity()).get(SchemeViewModel.class);

        mSchemeDetailModel = schemeViewModel.getSchemeDetailModel();

        mModuleIndex = requireArguments().getInt(ARG_MODULE_INDEX);
        mGroupIndex = requireArguments().getInt(ARG_GROUP_INDEX);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return new RecyclerView(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView((RecyclerView) view);
    }

    private void initView(@NonNull RecyclerView view) {
        view.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.setAdapter(schemeDetailAdapter);

        schemeDetailAdapter.submitList(mSchemeDetailModel.schemeDetailModule.get(mModuleIndex)
                .schemeDetailGroups.get(mGroupIndex).schemeDetailLessons);
    }

    /**
     * 展示方案细节的Adapter
     */
    private static class SchemeDetailAdapter extends ClickedOnceListAdapter<SchemeDetailLesson, DataBindingVH<ItemSchemeDetailLessonBinding>> {

        public SchemeDetailAdapter() {
            super(new DefaultItemCallback<>());
        }

        @Override
        public void onClick(@NonNull View v) {

        }

        @NonNull
        @Override
        public DataBindingVH<ItemSchemeDetailLessonBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DataBindingVH<ItemSchemeDetailLessonBinding> vh = new DataBindingVH<>(parent, R.layout.item_scheme_detail_lesson);

            setOnClickListener(vh.itemView);

            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ItemSchemeDetailLessonBinding> holder, int position) {
            SchemeDetailLesson lesson = getItem(position);

            holder.itemView.setTag(lesson);

            holder.binding.setModel(lesson);
        }
    }
}
