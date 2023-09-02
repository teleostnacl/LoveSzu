package com.teleostnacl.szu.library.model.list;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.teleostnacl.szu.library.BR;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 参数清单馆藏地所使用的Model
 */
public class LibraryBookListCollectionSiteModel extends BaseObservable {
    // 馆藏地
    public String collectionSite;

    public final List<LibraryBookListModel> list = new ArrayList<>();

    public boolean check;

    public LibraryBookListCollectionSiteModel(String collectionSite, List<LibraryBookListModel> libraryBookListModels) {
        this.collectionSite = collectionSite;

        list.addAll(libraryBookListModels);

        for (LibraryBookListModel model : list) {
            model.collectionSiteModel = this;
        }

        // 检查子项是否全部被勾选
        isCheck();
    }

    /**
     * @return 是否被勾选
     */
    @Bindable
    public boolean isCheck() {
        for (LibraryBookListModel model : list) {
            if (!model.isCheck()) {
                check = false;
                return false;
            }
        }

        check = true;
        return true;
    }

    public void setCheck(boolean check) {
        if (this.check != check) {

            for (LibraryBookListModel model : list) {
                model.check = check;
                model.notifyPropertyChanged(BR.check);
            }
            notifyPropertyChanged(BR.check);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryBookListCollectionSiteModel that = (LibraryBookListCollectionSiteModel) o;
        return Objects.equals(collectionSite, that.collectionSite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionSite);
    }
}
