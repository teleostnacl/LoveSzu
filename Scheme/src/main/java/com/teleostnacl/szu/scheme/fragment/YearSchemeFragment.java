package com.teleostnacl.szu.scheme.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.teleostnacl.common.android.paging.PagingRecyclerView;
import com.teleostnacl.common.android.paging.adapter.ClickOncePagingDataAdapter;
import com.teleostnacl.common.android.fragment.BaseDisposableFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.scheme.R;
import com.teleostnacl.szu.scheme.databinding.ItemYearSchemeBinding;
import com.teleostnacl.szu.scheme.model.SchemeDetailModel;
import com.teleostnacl.szu.scheme.viewmodel.SchemeViewModel;

/**
 * 每年的培养方案展示
 */
public class YearSchemeFragment extends BaseDisposableFragment {
    private static final String ARG_YEAR_INDEX = "1";

    private int yearIndex;

    private SchemeViewModel schemeViewModel;

    private final YearSchemeAdapter yearSchemeAdapter = new YearSchemeAdapter();

    @NonNull
    public static YearSchemeFragment getInstance(int yearIndex) {
        YearSchemeFragment fragment = new YearSchemeFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(ARG_YEAR_INDEX, yearIndex);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        schemeViewModel = new ViewModelProvider(requireActivity()).get(SchemeViewModel.class);

        yearIndex = requireArguments().getInt(ARG_YEAR_INDEX);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return new PagingRecyclerView(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PagingRecyclerView pagingRecyclerView = (PagingRecyclerView) view;

        pagingRecyclerView.setAdapter(yearSchemeAdapter);

        disposable.add(schemeViewModel.getYearSchemeModel(yearIndex).subscribe(yearSchemeModelPagingData ->
                yearSchemeAdapter.submitData(getLifecycle(), yearSchemeModelPagingData)));
    }

    private class YearSchemeAdapter extends ClickOncePagingDataAdapter<SchemeDetailModel, DataBindingVH<ItemYearSchemeBinding>> {

        public YearSchemeAdapter() {
            super(new DefaultItemCallback<>());
        }

        @Override
        public void onClick(@NonNull View v) {
            schemeViewModel.setSchemeDetailModel((SchemeDetailModel) v.getTag());
            NavigationUtils.navigate(v, R.id.action_schemeMainFragment_to_schemeDetailMainFragment);
        }

        @NonNull
        @Override
        public DataBindingVH<ItemYearSchemeBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DataBindingVH<ItemYearSchemeBinding> vh = new DataBindingVH<>(parent, R.layout.item_year_scheme);

            setOnClickListener(vh.itemView);

            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull DataBindingVH<ItemYearSchemeBinding> holder, int position) {
            SchemeDetailModel schemeDetailModel = getItem(position);

            holder.itemView.setTag(schemeDetailModel);
            holder.binding.setModel(schemeDetailModel);
        }
    }
}
