package com.teleostnacl.szu.library.repository;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.network.NetworkUtils;
import com.teleostnacl.common.android.retrofit.RetrofitUtils;
import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.java.util.IOUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.detail.LibraryCollectionModel;
import com.teleostnacl.szu.library.model.detail.ReserveModel;
import com.teleostnacl.szu.library.retrofit.BookDetailApi;
import com.teleostnacl.szu.library.retrofit.LibraryRetrofit;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.io.FileSystem;
import okio.Buffer;
import okio.Sink;
import okio.Source;
import retrofit2.Response;

public class LibraryBookDetailRepository {
    // 储存图书封面的缓存管理器
    private final DiskLruCache diskLruCache = DiskLruCache.create(FileSystem.SYSTEM,
            ContextUtils.getDiskCacheDir("LibraryBookCover"),
            1, 1, /* 10M 缓存*/10 * 1024 * 1024);

    private final BookDetailApi api = LibraryRetrofit.getInstance().getBookDetailApi();

    /**
     * 获取图书详细信息
     *
     * @param book BookModel
     * @return 图书详细信息
     */
    public Observable<BookModel> getBookDetail(@NonNull BookModel book) {
        return getBookDetailResponseBody(book).flatMap(responseBody -> {
            String bodyString = responseBody.string();
            Document document = Jsoup.parse(bodyString);

            //获取ISBN
            book.ISBN = bodyString.split("getBookCover\\(\"")[1]
                    .split("\",\"")[1].split("\"")[0];

            //获取图书基本信息
            String text = Objects.requireNonNull(
                    document.getElementById("ctl00_ContentPlaceHolder1_bookcardinfolbl")).text();

            //标准text :书名／作者．—地点：出版社，出版年 　　其它内容...
            if (text.matches(".*／.*．—.*：.*，.*")) {
                String[] tmp = text.split("／", 2);
                // 获取书名
                book.title = tmp[0].replaceFirst("(\\s)+", "");

                //获取作者
                tmp = tmp[1].split("．—", 2);
                book.author = tmp[0];

                //获取出版社
                tmp = tmp[1].split("：", 2);
                tmp = tmp[1].split("，", 2);
                book.publisher = tmp[0];

                //获取年份
                tmp = tmp[1].split(" 　　", 2);
                book.year = tmp[0];
            }
            //缺年份: 书名／作者．—地点：出版社 　　其它内容...
            else if (text.matches(".*／.*．—.*：.*")) {
                String[] tmp = text.split("／", 2);
                // 获取书名
                book.title = tmp[0].replaceFirst("(\\s)+", "");

                //获取作者
                tmp = tmp[1].split("．—", 2);
                book.author = tmp[0];

                //获取出版社
                tmp = tmp[1].split("：", 2);
                tmp = tmp[1].split(" 　　", 2);
                book.publisher = tmp[0];
            }
            //缺作者: 书名(．—版本)．—地点：出版社，出版年\n其它内容...
            else if (text.matches(".*．—.*：.*，.*")) {
                String[] tmp = text.split("．—", 2);

                // 获取书名
                book.title = tmp[0].replaceFirst("(\\s)+", "");

                //获取出版社
                tmp = tmp[1].split("：", 2);
                tmp = tmp[1].split("，", 2);
                book.publisher = tmp[0];

                //获取年份
                tmp = tmp[1].split(" 　　", 2);
                book.year = tmp[0];
            }

            //获取图书简介
            book.information = text
                    .replaceFirst("(\\s)+", "")           //去除开头空白
                    .replaceAll("(\\s){2,}", "<br>");     //替换换行符

            //获取近一年借阅次数
            book.loanTotal = NumberUtils.parseInt(Objects.requireNonNull(
                    document.getElementById("ctl00_ContentPlaceHolder1_blclbl")).text(), 0);

            // 获取藏书信息
            Element libraryBookCollection = document.selectFirst("tbody");
            if (libraryBookCollection != null) {
                getBookCollectionInformation(book, libraryBookCollection);
            } else {
                // tbody为空时表示已经登录, 调用Ajax进行获取
                return getBookCollectionByAjax(book, true);
            }

            return Observable.just(book);
        });
    }

    /**
     * 获取图书详细信息, 并判断是否需要登录
     *
     * @param bookModel BookModel
     * @return 图书详细信息
     */
    private Observable<ResponseBody> getBookDetailResponseBody(@NonNull BookModel bookModel) {
        return api.getBookDetail(bookModel.ctrlno)
                .flatMap(LoginUtil.checkLoginHandleObservable(() -> getBookDetailResponseBody(bookModel)));
    }

