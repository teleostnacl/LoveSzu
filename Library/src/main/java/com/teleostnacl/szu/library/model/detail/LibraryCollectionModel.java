package com.teleostnacl.szu.library.model.detail;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.library.R;

import org.jsoup.nodes.Element;

/**
 * 图书馆馆藏信息的Model
 */
public class LibraryCollectionModel {
    // 藏书地点
    public String collectionSite;
    // 货架号
    public String shelfReference;
    // 索引号
    public String callNumber;
    // 登录号
    public String accessionNumber;
    // 卷期
    public String volume;
    // 状态
    public String state;
    // 借阅类型
    public String loanType;
    // 预约队列
    public String reservationQueue;
    // 本类预约队列
    public String readerQueueThisKind;

    public LibraryCollectionModel(@NonNull Element e) {
        //获取馆藏地址及货架号
        Element element = e.child(0);
        //如果选择"font"之后的element的size不为0,则表示有货架号
        if (element.select("font").size() != 0) {
            //设置馆藏地点
            collectionSite = element.child(0).html().split("<font class")[0];
            String tmp = element.select("font").text();
            //设置货架号
            shelfReference = tmp.substring(1, tmp.length() - 1);
        } else collectionSite = element.text();

        // 根据子项的数量, 获取其他信息
        switch (e.childrenSize()) {
            // 未登录时
            case 7: {
                callNumber = e.child(1).text();
                accessionNumber = e.child(2).text();
                volume = e.child(3).text();
                state = e.child(5).text();
                loanType = e.child(6).text();
            }
            break;
            // 未登录时, 多一个特藏码, 跳过
            case 8: {
                callNumber = e.child(2).text();
                accessionNumber = e.child(3).text();
                volume = e.child(4).text();
                state = e.child(6).text();
                loanType = e.child(7).text();
            }
            break;
            // 登录状态下,多预约情况
            case 9: {
                callNumber = e.child(1).text();
                accessionNumber = e.child(2).text();
                volume = e.child(3).text();
                state = e.child(5).text();
                loanType = e.child(6).text();
                reservationQueue = e.child(7).text();
                readerQueueThisKind = e.child(8).text();
            }
            break;
            // 登录状态下, 存在特藏码, 跳过
            case 10: {
                callNumber = e.child(2).text();
                accessionNumber = e.child(3).text();
                volume = e.child(4).text();
                state = e.child(6).text();
                loanType = e.child(7).text();
                reservationQueue = e.child(8).text();
                readerQueueThisKind = e.child(9).text();
            }
            break;
        }
    }

    public LibraryCollectionModel(){}

    // region DataBinding
    public Spanned getCollectionSite() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_site) + collectionSite, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getShelfReference() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_shelf) + shelfReference, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getCallNumber() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_call_number) + callNumber, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getAccessionNumber() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_accession_number) + accessionNumber, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getVolume() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_volume) + volume, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getState() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_state) + state, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLoanType() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_loan_type) + loanType, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getReservationQueue() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_reservation_queue) + reservationQueue, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getReaderQueueThisKind() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_reader_queue_this_kind) + readerQueueThisKind, FROM_HTML_MODE_COMPACT);
    }

    public int getShelfReferenceVisibility() {
        return TextUtils.isEmpty(shelfReference) ? GONE : VISIBLE;
    }

    public int getVolumeVisibility() {
        return TextUtils.isEmpty(volume) ? GONE : VISIBLE;
    }

    public int getReservationQueueVisibility() {
        return TextUtils.isEmpty(reservationQueue) ? GONE : VISIBLE;
    }

    public int getReaderQueueThisKindVisibility() {
        return TextUtils.isEmpty(readerQueueThisKind) ? GONE : VISIBLE;
    }
    // endregion
}
