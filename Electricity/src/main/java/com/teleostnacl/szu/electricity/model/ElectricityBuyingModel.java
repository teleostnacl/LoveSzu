package com.teleostnacl.szu.electricity.model;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.database.BaseModel;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.electricity.R;
import com.teleostnacl.szu.electricity.database.ElectricityBuyingEntry;

import org.jsoup.select.Elements;

/**
 * 购电记录
 */
public class ElectricityBuyingModel implements BaseModel<ElectricityBuyingEntry> {

    // 年月日
    public int year;
    public int month;
    public int day;

    //购买者
    public String buyingPerson;
    //购买形式
    public String buyingWay;
    //购买电量
    public float buyingSum;
    //购买金额
    public float buyingMoney;
    //购买时间
    public String time = "";

    public ElectricityBuyingModel() {
    }

    public ElectricityBuyingModel(@NonNull Elements element) {
        buyingPerson = element.get(2).text();
        buyingWay = element.get(3).text();
        buyingSum = NumberUtils.parseFloat(element.get(4).text(), 0);
        buyingMoney = NumberUtils.parseFloat(element.get(5).text(), 0);

        String[] date = element.get(6).text().split(" ")[0].split("-");
        year = NumberUtils.parseInt(date[0], 0);
        month = NumberUtils.parseInt(date[1], 0);
        day = NumberUtils.parseInt(date[2], 0);

        time = element.get(6).text().split(" ")[1].split("\\.")[0];
    }

    public Spanned getTime() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_buying_detail_time, time), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getBuyingPerson() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_buying_detail_person, buyingPerson), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getBuyingWay() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_buying_detail_way, buyingWay), FROM_HTML_MODE_COMPACT);
    }

    public int getBuyingWayVisibility() {
        return TextUtils.isEmpty(buyingWay) ? View.GONE : View.VISIBLE;
    }

    public Spanned getBuyingSum() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_buying_detail_sum, buyingSum), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getBuyingMoney() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.electricity_buying_detail_money, buyingMoney), FROM_HTML_MODE_COMPACT);
    }

    @Override
    public ElectricityBuyingEntry toEntry() {
        return new ElectricityBuyingEntry(this);
    }
}
