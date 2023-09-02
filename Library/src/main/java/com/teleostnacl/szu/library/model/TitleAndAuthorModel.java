package com.teleostnacl.szu.library.model;

import android.graphics.drawable.Drawable;

import com.teleostnacl.szu.library.model.detail.BookModel;

/**
 * 仅显示书名与作者的item所使用的Model
 */
public class TitleAndAuthorModel {
    // 文字前的drawable
    public Drawable drawable;
    // BookModel
    public BookModel bookModel;

    public TitleAndAuthorModel(BookModel bookModel, Drawable drawable) {
        this.drawable = drawable;
        this.bookModel = bookModel;
    }
}

