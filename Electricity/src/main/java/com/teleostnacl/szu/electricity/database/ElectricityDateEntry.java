package com.teleostnacl.szu.electricity.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.teleostnacl.common.android.database.BaseEntry;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.electricity.model.ElectricityDateModel;

@Entity(tableName = "electricity_date", primaryKeys = {"year", "month", "day"})
public class ElectricityDateEntry implements BaseEntry<ElectricityDateModel> {

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

    // 剩余电量
    @ColumnInfo(name = "remain")
    public final String remain;
    // 总用电量
    @ColumnInfo(name = "usingSum")
    public final String usingSum;
    // 总购电量
    @ColumnInfo(name = "buyingSum")
    public final String buyingSum;

    // 该日用电量
    @ColumnInfo(name = "using")
    public final String using;
    // 该日购电量
    @ColumnInfo(name = "buying")
    public final String buying;

    public ElectricityDateEntry(@NonNull String year, @NonNull String month, @NonNull String day, String remain, String usingSum,
                                String buyingSum, String using, String buying) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.remain = remain;
        this.usingSum = usingSum;
        this.buyingSum = buyingSum;
        this.using = using;
        this.buying = buying;
    }

    public ElectricityDateEntry(@NonNull ElectricityDateModel model) {
        this.year = EncryptUtils.encrypt(model.year);
        this.month = EncryptUtils.encrypt(model.month);
        this.day = EncryptUtils.encrypt(model.day);
        this.remain = EncryptUtils.encrypt(model.remain);
        this.usingSum = EncryptUtils.encrypt(model.usingSum);
        this.buyingSum = EncryptUtils.encrypt(model.buyingSum);
        this.using = EncryptUtils.encrypt(model.using);
        this.buying = EncryptUtils.encrypt(model.buying);
    }

    @Override
    public ElectricityDateModel toModel() {
        ElectricityDateModel model = new ElectricityDateModel();
        model.year = NumberUtils.parseInt(EncryptUtils.decrypt(year), 0);
        model.month = NumberUtils.parseInt(EncryptUtils.decrypt(month), 0);
        model.day = NumberUtils.parseInt(EncryptUtils.decrypt(day), 0);
        model.remain = NumberUtils.parseFloat(EncryptUtils.decrypt(remain), 0);
        model.usingSum = NumberUtils.parseFloat(EncryptUtils.decrypt(usingSum), 0);
        model.buyingSum = NumberUtils.parseFloat(EncryptUtils.decrypt(buyingSum), 0);
        model.using = NumberUtils.parseFloat(EncryptUtils.decrypt(using), 0);
        model.buying = NumberUtils.parseFloat(EncryptUtils.decrypt(buying), 0);
        return model;
    }
}
