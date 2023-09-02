package com.teleostnacl.szu.library.model.detail;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.MutableLiveData;

import com.teleostnacl.common.android.retrofit.QueryFieldMap;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.StringUtils;
import com.teleostnacl.szu.library.BR;
import com.teleostnacl.szu.library.R;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 记录图书信息的model
 */
public class BookModel extends BaseObservable {

    // referer Url
    public String referer;

    // 图书控制号
    public String ctrlno;
    // 书名
    public String title;
    // 作者
    public String author;
    // 出版者
    public String publisher;
    // 出版年
    public String year;
    // 记录号
    public String callNumber;
    // 馆藏数量
    public String collectionsSum;
    // 可借
    public String canLend;
    // 封面
    public Bitmap cover;
    // ISBN
    public String ISBN;
    // 介绍信息
    public String information;

    //图书类型
    public String bookType;

    // 近一年的借阅次数
    public int loanTotal;
    // 总收藏数
    public int collectTotal;
    // 总评分次数
    public int totalVotes;

    //获得的评分
    public int score = -1;
    //我的评分
    public int myScore = -1;

    //图书馆的藏书信息
    public final MutableLiveData<List<LibraryCollectionModel>> bookCollectionLiveData = new MutableLiveData<>();

    //预借预约的信息
    public ReserveModel reserveModel;

    // 相关图书
    public final List<BookModel> relatedBookList = new ArrayList<>();

    // 标记是否已经获得了图书详细信息
    public boolean init = false;

    /**
     * 热门借阅与收藏使用的构造方法
     *
     * @param title  书名
     * @param author 作者
     * @param url    图书详细页面
     */
    public BookModel(String title, String author, @NonNull String url) {
        this.title = title;
        this.author = author;
        this.ctrlno = url.split("ctrlno=")[1];
    }

    public BookModel() {
    }

    public BookModel(String ctrlno) {
        this.ctrlno = ctrlno;
    }

    /**
     * 供搜索结果使用的构造方法
     *
     * @param tdList 搜索结果的HTML表格中的一行, 即一项图书信息
     */
    public BookModel(@NonNull List<Element> tdList) {
        title = tdList.get(1).text();
        ctrlno = tdList.get(1).select("a").attr("href").split("ctrlno=")[1];
        author = tdList.get(2).text();
        publisher = tdList.get(3).text();
        year = tdList.get(4).text();
        callNumber = tdList.get(5).text();
        collectionsSum = tdList.get(6).text();
        canLend = tdList.get(7).text();
    }

    /**
     * @return 获取图片封面url的post表单
     */
    public Map<String, String> getBookCoverUrlMap() {
        Map<String, String> map = getCommonMap();
        map.put("title", title);
        map.put("isbn", ISBN);
        return map;
    }

    /**
     * @return 含ctrlno通用map
     */
    public Map<String, String> getCommonMap() {
        Map<String, String> map = new QueryFieldMap();
        map.put("ctrlno", ctrlno);
        map.put("_", "");
        return map;
    }

    // region Databinding
    public String getTitleString() {
        return title;
    }

    public Spanned getTitle() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_title) + StringUtils.getOrBlank(title), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getPublishYear() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_publish_year) + StringUtils.getOrBlank(year), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getAuthor() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_author) + StringUtils.getOrBlank(author), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getPublisher() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_publisher) + StringUtils.getOrBlank(publisher), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getPublisherAndYear() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_publisher_and_year) +
                        StringUtils.getOrBlank(publisher) + ", " + StringUtils.getOrBlank(year),
                FROM_HTML_MODE_COMPACT);
    }

    public Spanned getCanLend() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_can_lend) + StringUtils.getOrBlank(canLend), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getCallNumber() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_call_number) + callNumber, FROM_HTML_MODE_COMPACT);
    }

    public Spanned getCallNumberAndCanLend() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_call_number) + StringUtils.getOrBlank(callNumber) + " "
                + ResourcesUtils.getString(R.string.library_book_can_lend) + StringUtils.getOrBlank(canLend), FROM_HTML_MODE_COMPACT);
    }

    @Bindable
    public Drawable getCover() {
        return cover != null ? new BitmapDrawable(ResourcesUtils.getResources(), cover) :
                ResourcesUtils.getDrawable(R.drawable.book_cover_default);
    }

    public void setCover(Bitmap cover) {
        if (this.cover != null && this.cover != cover) {
            this.cover.recycle();
        }
        this.cover = cover;

        notifyPropertyChanged(BR.cover);
    }

    public Spanned getInformation() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.library_book_detail_information) + StringUtils.getOrBlank(information), FROM_HTML_MODE_COMPACT);
    }

    public Spanned getLoanTotal() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_loan_total) + loanTotal + ResourcesUtils.getString(R.string.book_detail_times), FROM_HTML_MODE_COMPACT);
    }

    @Bindable
    public Spanned getCollectTotal() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_collect_total) + collectTotal + ResourcesUtils.getString(R.string.book_detail_times), FROM_HTML_MODE_COMPACT);
    }

    @Bindable
    public Spanned getTotalVotesString() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.book_detail_fragment_total_votes) + totalVotes + ResourcesUtils.getString(R.string.book_detail_times), FROM_HTML_MODE_COMPACT);
    }

    /**
     * 更新评分信息
     */
    public void updateScore() {
        notifyPropertyChanged(BR.score);
        notifyPropertyChanged(BR.myScore);
        notifyPropertyChanged(BR.totalVotesString);
    }

    @Bindable
    public int getMyScore() {
        return myScore;
    }

    @Bindable
    public int getScore() {
        return score;
    }

    @Bindable
    public int getTotalVotes() {
        return totalVotes;
    }

    public void setCollectTotal(int collectTotal) {
        this.collectTotal = collectTotal;
        notifyPropertyChanged(BR.collectTotal);
    }

    /**
     * 根据评分以及星星所在具体位置,设置显示不同的星星
     */
    @BindingAdapter(value = {"library_book_score", "library_score_location"})
    public static void showStar(@NonNull ImageView view, int score, int i) {
        view.setImageResource(i <= score ? R.drawable.xing : R.drawable.xing_gray);
    }
    // endregion
}
