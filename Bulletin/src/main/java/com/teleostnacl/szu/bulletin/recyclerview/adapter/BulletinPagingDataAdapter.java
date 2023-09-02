package com.teleostnacl.szu.bulletin.recyclerview.adapter;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.paging.adapter.ClickOncePagingDataAdapter;
import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.view.recyclerview.DataBindingVH;
import com.teleostnacl.common.android.view.recyclerview.DefaultItemCallback;
import com.teleostnacl.szu.bulletin.R;
import com.teleostnacl.szu.bulletin.databinding.ItemInfoTypeBulletinBinding;
import com.teleostnacl.szu.bulletin.model.Bulletin;
import com.teleostnacl.szu.bulletin.retrofit.BulletinRetrofit;

public class BulletinPagingDataAdapter extends ClickOncePagingDataAdapter<Bulletin, DataBindingVH<ItemInfoTypeBulletinBinding>> {
    public BulletinPagingDataAdapter() {
        super(new DefaultItemCallback<>());
    }

    @Override
    public void onClick(@NonNull View v) {
        Bulletin bulletin = (Bulletin) v.getTag();

        ContextUtils.startBrowserActivity(BulletinRetrofit.getInstance().getBaseUrl() + "view.asp?id=" + bulletin.id);
    }

    @NonNull
    @Override
    public DataBindingVH<ItemInfoTypeBulletinBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DataBindingVH<ItemInfoTypeBulletinBinding> viewHolder = new DataBindingVH<>(parent, R.layout.item_info_type_bulletin);

        setOnClickListener(viewHolder.itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DataBindingVH<ItemInfoTypeBulletinBinding> holder, int position) {
        Bulletin bulletin = getItem(position);

        if (bulletin == null) {
            return;
        }

        holder.itemView.setTag(bulletin);
        holder.binding.setBulletin(bulletin);
        holder.binding.title.setTypeface(bulletin.titleBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

    }
}
