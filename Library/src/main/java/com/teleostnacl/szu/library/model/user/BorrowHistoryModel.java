package com.teleostnacl.szu.library.model.user;

import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.model.detail.BookModel;

import org.jsoup.nodes.Element;

public class BorrowHistoryModel extends BookModel {

    //登录号
    public String accessionNumber;

    //借期
    public String loanPeriod;

    //还期
    public String dueDate;

    //最晚还书日期
    public String lastDueDate;

    //书名与作者
    public String titleAndAuthor;

    public BorrowHistoryModel(@NonNull Element element) {
        loanPeriod = element.child(0).text();
        dueDate = element.child(1).text();
        lastDueDate = element.child(2).text();
        titleAndAuthor = element.child(3).text();
        ctrlno = element.child(3).select("a").attr("href").split("ctrlno=")[1];
        bookType = element.child(4).text();
        accessionNumber = element.child(5).text();
    }


    public Spanned getTitleAndAuthor() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_title_and_author) + titleAndAuthor, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getBookType() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_loan_type) + bookType, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getAccessionNumber() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collection_accession_number) + accessionNumber, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLoanPeriod() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_borrow_history_loan_period) + loanPeriod, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getDueDate() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_borrow_history_due_date) + dueDate, Html.FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLastDueDate() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.my_library_borrow_history_latest_due_date) + lastDueDate, Html.FROM_HTML_MODE_COMPACT);
    }
}