    /**
     * 获取藏书信息
     *
     * @param book    BookModel
     * @param element document.selectFirst("tbody")
     */
    private void getBookCollectionInformation(@NonNull BookModel book, @NonNull Element element) {

        //获取藏书信息
        List<LibraryCollectionModel> collectionModels = new ArrayList<>();
        for (Element e : element.select("tr"))
            collectionModels.add(new LibraryCollectionModel(e));

        //遍历LibraryCollectionModel获取可借信息,并记录仅供阅览的图书
        int i = 0;
        List<LibraryCollectionModel> list = new ArrayList<>();
        for (LibraryCollectionModel collection : collectionModels) {
            // 可供出借 可借数量+1
            if (collection.state.equals("可供出借")) i++;
                // 不含有应还, 此书暂不可借阅, 将其放到底部
            else if (!collection.state.contains("应还") &&
                    !collection.state.equals("预借待取") &&
                    !collection.state.equals("预约待借")) {
                list.add(collection);
            }
        }

        //调整LibraryCollectionModel的顺序,将不可借阅的图书置于末尾
        collectionModels.removeAll(list);
        collectionModels.addAll(list);

        book.canLend = String.valueOf(i);

        book.bookCollectionLiveData.postValue(collectionModels);
    }

    /**
     * 通过Ajax获取参数信息, 并获取可预约预借的情况
     *
     * @param book                  BookModel
     * @param refreshBookCollection 是否刷新馆藏信息
     * @return book
     */
    public Observable<BookModel> getBookCollectionByAjax(@NonNull BookModel book, boolean refreshBookCollection) {
        return api.getBookCollectionByAjax(book.referer, book.getCommonMap()).map(responseBody -> {
            // book.reserveModel为空 表示登录之后暂未获取馆藏信息, 进行获取 或者强制刷新获取
            if (book.reserveModel == null || refreshBookCollection) {
                String bodyString = responseBody.string();
                Document document = Jsoup.parse(bodyString);

                //预约信息
                ReserveModel model = book.reserveModel = new ReserveModel();
                model.ctrlno = book.ctrlno;

                if (bodyString.contains("根据本馆预约规则，此书预约排队人数已达上限")) {
                    model.reserveFlag = ReserveModel.CAN_NOT_LEND_FULL;
                } else if (bodyString.contains("根据本馆预约规则，当已出借册数至少为1册时方可预约")) {
                    model.reserveFlag = ReserveModel.CAN_NOT_LEND_NO;
                } else {
                    model.reserveFlag = ReserveModel.CAN_LEND;
                    Element bodyElement = document.body();
                    model.bktype = Objects.requireNonNull(
                            bodyElement.getElementById("txtbktype")).val();
                    model.org = Objects.requireNonNull(bodyElement.getElementById("txtorg")).val();
                    model.volbooktype = Objects.requireNonNull(bodyElement.getElementById("txtvolbooktpe")).val();

                    model.vol = null;
                    //可预约的卷期
                    for (Element element : Objects.requireNonNull(bodyElement.getElementById("lblyy")).select("td")) {
                        String key = element.select("input").val();
                        if (!key.equals("")) {
                            //默认选择预约第一个卷期
                            if (model.vol == null) {
                                model.vol = key;
                            }
                            model.volumes.put(key, element.text());
                        }
                    }
                }

                //获取藏书信息
                getBookCollectionInformation(book, Objects.requireNonNull(document.selectFirst("tbody")));
            }
            return book;
        });
    }

    /**
     * 获取图书评分信息
     *
     * @param book BookModel
     * @return book
     */
    public Observable<BookModel> getBookScore(@NonNull BookModel book) {
        return api.getBookScore(book.referer, book.getCommonMap()).map(responseBody -> {
            String bodyString = responseBody.string();

            if (!(bodyString.equals("1") || bodyString.equals(""))) {
                String[] rcs = bodyString.split("@@");
                //获取总评分次数
                book.totalVotes = NumberUtils.parseInt(rcs[1], 0);
                //获取总评分
                if (rcs[0].equals("2") || rcs[0].equals("3"))
                    book.score = NumberUtils.parseInt(rcs[2], 0);
                //获取用户评分
                if (rcs.length == 5) {
                    book.myScore = NumberUtils.parseInt(rcs[4], 0);
                }
                book.updateScore();
            }

            return book;
        });
    }

