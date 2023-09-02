package com.teleostnacl.szu.library.viewmodel;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.szu.library.R;
import com.teleostnacl.szu.library.model.TitleAndAuthorModel;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.detail.ReserveModel;
import com.teleostnacl.szu.library.repository.LibraryBookDetailRepository;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;

public class LibraryBookDetailViewModel extends ViewModel {

    private final LibraryBookDetailRepository libraryBookDetailRepository = new LibraryBookDetailRepository();

    /**
     * 用于控制BookDetailFragment显示的书, 即组成类似堆栈进行控制 当Fragment创建时入堆, 当被销毁时出栈
     */
    public final List<BookModel> bookModels = new ArrayList<>();

    /**
     * 用于记录BookModel缓存, 复用已加载过的BookModel, 避免重复加载
     */
    private static final Map<String, WeakReference<BookModel>> bookModelCacheMap = new HashMap<>();

    /**
     * 设置BookDetailFragment显示的书
     *
     * @param book BookModel
     */
    public void setBook(@NonNull BookModel book) {
        WeakReference<BookModel> bookModel = bookModelCacheMap.computeIfAbsent(
                book.ctrlno, s -> new WeakReference<>(book));

        // 判断是否已经被回收
        BookModel tmp = bookModel.get();
        if (tmp == null) {
            // 已经被回收 重新push
            bookModelCacheMap.put(book.ctrlno, new WeakReference<>(book));
            tmp = book;
        }

        bookModels.add(tmp);
    }

    /**
     * @return 返回bookModels的最后一本书, 即在BookDetailFragment显示的书
     */
    public BookModel getBook() {
        return bookModels.get(bookModels.size() - 1);
    }

    /**
     * 移除最后一个bookModel
     */
    public void removeBook() {
        if (bookModels.size() > 0) {
            bookModels.remove(bookModels.size() - 1);
        }
    }

