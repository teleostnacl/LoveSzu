package com.teleostnacl.szu.bulletin.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.fragment.BaseLoadingFragment;
import com.teleostnacl.common.android.utils.NavigationUtils;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.bulletin.R;
import com.teleostnacl.szu.bulletin.databinding.FragmentBulletinDetailBinding;
import com.teleostnacl.szu.bulletin.model.Bulletin;
import com.teleostnacl.szu.bulletin.model.BulletinContentModel;
import com.teleostnacl.szu.bulletin.retrofit.BulletinRetrofit;
import com.teleostnacl.szu.bulletin.viewmodel.BulletinViewModel;

/**
 * 显示公文通详细信息的Fragment
 */
public class BulletinDetailFragment extends BaseLoadingFragment {

    private BulletinViewModel bulletinViewModel;

    private Bulletin bulletin;

    private FragmentBulletinDetailBinding binding;

    private final ListAdapter<BulletinContentModel, RecyclerView.ViewHolder> listAdapter = new ListAdapter<>(new DefaultItemCallback<>()) {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());

            textView.setTextColor(ColorResourcesUtils.getColor(com.teleostnacl.szu.libs.R.color.text_color_common));

            textView.setTextSize(17);

            textView.setLineSpacing(0, 1.25f);

            int padding = ResourcesUtils.getDensityPx(6);

            textView.setPadding(0, padding, 0, padding);

            textView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            return new RecyclerView.ViewHolder(textView) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            BulletinContentModel model = getItem(position);
            TextView textView = (TextView) holder.itemView;
            textView.setText(model.getContent());
            textView.setGravity(model.rtl ? Gravity.END : Gravity.START);

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bulletinViewModel = new ViewModelProvider(requireActivity()).get(BulletinViewModel.class);

        bulletin = bulletinViewModel.getBulletin();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bulletin_detail, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setBulletin(bulletin);

        initToolbar();

//        disposable.add(bulletinViewModel.getBulletinContent(bulletin).subscribe(aBoolean -> initRecyclerView()));

        initWebView();
    }

    /**
     * 初始化toolbar
     */
    private void initToolbar() {
        // 显示返回键
        NavigationUtils.navPopBackForToolbar(binding.fragmentBulletinDetailToolbar);
    }

    private void initWebView() {
        binding.webView.loadUrl(BulletinRetrofit.getInstance().getBaseUrl() + "view.asp?id=" + bulletin.id);

    }

    private void initRecyclerView() {
//        binding.fragmentBulletinDetailRecyclerView.setAdapter(listAdapter);

//        listAdapter.submitList(bulletin.contents);
    }
}