    /**
     * 获取图书收藏数的信息
     *
     * @param book BookModel
     * @return book
     */
    public Observable<BookModel> getBookCollectCount(@NonNull BookModel book) {
        return api.getBookCollectCount(book.referer, book.getCommonMap())
                .map(responseBody -> {
                    book.setCollectTotal(NumberUtils.parseInt(responseBody.string(), 0));
                    return book;
                });
    }

    // region 封面

    /**
     * 获取图片封面
     *
     * @param book BookModel
     * @return book
     */
    public Observable<BookModel> getBookCover(@NonNull BookModel book) {

        // ISBN值为空时, 不进行获取
        if (TextUtils.isEmpty(book.ISBN)) {
            book.setCover(null);
            return Observable.just(book);
        }

        if (readFromDiskLruCache(book)) {
            return Observable.just(book);
        }

        return Observable.just(new Object()).map(o -> {
                    // 从缓存中获取
                    DiskLruCache.Snapshot snapShot = diskLruCache.get(book.ctrlno);

                    // 缓存为空
                    if (snapShot == null) {
                        return false;
                    }

                    //获取资源的输出流, Source类似InputStream
                    Source source = snapShot.getSource(0);
                    Buffer buffer = new Buffer();
                    //读取4*1024数据放入buffer中并返回读取到的数据的字节长度
                    long ret = source.read(buffer, 4 * 1024);
                    //判断文件是否读完
                    while (ret != -1) {
                        ret = source.read(buffer, 4 * 1024);
                    }
                    //获取到buffer的inputStream对象
                    InputStream inputStream = buffer.inputStream();

                    // 设置封面
                    book.setCover(BitmapFactory.decodeStream(inputStream));

                    // 关闭IO流
                    IOUtils.close(inputStream, buffer, source, snapShot);
                    return true;
                })
                .onErrorReturnItem(false)
                .flatMap(aBoolean -> {
                    // 从缓存中获取成功
                    if (aBoolean) {
                        return Observable.just(book);
                    }

                    return api.getBookCoverUrl(book.referer, book.getBookCoverUrlMap()).flatMap(responseBody -> {
                                String bodyString = responseBody.string();
                                //结果为预期非正确结果,不显示封面图片
                                if (bodyString.equals("") || bodyString.equals("1") || bodyString.equals("2")) {
                                    book.setCover(null);
                                    return Observable.just(book);
                                } else {
                                    return api.getBookCover(bodyString).map(responseBody1 -> {
                                        writeToDiskLruCache(book, responseBody1);
                                        readFromDiskLruCache(book);
                                        return book;
                                    });
                                }
                            })
                            .onErrorReturn(throwable -> {
                                Logger.e(throwable);
                                book.setCover(null);
                                return book;
                            });
                })
                .onErrorReturnItem(book);

    }

    /**
     * 从磁盘缓存中读取到的封面
     *
     * @param book BookModel
     * @return 是否成功
     */
    private boolean readFromDiskLruCache(BookModel book) {
        Source source = null;
        Buffer buffer = null;
        InputStream inputStream = null;
        DiskLruCache.Snapshot snapShot = null;

        try {
            // 从缓存中获取
            snapShot = diskLruCache.get(book.ctrlno);

            //获取资源的输出流, Source类似InputStream
            source = snapShot.getSource(0);
            buffer = new Buffer();
            //读取4*1024数据放入buffer中并返回读取到的数据的字节长度
            long ret = source.read(buffer, 4 * 1024);
            //判断文件是否读完
            while (ret != -1) {
                ret = source.read(buffer, 4 * 1024);
            }
            //获取到buffer的inputStream对象
            inputStream = buffer.inputStream();

            // 设置封面
            book.setCover(BitmapFactory.decodeStream(inputStream));

            return true;
        } catch (Exception e) {
            Logger.e(e);
            return false;
        } finally {
            // 关闭IO流
            IOUtils.close(inputStream, buffer, source, snapShot);
        }
    }

