package com.teleostnacl.common.android.database;

/**
 * 使用数据库实体的数据的模型
 *
 * @param <Entry> 数据库实体
 */
public interface BaseModel<Entry extends BaseEntry<?>> {
    /**
     * @return 转化成数据库实体
     */
    Entry toEntry();
}
