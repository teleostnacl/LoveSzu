package com.teleostnacl.common.android.database;

/**
 * 数据库实体
 *
 * @param <Model> 使用数据库实体的数据的模型
 */
public interface BaseEntry<Model extends BaseModel<?>> {
    /**
     * @return 转化成使用数据的模型
     */
    Model toModel();
}
