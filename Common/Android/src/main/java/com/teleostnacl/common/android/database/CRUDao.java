package com.teleostnacl.common.android.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * 自带增删改的Dao, 且数据库实体与使用的模型分离
 *
 * @param <Entry> 数据库实体
 * @param <Model> 使用的数据模型
 */
@Dao
public interface CRUDao<Entry extends BaseEntry<Model>, Model extends BaseModel<Entry>> {

    /**
     * 增加数据库实体
     */
    @Insert
    void insert(Entry entry);

    /**
     * 删除数据库实体
     */
    @Delete
    void delete(Entry entry);

    /**
     * 更新数据库实体
     */
    @Update
    void update(Entry entry);

    /**
     * 增加 或 更新(如果原来存在的话) 数据库实体
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(Entry entry);

    /**
     * 增加使用的数据模型
     */
    default void insert(@NonNull Model model) {
        insert(model.toEntry());
    }

    /**
     * 删除使用的数据模型
     */
    default void delete(@NonNull Model model) {
        delete(model.toEntry());
    }

    /**
     * 更新使用的数据模型
     */
    default void update(@NonNull Model model) {
        update(model.toEntry());
    }

    /**
     * 增加 或 更新(如果原来存在的话) 数据模型
     */
    default void insertOrUpdate(@NonNull Model model) {
        insertOrUpdate(model.toEntry());
    }

    /**
     * 将List&lt;Model> 转化成 List&lt;Entry>
     */
    default List<Entry> modelsToEntries(@NonNull List<Model> models) {
        List<Entry> entries = new ArrayList<>(models.size());

        for (Model model : models) {
            entries.add(model.toEntry());
        }

        return entries;
    }

    /**
     * 将List&lt;Entry> 转化成 List&lt;Model>
     */
    default List<Model> entriesToModels(@NonNull List<Entry> entries) {
        List<Model> models = new ArrayList<>(entries.size());

        for (Entry entry : entries) {
            models.add(entry.toModel());
        }

        return models;
    }
}
