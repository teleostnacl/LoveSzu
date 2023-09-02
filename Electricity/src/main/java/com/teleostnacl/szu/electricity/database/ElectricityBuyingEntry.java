package com.teleostnacl.szu.electricity.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.teleostnacl.common.android.database.BaseEntry;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.electricity.model.ElectricityBuyingModel;

@Entity(tableName = "electricity_buy", primaryKeys = {"year", "month", "day", "time"})
public class ElectricityBuyingEntry implements BaseEntry<ElectricityBuyingModel> {
    // 年月日
    @ColumnInfo(name = "year")
    @NonNull
    public final String year;
    @ColumnInfo(name = "month")
    @NonNull
    public final String month;
    @ColumnInfo(name = "day")
    @NonNull
    public final String day;

    //购买者
    @ColumnInfo(name = "buying_person")
    public final String buyingPerson;
    //购买形式
    @ColumnInfo(name = "buying_way")
    public final String buyingWay;
    //购买电量
    @ColumnInfo(name = "buying_sum")
    public final String buyingSum;
    //购买金额
    @ColumnInfo(name = "buying_money")
    public final String buyingMoney;
    //购买时间
    @ColumnInfo(name = "time")
    @NonNull
    public final String time;

    public ElectricityBuyingEntry(@NonNull String year, @NonNull String month, @NonNull String day, String buyingPerson,
                                  String buyingWay, String buyingSum, String buyingMoney, @NonNull String time) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.buyingPerson = buyingPerson;
        this.buyingWay = buyingWay;
        this.buyingSum = buyingSum;
        this.buyingMoney = buyingMoney;
        this.time = time;
    }

    public ElectricityBuyingEntry(@NonNull ElectricityBuyingModel model) {
        this.year = EncryptUtils.encrypt(model.year);
        this.month = EncryptUtils.encrypt(model.month);
        this.day = EncryptUtils.encrypt(model.day);
        this.buyingPerson = EncryptUtils.encrypt(model.buyingPerson);
        this.buyingWay = EncryptUtils.encrypt(model.buyingWay);
        this.buyingSum = EncryptUtils.encrypt(model.buyingSum);
        this.buyingMoney = EncryptUtils.encrypt(model.buyingMoney);
        this.time = EncryptUtils.encrypt(model.time);
    }

    @Override
    public ElectricityBuyingModel toModel() {
        ElectricityBuyingModel model = new ElectricityBuyingModel();
        model.year = NumberUtils.parseInt(EncryptUtils.decrypt(year), 0);
        model.month = NumberUtils.parseInt(EncryptUtils.decrypt(month), 0);
        model.day = NumberUtils.parseInt(EncryptUtils.decrypt(day), 0);
        model.buyingPerson = EncryptUtils.decrypt(buyingPerson);
        model.buyingWay = EncryptUtils.decrypt(buyingWay);
        model.buyingSum = NumberUtils.parseFloat(EncryptUtils.decrypt(buyingSum), 0);
        model.buyingMoney = NumberUtils.parseFloat(EncryptUtils.decrypt(buyingMoney), 0);
        model.time = EncryptUtils.decrypt(time);
        return model;
    }
}
