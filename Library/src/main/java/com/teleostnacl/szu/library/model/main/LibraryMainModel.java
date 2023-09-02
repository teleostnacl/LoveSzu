package com.teleostnacl.szu.library.model.main;

import androidx.databinding.BaseObservable;

import com.teleostnacl.szu.library.model.detail.BookModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据模型
 */
public class LibraryMainModel extends BaseObservable {

    // 存放热门搜索原始数据
    public final List<String> hotSearchList = new ArrayList<>();
    // 存放热门借阅与收藏的原始数据
    public final List<BookModel> hotLoanList = new ArrayList<>();

}
