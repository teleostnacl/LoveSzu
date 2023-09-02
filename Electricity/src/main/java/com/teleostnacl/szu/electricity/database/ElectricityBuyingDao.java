package com.teleostnacl.szu.electricity.database;

import androidx.room.Dao;
import androidx.room.Query;

import com.teleostnacl.common.android.database.CRUDao;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.szu.electricity.model.ElectricityBuyingModel;

import java.util.List;

@Dao
public interface ElectricityBuyingDao extends CRUDao<ElectricityBuyingEntry, ElectricityBuyingModel> {
    // 查询指定月份的ElectricityDateModel
    @Query("SELECT * FROM electricity_buy WHERE year = :year AND month = :month AND day = :day")
    List<ElectricityBuyingEntry> getElectricityBuyModelFromDatabase(String year, String month, String day);

    default List<ElectricityBuyingModel> getElectricityBuyModel(int year, int month, int day) {
        return entriesToModels(getElectricityBuyModelFromDatabase(
                EncryptUtils.encrypt(year), EncryptUtils.encrypt(month), EncryptUtils.encrypt(day)));
    }

    @Query("DELETE FROM electricity_buy")
    void deleteAll();
}
