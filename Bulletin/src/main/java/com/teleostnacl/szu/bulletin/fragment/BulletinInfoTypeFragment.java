package com.teleostnacl.szu.bulletin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.teleostnacl.common.android.paging.PagingRecyclerView;
import com.teleostnacl.common.android.fragment.BaseDisposableFragment;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.szu.bulletin.databinding.ItemInfoTypeBulletinBinding;
import com.teleostnacl.szu.bulletin.model.Bulletin;
import com.teleostnacl.szu.bulletin.recyclerview.adapter.BulletinPagingDataAdapter;
import com.teleostnacl.szu.bulletin.viewmodel.BulletinViewModel;

/**
 * 展示各个类别的公文通列表
 */
public class BulletinInfoTypeFragment extends BaseDisposableFragment {
    private static final String ARG_MODEL = "1";

    public String model;

    private BulletinViewModel bulletinViewModel;

    private final PagingDataAdapter<Bulletin, DataBindingVH<ItemInfoTypeBulletinBinding>> pagingDataAdapter = new BulletinPagingDataAdapter();

    private BulletinInfoTypeFragment() {
    }

    @NonNull
    public static BulletinInfoTypeFragment getInstance(String model) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_MODEL, model);

        BulletinInfoTypeFragment bulletinInfoTypeFragment = new BulletinInfoTypeFragment();
        bulletinInfoTypeFragment.setArguments(bundle);

        return bulletinInfoTypeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = requireArguments().getString(ARG_MODEL);

        bulletinViewModel = new ViewModelProvider(requireActivity()).get(BulletinViewModel.class);
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

        disposable.add(bulletinViewModel.getInfoTypeBulletinsPagingData(model).subscribe(
                pagingData -> pagingDataAdapter.submitData(getLifecycle(), pagingData)));

        ((PagingRecyclerView) view).setAdapter(pagingDataAdapter).setSwipeRefreshEnabled(false);
        ((PagingRecyclerView) view).getRecyclerView().setLayoutManager(new LinearLayoutManager(requireContext()));
        ((PagingRecyclerView) view).getRecyclerView().setNestedScrollingEnabled(false);
    }
}
