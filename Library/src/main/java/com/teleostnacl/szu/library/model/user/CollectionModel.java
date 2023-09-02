package com.teleostnacl.szu.library.model.user;

import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.model.detail.BookModel;

import org.jsoup.nodes.Element;

public class CollectionModel extends BookModel {

    //馆藏地点
    public String collectionSite;

    //收藏时间
    public String collectionTime;

    public CollectionModel(@NonNull Element e) {
        this.title = e.child(1).text();
        this.ctrlno = e.child(1).select("a").attr("href").split("ctrlno=")[1];
        this.callNumber = e.child(2).text();
        this.canLend = e.child(4).text();
        this.collectionTime = e.child(5).text();

        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for(String s : e.child(3).html().split("<br>")) {
            stringBuilder.append("\n").append(i++).append(". ").append(s);
        }
        this.collectionSite = stringBuilder.substring(1);
    }

    //馆藏地点
    public Spanned getCollectionSiteTitle() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_site), Html.FROM_HTML_MODE_COMPACT);
    }

    //收藏时间
    public Spanned getCollectionTime() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_collection_date)
                + collectionTime, Html.FROM_HTML_MODE_LEGACY);
    }
}
