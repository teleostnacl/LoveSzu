package com.teleostnacl.szu.electricity.database;

import androidx.room.Dao;
import androidx.room.Query;

import com.teleostnacl.common.android.database.CRUDao;
import com.teleostnacl.common.android.utils.EncryptUtils;
import com.teleostnacl.szu.electricity.model.ElectricityDateModel;

import java.util.List;

@Dao
public interface ElectricityDateDao extends CRUDao<ElectricityDateEntry, ElectricityDateModel> {
    // 查询指定月份的ElectricityDateModel
    @Query("SELECT * FROM electricity_date WHERE year = :year AND month = :month ORDER BY day DESC")
    List<ElectricityDateEntry> getElectricityDateModelFromDatabase(String year, String month);

    default List<ElectricityDateModel> getElectricityDateModel(int year, int month) {
        return entriesToModels(getElectricityDateModelFromDatabase(
                EncryptUtils.encrypt(year), EncryptUtils.encrypt(month)));
    }

    @Query("DELETE FROM electricity_date")
    void deleteAll();

}
