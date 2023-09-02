package com.teleostnacl.szu.library.model.list;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import com.teleostnacl.common.android.database.BaseModel;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.library.BR;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.database.LibraryBookListEntry;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.detail.LibraryCollectionModel;

import java.util.Date;
import java.util.Objects;

public class LibraryBookListModel extends BookModel implements BaseModel<LibraryBookListEntry> {

    //卷期
    public String volume;

    // 馆藏地
    public String collectionSite;

    // 排/架
    public String shelfReference;

    // 放入图书清单的时间
    public long date;

    //登录号
    public String accessionNumber;

    // 是否被勾选
    public boolean check;

    // 是否为一组中的最后一个
    public boolean last;

    public LibraryBookListCollectionSiteModel collectionSiteModel;

    @Override
    public LibraryBookListEntry toEntry() {
        return new LibraryBookListEntry(this);
    }

    public LibraryBookListModel() {

    }

    public LibraryBookListModel(@NonNull BookModel bookModel, @NonNull LibraryCollectionModel libraryCollectionModel) {
        date = new Date().getTime();

        accessionNumber = libraryCollectionModel.accessionNumber;
        callNumber = libraryCollectionModel.callNumber;
        volume = libraryCollectionModel.volume;
        title = bookModel.title;
        ctrlno = bookModel.ctrlno;
        cover = bookModel.cover;
        collectionSite = libraryCollectionModel.collectionSite;
        shelfReference = libraryCollectionModel.shelfReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryBookListModel model = (LibraryBookListModel) o;
        return Objects.equals(accessionNumber, model.accessionNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessionNumber);
    }

    // region Databinding
    public Spanned getVolume() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_volume) + volume, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getAccessionNumber() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_accession_number) + accessionNumber, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getShelfReference() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_shelf) + shelfReference, FROM_HTML_MODE_COMPACT);
    }

    public int getShelfReferenceVisibility() {
        return TextUtils.isEmpty(shelfReference) ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
        notifyPropertyChanged(BR.check);
        collectionSiteModel.notifyPropertyChanged(BR.check);
    }

    // endregion
}
