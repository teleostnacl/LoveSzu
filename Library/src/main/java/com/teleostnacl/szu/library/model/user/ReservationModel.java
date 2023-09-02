package com.teleostnacl.szu.library.model.user;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.model.detail.BookModel;

import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

public class ReservationModel extends BookModel {

    // region 公共量
    //卷期
    public String volume;
    //取书地点
    public String pickupLocation;
    // endregion

    // region 预约信息
    //最迟取书日期
    public String latestLoanDate;
    //书名与作者
    public String titleAndAuthor;

    //图书类型
    public String bookType;
    //取书日期
    public String bookAvailableDate;
    //预约日期
    public String reservationDate;

    //预约信息
    public String ctl00$cpRight$borrowedRep$ctl01$cbk;

    // 取消预约用的请求map
    public final Map<String, String> cancelMap;
    // endregion

    // region 预借信息
    //提交日期
    public String submitDate;
    //状态
    public String status;
    //预借号
    public String borrowAdvanceNo;
    //预借状态号
    public String borrowAdvanceStatusNo;
    // endregion

    /**
     * 预借的构造方法
     *
     * @param element Element
     */
    public ReservationModel(@NonNull Element element) {
        ctrlno = element.child(1).select("a")
                .attr("href").split("ctrlno=")[1];
        title = element.child(1).text();
        volume = element.child(2).text();
        pickupLocation = element.child(3).text();
        submitDate = element.child(4).text();
        status = element.child(5).text();

        String tmp = element.child(6).select("a").attr("href");
        if (!TextUtils.isEmpty(tmp)) {
            borrowAdvanceNo = tmp.split("\\(")[1].split(",")[0];
            borrowAdvanceStatusNo = tmp.split(",")[1].split("\\)")[0];
        }
        cancelMap = null;
    }

    /**
     * 预约的构造方法
     *
     * @param element Element
     * @param map     含取消预约的信息
     */
    public ReservationModel(@NonNull Element element, @NonNull Map<String, String> map) {
        this.cancelMap = map;

        ctl00$cpRight$borrowedRep$ctl01$cbk = element.child(0).select("input").val();
        latestLoanDate = element.child(1).text();
        ctrlno = element.child(2).select("a").attr("href").split("ctrlno=")[1];
        titleAndAuthor = element.child(2).text();
        volume = element.child(3).text();
        bookType = element.child(4).text();
        bookAvailableDate = element.child(5).text();
        reservationDate = element.child(6).text();
        pickupLocation = element.child(7).text();
    }

    /**
     * @return 取消预约请求所用的表单信息
     */
    public Map<String, String> getCancelMap() {
        Map<String, String> map = new HashMap<>(cancelMap);
        map.put("ctl00$cpRight$borrowedRep$ctl01$cbk", ctl00$cpRight$borrowedRep$ctl01$cbk);
        return map;
    }

    // region DataBinding
    public Spanned getLatestLoanDate() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_reservation_latest_loan_date) + latestLoanDate, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getTitleAndAuthor() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_title_and_author) + titleAndAuthor, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getVolume() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_volume) + volume, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getBookType() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_loan_type) + bookType, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getBookAvailableDate() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_reservation_available_date) + bookAvailableDate, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getReservationDate() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_reservation_reserve_date) + reservationDate, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getPickupLocation() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_reservation_location_get_book) + pickupLocation, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getSubmitDate() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_reservation_borrow_advance_date) + submitDate, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getStatus() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_reservation_borrow_advance_status) + status, Html.FROM_HTML_MODE_COMPACT);
    }

    public boolean isReserve() {
        return cancelMap != null;
    }
    // endregion
}