    /**
     * 获取图书详细信息
     *
     * @return 是否成功
     */
    public Observable<Boolean> getBookDetail() {
        if (getBook().init) {
            return Observable.just(true);
        }

        //设置referer
        getBook().referer = "http://www.lib.szu.edu.cn/opac/bookinfo.aspx?ctrlno=" + getBook().ctrlno;

        return Observable.zip(libraryBookDetailRepository.getBookDetail(getBook())
                                .flatMap(libraryBookDetailRepository::getBookCover),
                        libraryBookDetailRepository.getBookScore(getBook()),
                        libraryBookDetailRepository.getBookCollectCount(getBook()),
                        (bookModel, bookModel2, bookModel3) -> {
                            getBook().init = true;
                            return true;
                        })
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 收藏图书
     *
     * @return 请求成功
     */
    public Observable<Object> collectBook() {
        return libraryBookDetailRepository.collectBook(getBook()).map(s -> {
            switch (s) {
                case "0": {
                    ToastUtils.makeToast(R.string.book_detail_fragment_collect_success);
                }
                break;
                case "1": {
                    ToastUtils.makeToast(R.string.book_detail_fragment_collected);
                }
                break;
                case "2": {
                    ToastUtils.makeToast(R.string.book_detail_fragment_collect_failed);
                }
                break;
            }
            return new Object();
        });
    }

    /**
     * 给书进行评分
     *
     * @param sc 评分
     * @return 请求成功
     */
    public Observable<Object> scoreBook(String sc) {
        return libraryBookDetailRepository.scoreBook(getBook(), sc).flatMap((Function<Boolean, ObservableSource<Object>>) aBoolean -> {
            if (aBoolean) {
                return libraryBookDetailRepository.getBookScore(getBook())
                        .onErrorReturnItem(getBook())
                        .map(bookModel -> {
                            ToastUtils.makeToast(R.string.book_detail_fragment_score_success, sc);
                            return new Object();
                        });
            } else {
                ToastUtils.makeToast(R.string.book_detail_fragment_score_fail);
                return Observable.just(new Object());
            }
        });
    }

    /**
     * 从服务器获取相关借阅
     *
     * @return 是否成功
     */
    public Observable<Boolean> getRelatedBooksFromNetwork() {
        if (getBook().relatedBookList.size() != 0) {
            return Observable.just(true);
        }

        return libraryBookDetailRepository.getRelatedBook(getBook())
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取相关借阅的图书, 并包装成TitleAndAuthorModel
     *
     * @param textColor 颜色, 从LibraryMainViewModel获取
     * @return 包装成TitleAndAuthorModel相关借阅的图书
     */
    public List<TitleAndAuthorModel> getRelatedBooks(int[] textColor) {
        List<TitleAndAuthorModel> list = new ArrayList<>();

        Random random = new Random();

        for (BookModel bookModel : getBook().relatedBookList) {
            list.add(new TitleAndAuthorModel(bookModel, new Drawable() {
                private final Paint paint = new Paint();

                @Override
                public void draw(@NonNull Canvas canvas) {
                    float width = getBounds().width();
                    float height = getBounds().height();
                    float length = Math.min(width, height) / 2;
                    // 设置随机颜色
                    paint.setColor(textColor[random.nextInt(textColor.length)]);
                    paint.setAntiAlias(true);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3);

                    // 画圆
                    canvas.drawCircle(width / 2, height / 2, length / 2, paint);
                }

                @Override
                public void setAlpha(int alpha) {
                }

                @Override
                public void setColorFilter(@Nullable ColorFilter colorFilter) {
                }

                @Override
                public int getOpacity() {
                    return PixelFormat.TRANSPARENT;
                }
            }));
        }

        return list;
    }

    // region 预约与预借

    /**
     * 获取预约的信息
     *
     * @return 是否可预约
     */
    public Observable<Boolean> getReserveInformation() {
        return libraryBookDetailRepository.checkLogin(getBook())
                .flatMap(aBoolean -> {
                    // 登录失败
                    if (!aBoolean) {
                        return Observable.just(false);
                    }

                    // 当前该书未被借阅, 无需预约
                    if (getBook().reserveModel.reserveFlag == ReserveModel.CAN_NOT_LEND_NO) {
                        ToastUtils.makeToast(R.string.book_detail_fragment_reserve_no);
                        return Observable.just(false);
                    }

                    //该书预约队列已满无法在进行预约
                    if (getBook().reserveModel.reserveFlag == ReserveModel.CAN_NOT_LEND_FULL) {
                        ToastUtils.makeToast(R.string.book_detail_fragment_reserve_full);
                        return Observable.just(false);
                    }

                    return libraryBookDetailRepository.getReserveInformation(getBook().reserveModel);
                })
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 提交预约
     *
     * @return 是否预约成功
     */
    public Observable<Boolean> submitReserve() {
        return libraryBookDetailRepository.submitReserve(getBook().reserveModel).flatMap(responseBody -> {
                    Document document = Jsoup.parse(responseBody.string());
                    //成功信息
                    String successMsg = Objects.requireNonNull(document.getElementById("lblyycg")).text();
                    //成功信息不为空,则表示预借成功
                    if (!successMsg.equals("")) {
                        ToastUtils.makeToast(R.string.book_detail_fragment_reserve_successful);

                        // 更新馆藏信息
                        return libraryBookDetailRepository.getBookCollectionByAjax(getBook(), true)
                                .onErrorReturnItem(getBook())
                                .map(bookModel -> true);
                    } else {
                        //失败信息
                        String failMsg = Objects.requireNonNull(document.getElementById("lblyysb")).text();
                        ToastUtils.makeToast(failMsg);

                        return Observable.just(false);
                    }
                })
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取预借的信息
     *
     * @return 是否可预借
     */
    public Observable<Boolean> getBorrowAdvanceInformation() {
        return libraryBookDetailRepository.checkLogin(getBook())
                .flatMap(aBoolean -> {
                    if (!aBoolean) {
                        return Observable.just(false);
                    }
                    return libraryBookDetailRepository.getBorrowAdvanceInformation(getBook().reserveModel).map(aBoolean1 -> {
                        switch (getBook().reserveModel.borrowAdvanceFlag) {
                            case ReserveModel.CAN_NOT_BORROW_ADVANCE:
                                ToastUtils.makeToast(R.string.book_detail_fragment_can_not_borrow_in_advance);
                                break;
                            case ReserveModel.BORROW_ADVANCE_USER_FULL:
                                ToastUtils.makeToast(R.string.book_detail_fragment_user_borrow_in_advance_full);
                                break;
                        }
                        return aBoolean1;
                    });
                })
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 提交预借
     *
     * @return 是否预借成功
     */
    public Observable<Boolean> submitBorrowAdvance() {
        return libraryBookDetailRepository.submitBorrowAdvance(getBook().reserveModel).map(responseBody -> {
                    String bodyString = responseBody.string();
                    //如果前面有弹出提示,则表示可进行判断是否预借成功
                    if (bodyString.contains("<script language='JavaScript' type='text/javascript'>alert('")) {
                        String tmp = bodyString.split(
                                "<script language='JavaScript' type='text/javascript'>alert\\('")[1]
                                .split("'")[0];
                        switch (tmp) {
                            case "预借请求提交成功":
                                ToastUtils.makeLongToast(R.string.book_detail_fragment_borrow_in_advance_successful);
                                return true;
                            case "您已预借过这本书，不能重复提交请求":
                                ToastUtils.makeLongToast(R.string.book_detail_fragment_user_borrow_in_advance_repeat);
                                return false;
                            case "对不起，您所属的用户类型对于可预借的典藏地没有预借权，请到馆阅览":
                                ToastUtils.makeLongToast(R.string.book_detail_fragment_user_borrow_in_advance_error1);
                                return false;
                            case "对不起，您的预借数量已满，请到馆阅览":
                                ToastUtils.makeLongToast(R.string.book_detail_fragment_user_borrow_in_advance_full);
                                return false;
                            case "对不起，书库预借量已满，请到馆阅览":
                                ToastUtils.makeLongToast(R.string.book_detail_fragment_user_borrow_in_advance_error3);
                                return false;
                            case "当前没有可预借图书":
                                ToastUtils.makeLongToast(R.string.book_detail_fragment_user_borrow_in_advance_error4);
                                return false;
                        }
                    }

                    // 预约失败
                    ToastUtils.makeToast(R.string.book_detail_fragment_borrow_in_advance_fail);
                    return false;
                })
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return false;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    // endregion
}
