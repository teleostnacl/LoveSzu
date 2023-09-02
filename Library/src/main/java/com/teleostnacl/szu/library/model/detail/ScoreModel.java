package com.teleostnacl.szu.library.model.detail;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.library.BR;
import com.teleostnacl.szu.library.R;

/**
 * 评分所使用的模型
 */
public class ScoreModel extends BaseObservable {
    public View.OnClickListener onClickListener = null;

    @Bindable
    public View.OnClickListener getOnClickListener() { return onClickListener; }
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        notifyPropertyChanged(BR.onClickListener);
    }

    @BindingAdapter("scoreText")
    public static void setText(@NonNull TextView textView, int i) {
        String text = i + (i == 1 ?
                ResourcesUtils.getString(R.string.book_detail_fragment_score_view_point) :
                ResourcesUtils.getString(R.string.book_detail_fragment_score_view_points));
        textView.setText(text);
    }
}
