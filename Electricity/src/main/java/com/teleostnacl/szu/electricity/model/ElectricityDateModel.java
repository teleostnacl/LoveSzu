package com.teleostnacl.szu.electricity.model;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.text.Html;
import android.text.Spanned;
import android.view.View;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.database.BaseModel;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.electricity.R;
import com.teleostnacl.szu.electricity.database.ElectricityDateEntry;

import org.jsoup.select.Elements;

import java.util.List;

/**
 * 用电记录的模型
 */
public class ElectricityDateModel implements BaseModel<ElectricityDateEntry> {

    // 年月日
    public int year;
    public int month;
    public int day;

    // 剩余电量
    public float remain;
    // 总用电量
    public float usingSum;
    // 总购电量
    public float buyingSum;

    // 该日用电量
    public float using = 0;
    // 该日购电量
    public float buying = 0;

    // 购电记录
    public List<ElectricityBuyingModel> buyingModelList;

    public ElectricityDateModel() {
    }

    public ElectricityDateModel(@NonNull Elements element) {
        remain = NumberUtils.parseFloat(element.get(2).text(), 0);
        usingSum = NumberUtils.parseFloat(element.get(3).text(), 0);
        buyingSum = NumberUtils.parseFloat(element.get(4).text(), 0);

        String[] date = element.get(5).text().split(" ")[0].split("-");
        year = NumberUtils.parseInt(date[0], 0);
        month = NumberUtils.parseInt(date[1], 0);
        day = NumberUtils.parseInt(date[2], 0);
    }

    public String getYear() {
        return String.valueOf(year);
    }

    public String getDate() {
        return month + "/" + day;
    }

    public Spanned getUsing() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_date_detail_use, using), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getRemain() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_date_detail_remain, remain), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getBuying() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_date_detail_buy, buying), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getFloatingRemain() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_date_detail_floating_remain, remain), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getFloatingUsingSum() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_date_detail_floating_using_sum, usingSum), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getFloatingBuyingSum() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_date_detail_floating_buying_sum, buyingSum), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getFloatingUsing() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_date_detail_floating_using, using), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getFloatingBuying() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_date_detail_floating_buying, buying), FROM_HTML_MODE_COMPACT);
    }

    public int getFloatingBuyingVisible() {
        return buying == 0 ? View.GONE : View.VISIBLE;
    }

    @Override
    public ElectricityDateEntry toEntry() {
        return new ElectricityDateEntry(this);
    }
}
