package com.teleostnacl.szu.library.repository;

import android.text.TextUtils;
import android.util.Xml;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.retrofit.QueryFieldMap;
import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.java.util.DateUtils;
import com.teleostnacl.common.java.util.ExecutorServiceUtils;
import com.teleostnacl.common.java.util.IOUtils;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.main.LibraryMainModel;
import com.teleostnacl.szu.library.retrofit.LibraryRetrofit;
import com.teleostnacl.szu.library.retrofit.MainApi;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class LibraryMainRepository {

    private final MainApi api = LibraryRetrofit.getInstance().getLibraryApi();

    // 储存图书馆数据所用的文件路径
    private final static String LIBRARY_PATH = ContextUtils.getContext().getFilesDir().toString()
            + File.separatorChar + "library" + File.separatorChar;

    /**
     * 获取图书馆网页的基本信息
     *
     * @return 成功
     */
    public Observable<Object> getForm() {
        return getFormResponseBody().map(responseBody -> new Object());
    }

    /**
     * @return 获取图书馆网页的基本信息, 并对是否需要登录进行判断
     */
    private Observable<ResponseBody> getFormResponseBody() {
        return api.getForm().flatMap(LoginUtil.checkLoginHandleObservable(this::getFormResponseBody));
    }

    /**
     * 获取推荐数据
     *
     * @param libraryMainModel 储存推荐原始数据的model
     * @return 是否成功
     */
    public Observable<Boolean> getHotSearchAndRankAndCollection(@NonNull LibraryMainModel libraryMainModel) {
        libraryMainModel.hotSearchList.clear();
        libraryMainModel.hotLoanList.clear();

        //实例化目录,目录不存在则创建目录
        File parentFile = new File(LIBRARY_PATH);
        if (!parentFile.exists()) parentFile.mkdirs();

        return Observable.zip(getHotSearch().onErrorReturnItem(new ArrayList<>()), getHotLoan().onErrorReturnItem(new ArrayList<>()), (strings, list) -> {
            // 去除空白内容
            for (String s : strings) {
                if (!TextUtils.isEmpty(s)) {
                    libraryMainModel.hotSearchList.add(s);
                }
            }

            for (BookModel bookModel : list) {
                if (!TextUtils.isEmpty(bookModel.title)) {
                    libraryMainModel.hotLoanList.add(bookModel);
                }
            }

            return true;
        }).subscribeOn(Schedulers.io());

    }

    /**
     * @return 获取热门搜索(共一百项)
     */
    private Observable<List<String>> getHotSearch() {
        // 储存热门搜索数据的文件
        File hotSearchFile = new File(LIBRARY_PATH + "hot_search.xml");

        //如果储存热门搜索的文件的修改时间不是今天 或者不存在热门搜索文件 则请求网络获取
        if ((hotSearchFile.exists() && !DateUtils.isToday(hotSearchFile.lastModified()))
                || !hotSearchFile.exists()) {
            return getHotSearchFromNetwork(hotSearchFile)
                    // 网络获取失败时 读本地文件
                    .onErrorReturn(throwable -> getHotSearchFromFile(hotSearchFile));
        }

        //读取文件
        else {
            return Observable.just(getHotSearchFromFile(hotSearchFile));
        }
    }

    /**
     * @return 从网络获取热门搜索的数据, 并保存的本地中
     */
    private Observable<List<String>> getHotSearchFromNetwork(File hotSearchFile) {
        List<String> list = new ArrayList<>();
        return api.getHotSearch().map(responseBody -> {
            for (Element element : Objects.requireNonNull(Jsoup.parse(responseBody.string()).getElementById("top100Inner"))
                    .select("td")) {
                //去除搜索次数
                String tmp = element.text().replaceAll("\\(\\d+\\)", "");
                //仅添加关键词
                list.add(tmp);
            }

            // 启动线程贮存文件
            ExecutorServiceUtils.executeByCached(() -> {
                //写入文件
                FileOutputStream out = null;
                try {
                    hotSearchFile.delete();
                    hotSearchFile.createNewFile();
                    XmlSerializer serializer = Xml.newSerializer();
                    out = new FileOutputStream(hotSearchFile);
                    serializer.setOutput(out, "UTF-8");
                    serializer.startDocument("UTF-8", true);
                    serializer.startTag(null, "books");
                    for (String s : list) {
                        serializer.startTag(null, "book");

                        serializer.startTag(null, "title");
                        serializer.text(s);
                        serializer.endTag(null, "title");

                        serializer.endTag(null, "book");
                    }
                    serializer.endTag(null, "books");
                    serializer.endDocument();
                    out.flush();
                } catch (Exception e) {
                    Logger.e(e);
                } finally {
                    IOUtils.close(out);
                }
            });

            return list;
        });
    }

    /**
     * @return 从本地获取热门搜索
     */
    @NonNull
    private List<String> getHotSearchFromFile(@NonNull File hotSearchFile) {
        FileInputStream fileInputStream = null;

        List<String> list = new ArrayList<>();

        try {
            fileInputStream = new FileInputStream(hotSearchFile);

            // 文件不存在 直接返回
            if (!hotSearchFile.exists()) {
                return list;
            }

            // 创建一个xml解析的工厂
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            // 获得xml解析类的引用
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(fileInputStream, "UTF-8");

            StringBuilder bookString = new StringBuilder();
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //文档开始处,先清空list
                    case XmlPullParser.START_DOCUMENT: {
                        list.clear();
                    }
                    break;

                    //获取书的细节
                    case XmlPullParser.START_TAG: {
                        if ("book".equals(parser.getName())) bookString = new StringBuilder();
                        else if ("title".equals(parser.getName()))
                            bookString.append(parser.nextText());
                    }
                    break;
                    //结束时,将book添加进list中
                    case XmlPullParser.END_TAG:
                        if ("book".equals(parser.getName())) {
                            list.add(bookString.toString());
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Logger.e(e);
            list.clear();
            hotSearchFile.delete();
        } finally {
            IOUtils.close(fileInputStream);
        }

        return list;
    }

    /**
     * @return 获取最终的热门借阅与收藏
     */
    private Observable<List<BookModel>> getHotLoan() {
        return Observable.zip(getHotCollection(), getRankResult(), getScoreRank(), (list, list2, list3) -> {
            List<BookModel> list1 = new ArrayList<>(list);
            list1.addAll(list2);
            list1.addAll(list3);
            return list1;
        });
    }

    /**
     * @return 获取热门收藏的数据(共一百项)
     */
    private Observable<List<BookModel>> getHotCollection() {
        File collectionFile = new File(LIBRARY_PATH + "collection.xml");

        return getHotLoan(collectionFile, () -> api.getHotCollection().map(responseBody -> {
            List<BookModel> list = new ArrayList<>();

            Elements elements = Jsoup.parse(responseBody.string()).select("tbody");

            for (Element element : elements.select("tr")) {
                BookModel bookModel = null;
                //标题与链接
                Element tmp = element.child(1).selectFirst("a");
                if (tmp != null) {
                    //title + author + url
                    bookModel = new BookModel(tmp.html(),
                            element.child(2).html(),
                            tmp.attr("href"));
                }
                list.add(bookModel == null ? new BookModel() : bookModel);
            }

            //写入文件
            writeHotLoanToFile(collectionFile, list);

            return list;
        })).onErrorReturnItem(new ArrayList<>());
    }

    /**
     * @return 获取借阅排行(共三百项)
     */
    private Observable<List<BookModel>> getRankResult() {
        File rankFile = new File(LIBRARY_PATH + "rank.xml");

        // 用以获取查询开始倒结束的日期
        Calendar calendar = Calendar.getInstance();

        // 获取查询结束的年月(即当前的年月)
        calendar.setTime(new Date());
        int year_end = calendar.get(Calendar.YEAR);
        int month_end = calendar.get(Calendar.MONTH) + 1;

        // 获取查询结束的年月
        calendar.add(Calendar.MONTH, -12);
        int year_start = calendar.get(Calendar.YEAR);
        int month_start = calendar.get(Calendar.MONTH) + 1;

        // 构建查询的表单
        Map<String, String> rankMap = new QueryFieldMap();
        rankMap.put("maxcount", "300");
        rankMap.put("d1", year_start + "-" + month_start + "-1");
        rankMap.put("d2", year_end + "-" + month_end + "-1");
        rankMap.put("cls", "");
        rankMap.put("queryfile", "1");
        rankMap.put("ranktypevalue", "0");

        return getHotLoan(rankFile, () -> api.getRankResult(rankMap).map(responseBody -> {
            List<BookModel> list = new ArrayList<>();

            Elements elements = Jsoup.parse(responseBody.string()).select("tbody");
            for (Element element : elements.select("tr")) {
                Element book = element.selectFirst("a");
                BookModel bookModel = null;
                if (book != null) {
                    String url = book.attr("href");

                    String[] tmp = book.html().split("／");
                    String title = tmp[0];
                    String author = "";
                    if (tmp.length > 1) author = tmp[1];

                    bookModel = new BookModel(title, author, url);
                }


                list.add(bookModel == null ? new BookModel() : bookModel);
            }

            //写入文件
            writeHotLoanToFile(rankFile, list);

            return list;
        })).onErrorReturnItem(new ArrayList<>());
    }

    /**
     * @return 获取热门评价(共一百项)
     */
    private Observable<List<BookModel>> getScoreRank() {
        File scoreRankFile = new File(LIBRARY_PATH + "score.xml");
        return getHotLoan(scoreRankFile, () -> api.getScoreRank().map(responseBody -> {
            List<BookModel> list = new ArrayList<>();

            Elements elements = Jsoup.parse(responseBody.string()).select("tbody");
            for (Element element : elements.select("tr")) {
                //标题与链接
                Element tmp = element.child(1).selectFirst("a");

                list.add(tmp != null ?
                        //title + author + url
                        new BookModel(tmp.html(), element.child(2).html(), tmp.attr("href")) :
                        new BookModel());
            }

            //写入文件
            writeHotLoanToFile(scoreRankFile, list);

            return list;
        })).onErrorReturnItem(new ArrayList<>());
    }

    /**
     * 获取热门收藏与借阅的统一处理
     *
     * @param file       需要读取的文件
     * @param observable 从网络获取数据
     * @return 获取到的热门收藏与借阅
     */
    private Observable<List<BookModel>> getHotLoan(@NonNull File file,
                                                   Supplier<Observable<List<BookModel>>> observable) {

        //如果储存热门收藏的文件的修改时间不在这个月, 或者不存在 则请求网络进行获取
        if ((file.exists() && !DateUtils.isThisMonth(file.lastModified()))
                || !file.exists()) {
            return observable.get().onErrorReturnItem(getHotLoanFromFile(file));
        }

        //读取文件
        else {
            return Observable.just(getHotLoanFromFile(file));
        }
    }

    /**
     * 从文件获取获取热门收藏与借阅的统一处理
     */
    private List<BookModel> getHotLoanFromFile(File file) {
        FileInputStream fileInputStream = null;
        List<BookModel> list = new ArrayList<>();
        try {
            if (!file.exists()) {
                return list;
            }

            fileInputStream = new FileInputStream(file);
            // 创建一个xml解析的工厂
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            // 获得xml解析类的引用
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(fileInputStream, "UTF-8");

            BookModel bookModel = new BookModel();
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //文档开始处,先清空list
                    case XmlPullParser.START_DOCUMENT:
                        list.clear();
                        break;
                    //获取书的细节
                    case XmlPullParser.START_TAG: {
                        if ("book".equals(parser.getName()))
                            bookModel = new BookModel();
                        else if ("title".equals(parser.getName()))
                            bookModel.title = parser.nextText();
                        else if ("author".equals(parser.getName()))
                            bookModel.author = parser.nextText();
                        else if ("url".equals(parser.getName()))
                            bookModel.ctrlno = parser.nextText();
                    }
                    break;
                    //结束时,将book添加进list中
                    case XmlPullParser.END_TAG: {
                        if ("book".equals(parser.getName()))
                            list.add(bookModel);
                    }
                    break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Logger.e(e);
            file.delete();
            list.clear();
        } finally {
            IOUtils.close(fileInputStream);
        }

        return list;
    }

    /**
     * 将获取到的热门借阅与收藏信息存入文件中
     *
     * @param file 需要存入的文件
     * @param list BookInfoModel List
     */
    private void writeHotLoanToFile(@NonNull File file, @NonNull List<BookModel> list) {
        ExecutorServiceUtils.executeByCached(() -> {
            FileOutputStream out = null;
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                XmlSerializer serializer = Xml.newSerializer();
                out = new FileOutputStream(file);
                serializer.setOutput(out, "UTF-8");
                serializer.startDocument("UTF-8", true);
                serializer.startTag(null, "books");
                for (BookModel bookModel : list) {

                    serializer.startTag(null, "book");

                    serializer.startTag(null, "title");
                    serializer.text(bookModel.title);
                    serializer.endTag(null, "title");

                    serializer.startTag(null, "author");
                    serializer.text(bookModel.author);
                    serializer.endTag(null, "author");

                    serializer.startTag(null, "url");
                    serializer.text(bookModel.ctrlno);
                    serializer.endTag(null, "url");

                    serializer.endTag(null, "book");
                }
                serializer.endTag(null, "books");
                serializer.endDocument();
                out.flush();
            } catch (Exception e) {
                Logger.e(e);
            } finally {
                IOUtils.close(out);
            }
        });
    }
}