    /**
     * 将缓存写入到磁盘
     *
     * @param book         BookModel
     * @param responseBody 请求封面的响应
     */
    private void writeToDiskLruCache(BookModel book, @NonNull ResponseBody responseBody) {
        Sink sink = null;
        Buffer buffer = new Buffer();
        DiskLruCache.Editor editor = null;

        // 缓存
        InputStream inputStream = responseBody.byteStream();

        try {
            editor = diskLruCache.edit(book.ctrlno);

            if (editor != null) {
                sink = editor.newSink(0);

                // 存入缓存
                byte[] fileReader = new byte[4096];
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    buffer.write(fileReader, 0, read);
                    sink.write(buffer, read);
                    buffer.clear();
                }

                buffer.close();

                sink.close();

                editor.commit();
            }
        } catch (Exception e) {
            Logger.e(e);
            if (editor != null) {
                try {
                    editor.abort();
                } catch (Exception e1) {
                    Logger.e(e1);
                }
            }
        } finally {
            IOUtils.close(inputStream, sink, buffer);
        }
    }

    // endregion

    // region 检查是否已经登录, 并尝试登录

    /**
     * 尝试进行登录并返回是否登录成功
     *
     * @param book BookModel
     * @return 是否登录成功
     */
    public Observable<Boolean> checkLogin(@NonNull BookModel book) {
        return checkLoginResponse().map(responseBodyResponse ->
                        RetrofitUtils.getUrlFromResponse(responseBodyResponse).contains("http://www.lib.szu.edu.cn/opac/user/userinfo.aspx"))
                .flatMap(aBoolean -> getBookCollectionByAjax(book, false)
                        .onErrorReturnItem(book)
                        .map(bookModel -> aBoolean));
    }

    /**
     * 检查是否需要跳转到登录界面 并尝试登录
     *
     * @return 登录后的Response
     */
    private Observable<Response<ResponseBody>> checkLoginResponse() {
        return api.checkLogin().flatMap(LoginUtil.checkLoginHandleObservableResponse(this::checkLoginResponse));
    }
    // endregion

    /**
     * 获取相关图书
     *
     * @return 是否成功
     */
    public Observable<Boolean> getRelatedBook(@NonNull BookModel book) {
        return api.getRelatedBook(book.referer, book.getCommonMap())
                .map(responseBody -> {
                    String bodyString = responseBody.string();

                    book.relatedBookList.clear();

                    for (Element element1 : Jsoup.parse(bodyString).select("li")) {
                        BookModel bookModel = new BookModel();
                        bookModel.title = element1.select("a").text();
                        bookModel.ctrlno = element1.select("a").attr("href")
                                .split("ctrlno=")[1];
                        book.relatedBookList.add(bookModel);
                    }

                    return true;
                });
    }

    /**
     * 收藏书
     *
     * @param book BookModel
     * @return 收藏的结果 0:收藏成功, 1:已经收藏, 2:收藏失败
     */
    public Observable<String> collectBook(@NonNull BookModel book) {
        return checkLogin(book).flatMap(aBoolean -> {
                    if (aBoolean) {
                        return api.collectBook(book.referer, book.getCommonMap()).map(ResponseBody::string);
                    } else {
                        return Observable.just("2");
                    }
                })
                .onErrorReturn(throwable -> {
                    NetworkUtils.errorHandle(throwable);
                    return "2";
                })
                .flatMap(s -> {
                    // 收藏成功 刷新收藏数
                    if (s.equals("0")) {
                        return getBookCollectCount(book)
                                .map(bookModel -> s)
                                .onErrorReturnItem(s);
                    } else return Observable.just(s);
                });
    }

    /**
     * 给书评分
     *
     * @param book 需要评分的书
     * @param sc   评分
     * @return 是否评分成功
     */
    public Observable<Boolean> scoreBook(@NonNull BookModel book, String sc) {
        return checkLogin(book).flatMap(aBoolean -> {
            if (aBoolean) {
                return api.scoreBook(book.referer, book.getCommonMap(), sc)
                        .map(responseBody -> responseBody.string().equals("0"))
                        .onErrorReturnItem(false);
            } else {
                return Observable.just(false);
            }
        });
    }

    // region 预约与预借

    /**
     * 获取预约信息
     *
     * @param reserveModel 预约的模型
     * @return 是否可预约
     */
    public Observable<Boolean> getReserveInformation(@NonNull ReserveModel reserveModel) {
        return api.getReserveInformation(reserveModel.getReserveQueryMap())
                .map(responseBody -> {
                    Element element = Jsoup.parse(responseBody.string());

                    // 地点
                    reserveModel.places.clear();
                    for (Element place : Objects.requireNonNull(element.getElementById("ddlfl")).children()) {
                        if (!place.val().equals("-1") && !place.val().equals("5"))
                            reserveModel.places.put(place.val(), place.text());
                    }

                    reserveModel.txtemail = Objects.requireNonNull(element.getElementById("txtemail")).val();
                    reserveModel.Button1 = Objects.requireNonNull(element.getElementById("Button1")).val();

                    return true;
                });
    }


    /**
     * 提交预约
     *
     * @param reserveModel 预约的模型
     * @return 提交预约的响应
     */
    public Observable<ResponseBody> submitReserve(@NonNull ReserveModel reserveModel) {
        return api.getReserveInformation(reserveModel.getReserveQueryMap()).flatMap(responseBody -> {
            Element element = Jsoup.parse(responseBody.string());
            reserveModel.__VIEWSTATE = Objects.requireNonNull(element.getElementById("__VIEWSTATE")).val();
            reserveModel.__VIEWSTATEGENERATOR = Objects.requireNonNull(element.getElementById("__VIEWSTATEGENERATOR")).val();
            reserveModel.__EVENTVALIDATION = Objects.requireNonNull(element.getElementById("__EVENTVALIDATION")).val();
            reserveModel.hiduid = Objects.requireNonNull(element.getElementById("hiduid")).val();

            return api.reserveBook(reserveModel.getReserveQueryMap(), reserveModel.getReserveFieldMap());
        });
    }

    /**
     * 获取预借信息
     *
     * @param reserveModel 预借的模型
     * @return 是否可预借
     */
    public Observable<Boolean> getBorrowAdvanceInformation(@NonNull ReserveModel reserveModel) {
        return api.getBorrowAdvanceInformation(reserveModel.ctrlno).map(responseBody -> {
            String bodyString = responseBody.string();
            //如果前面有弹出提示,则表示无法进行预借
            if (bodyString.contains("<script language='JavaScript' type='text/javascript'>alert('")) {
                String tmp = bodyString.split(
                        "<script language='JavaScript' type='text/javascript'>alert\\('")[1]
                        .split("'")[0];
                if (tmp.equals("当前没有可预借图书"))
                    reserveModel.borrowAdvanceFlag = ReserveModel.CAN_NOT_BORROW_ADVANCE;
                else if (tmp.equals("对不起，您的预借数量已满，请到馆阅览"))
                    reserveModel.borrowAdvanceFlag = ReserveModel.CAN_NOT_LEND_FULL;

                return false;
            } else {
                reserveModel.borrowAdvanceFlag = ReserveModel.CAN_LEND;
                Element element = Jsoup.parse(bodyString).body();
                reserveModel.__VIEWSTATE = Objects.requireNonNull(element.getElementById("__VIEWSTATE")).val();
                reserveModel.__VIEWSTATEGENERATOR = Objects.requireNonNull(
                        element.getElementById("__VIEWSTATEGENERATOR")).val();
                reserveModel.__EVENTVALIDATION = Objects.requireNonNull(
                        element.getElementById("__EVENTVALIDATION")).val();
                reserveModel.isxili = Objects.requireNonNull(element.getElementById("isxili")).val();

                // 卷期
                reserveModel.volumes.clear();
                for (Element volume : Objects.requireNonNull(element.getElementById("ddlvolume")).children()) {
                    if (!volume.val().equals("")) {
                        reserveModel.volumes.put(volume.val(), volume.text());
                    }
                }

                // 地点
                reserveModel.places.clear();
                for (Element place : Objects.requireNonNull(element.getElementById("ddlfl")).children()) {
                    if (!place.val().equals("-1") && !place.val().equals("5"))
                        reserveModel.places.put(place.val(), place.text());
                }


                reserveModel.txtemail = Objects.requireNonNull(element.getElementById("txtemail")).val();
                reserveModel.txtphone = Objects.requireNonNull(element.getElementById("txtphone")).val();
                reserveModel.Button1 = Objects.requireNonNull(element.getElementById("Button1")).val();

                return true;
            }
        });
    }

    /**
     * 提交预借
     *
     * @param reserveModel 预借的模型
     * @return 提交预借的响应
     */
    public Observable<ResponseBody> submitBorrowAdvance(@NonNull ReserveModel reserveModel) {
        return api.borrowInAdvance(reserveModel.ctrlno, reserveModel.getBorrowAdvanceFieldMap());
    }
    // endregion

}
