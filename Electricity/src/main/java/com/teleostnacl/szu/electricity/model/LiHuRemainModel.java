package com.teleostnacl.szu.electricity.model;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.electricity.R;

import java.util.Objects;

/**
 * 记录丽湖剩余电费的模型
 */
public class LiHuRemainModel {
    public String remainBuying;
    public String remainSending;
    public String remain;
    public String date;

    /**
     * 供从网络获取数据时创建模型使用
     */
    public LiHuRemainModel(@NonNull String tmp) {
        remainBuying = tmp.split("购买剩余电\\(水\\)量 ")[1].split(" 度\\(吨\\)")[0];
        remainSending = tmp.split("补助剩余电\\(水\\)量 ")[1].split(" 度\\(吨\\)")[0];
        remain = tmp.split("，剩余电\\(水\\)量 ")[1].split(" 度\\(吨\\)")[0];
        date = tmp.split("日期：")[1].split(" 退出")[0];
    }

    public LiHuRemainModel() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiHuRemainModel that = (LiHuRemainModel) o;
        return Objects.equals(remainBuying, that.remainBuying) && Objects.equals(remainSending, that.remainSending) && Objects.equals(remain, that.remain) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remainBuying, remainSending, remain, date);
    }

    public Spanned getRemainBuying() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_lihu_ramin_detail_buying, remainBuying), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getRemainSending() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_lihu_ramin_detail_sending, remainSending) , FROM_HTML_MODE_COMPACT);
    }

    public Spanned getRemain() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_lihu_ramin_detail, remain), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getDate() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_lihu_ramin_detail_date, date), FROM_HTML_MODE_COMPACT);
    }
}
