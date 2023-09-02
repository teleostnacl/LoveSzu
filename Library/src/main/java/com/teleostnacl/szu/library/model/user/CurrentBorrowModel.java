package com.teleostnacl.szu.library.model.user;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.context.ColorResourcesUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.android.utils.TimeUtils;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.model.detail.BookModel;

import org.jsoup.nodes.Element;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * 当前借阅使用的模型
 */
public class CurrentBorrowModel extends BookModel {

    //续期
    public String renew;

    //最晚还书日期
    public String lastDueDate;

    //书名与作者
    public String titleAndAuthor;

    //登录号
    public String accessionNumber;

    //卷期
    public String volume;

    //借期
    public String loanPeriod;

    //是否被预约
    public boolean hasReserve;

    //获取当前时间
    private final static long date = new Date().getTime();

    public CurrentBorrowModel(@NonNull Element e) {
        this.renew = e.child(0).text();
        this.lastDueDate = e.child(1).text();
        this.titleAndAuthor = e.child(2).text();
        this.ctrlno = e.child(2).select("a").attr("href").split("ctrlno=")[1];
        this.volume = e.child(3).text();
        this.bookType = e.child(4).text();
        this.accessionNumber = e.child(5).text();
        this.loanPeriod = e.child(6).text();
        this.hasReserve = !e.child(7).text().equals("");
    }

    //region binding
    public String getLoanPeriod() {
        return ResourcesUtils.getString(R.string.my_library_current_borrow_at) + loanPeriod;
    }

    public Spanned getTitleAndAuthor() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_title_and_author) + titleAndAuthor, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getVolume() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_volume) + volume, Html.FROM_HTML_MODE_COMPACT);
    }

    public int getVolumeVisibility() {
        return TextUtils.isEmpty(volume) ? View.GONE : View.VISIBLE;
    }

    public Spanned getBookType() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_loan_type) + bookType, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getAccessionNumber() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_accession_number) + accessionNumber, Html.FROM_HTML_MODE_COMPACT);
    }

    @BindingAdapter("lastDueDate")
    public static void getLastDueDateColor(TextView textView, String lastDueDate) {
        long time;
        try {
            time = Objects.requireNonNull(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(lastDueDate)).getTime();
        } catch (Exception e) {
            Logger.e(e);
            time = date + TimeUtils.WEEK;
        }

        //如果应还日期晚于当前时间,则表示逾期,使用红色背景
        if (time < date) {
            textView.setBackgroundColor(ColorResourcesUtils.getColor(R.color.my_library_current_borrow_overdue_color));
            String tmp = ResourcesUtils.getString(R.string.my_library_current_borrow_should_be_back_latest_at) + lastDueDate + ResourcesUtils.getString(R.string.my_library_current_borrow_overdue);
            textView.setText(tmp);
        }
        //如果应还日期小于一周,则使用黄色背景
        else if (time - date < TimeUtils.WEEK) {
            textView.setBackgroundColor(ColorResourcesUtils.getColor(R.color.my_library_current_borrow_be_on_due));
            String tmp = ResourcesUtils.getString(R.string.my_library_current_borrow_should_be_back_latest_at) + lastDueDate + ResourcesUtils.getString(R.string.my_library_current_borrow_will_be_on_due);
            textView.setText(tmp);
        }
        //正常显示
        else {
            String tmp = ResourcesUtils.getString(R.string.my_library_current_borrow_should_be_back_latest_at) + lastDueDate;
            textView.setText(tmp);
        }
    }

    //endregion
}
