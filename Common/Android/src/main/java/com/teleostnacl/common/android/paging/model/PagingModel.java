package com.teleostnacl.common.android.paging.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 从网页获取的数据
 */
public class PagingModel<T> {
    // 数据
    public List<T> models = new ArrayList<>();

    // 当前页
    public int current;

    // 总页数
    public int pages;
}
