package com.teleostnacl.szu.library.model.user;

import androidx.lifecycle.MutableLiveData;

public class MyLibraryModel {

    public MyLibraryUserInfoModel userInfoModel;

    // 借书历史总数
    public final MutableLiveData<Integer> historyBorrowBooks = new MutableLiveData<>();
}
