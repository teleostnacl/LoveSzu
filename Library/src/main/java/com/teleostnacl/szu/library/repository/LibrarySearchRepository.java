package com.teleostnacl.szu.library.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.library.model.detail.BookModel;
import com.teleostnacl.szu.library.model.search.SearchModel;
import com.teleostnacl.szu.library.retrofit.LibraryRetrofit;
import com.teleostnacl.szu.library.retrofit.SearchApi;
import com.teleostnacl.szu.library.viewmodel.LibrarySearchViewModel;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;

public class LibrarySearchRepository {

    private final SearchApi api = LibraryRetrofit.getInstance().getSearchApi();

    /**
     * 展示搜索建议
     * @param searchModel SearchModel
     * @return 搜索建议
     */
    public Observable<List<String>> showSearchSuggestion(@NonNull SearchModel searchModel, String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return Observable.just(new ArrayList<>());
        }

        return api.showSearchSuggestion(searchModel.searchModeModel.modeVal, keyword).map(responseBody -> {
            List<String> list = new ArrayList<>();
            for (Element element : Jsoup.parse(responseBody.string()).select("li")) {
                list.add(element.text());
            }
            return list;
        });
    }

    /**
     * 搜索
     * @param searchModel SearchModel
     * @param page 页码
     * @return 搜索结果
     */
    public Single<LibrarySearchViewModel.SearchResultModel> search(@NonNull SearchModel searchModel, int page) {
        return searchResponseBody(searchModel, page).map(responseBody -> {
            List<BookModel> list = new ArrayList<>();

            String responseBodyString = responseBody.string();

            Element body = Jsoup.parse(responseBodyString);

            // 获取图书信息
            for (Element book : body.select("tbody").select("tr")) {
                BookModel bookModel = new BookModel(book.select("td"));

                // 如果开启了可借 同时可借数量不为0, 则添加仅list中, 否则, 显示全部
                if(!searchModel.onlyShownCanLend || !bookModel.canLend.equals("0")) {
                    list.add(bookModel);
                }
            }

            // 获取到图书信息为空时, 表示搜索结果为空
            if(list.size() == 0) {
                return new LibrarySearchViewModel.SearchResultModel(list, 0, 0);
            }

            // 获取当前页数和总页数
            Elements elements = body.getElementsByClass("pd tbl");

            String tmp = elements.text();

            tmp = tmp.split("第")[1];
            String [] tmp1 = tmp.split("\\/");
            // 当前页
            int current = NumberUtils.parseInt(tmp1[0], 0);
            // 总页数
            int sum = NumberUtils.parseInt(tmp1[1].split("页")[0], 0);

            return new LibrarySearchViewModel.SearchResultModel(list, current, sum);
        });
    }

    /**
     * 搜索(判断是否需要登录)
     * @param searchModel SearchModel
     * @param page – 页码
     * @return 搜索结果
     */
    private Single<ResponseBody> searchResponseBody(@NonNull SearchModel searchModel, int page) {
        return api.search(searchModel.getSearchPath(page)).flatMap(LoginUtil.checkLoginHandleSingle(() -> searchResponseBody(searchModel, page)));
    }

    /**
     * 获取搜索范围
     * @param searchModel SearchModel
     * @param reqkey reqkey
     * @return 成功的标识符
     */
    public Observable<Boolean> getSearchFields(@NonNull SearchModel searchModel,
                                               @Nullable String reqkey) {
        return getSearchFieldsResponseBody(searchModel, reqkey)
                .flatMap(responseBody -> {
                    String s = responseBody.string();

                    // 获取reqkey
                    String reqkey1 = s.split("\r\n")[0];

                    // 为1时表示已经检索完毕, 进行获取数据
                    if(reqkey1.startsWith("1")) {
                        Element body = Jsoup.parse(s).body();

                        // 分类
                        for(Element cf : getSearchFieldsLiElements(body, "facet_cl")) {
                            String value = cf.getElementsByAttribute("href").attr("href");

                            if(value.matches(".*cf=.+")) {
                                searchModel.cfModels.add(new SearchModel.CfModel(
                                        cf.text().substring(1).trim(),
                                        value.split("cf=")[1].split("&")[0]));
                            }
                        }

                        int sum = 0;
                        // 文献类型
                        for(Element documentType : getSearchFieldsLiElements(body, "facet_wxlx")) {
                            String value = documentType.getElementsByAttribute("href").attr("href");

                            if(value.matches(".*cl=.+") && value.matches(".*dt=.+")) {
                                String title = documentType.text().substring(1).trim();
                                searchModel.documentTypeModels.add(new SearchModel.DocumentTypeModel(
                                        title,
                                        value.split("cl=")[1].split("&")[0],
                                        value.split("dt=")[1].split("&")[0]));

                                if(title.matches(".*\\(\\d+\\)")) {
                                    sum += NumberUtils.parseInt(title.split("\\(")[1].split("\\)")[0], 0);
                                }
                            }
                        }
                        searchModel.setResultSum(sum);

                        // 典藏地点
                        for(Element dept : getSearchFieldsLiElements(body, "facet_dcdd")) {
                            String value = dept.getElementsByAttribute("href").attr("href");

                            if(value.matches(".*dept=.+")) {
                                searchModel.deptModels.add(new SearchModel.DeptModel(
                                        dept.text().substring(1).trim(),
                                        value.split("dept=")[1].split("&")[0]));
                            }
                        }

                        // 出版日期
                        for (Element pyf : getSearchFieldsLiElements(body, "facet_cbrq")) {
                            String value = pyf.getElementsByAttribute("href").attr("href");
                            if(value.matches(".*pyf=.+")) {
                                searchModel.pyfModels.add(new SearchModel.PyfModel(
                                        pyf.text().substring(1).trim(),
                                        value.split("pyf=")[1].split("&")[0]));
                            }

                        }

                        return Observable.just(true);
                    } else {
                        // 等待3s, 发起下一个请求
                        Thread.sleep(3000);
                        return getSearchFields(searchModel, reqkey1.substring(1));
                    }

                });
    }

    /**
     * 用于获取搜索范围时统一处理
     * @param body Body
     * @param fieldId 搜索范围的id
     * @return 搜索范围的项
     */
    @NonNull
    private Elements getSearchFieldsLiElements(@NonNull Element body, String fieldId) {
        Element element = body.getElementById(fieldId);
        if(element == null) return new Elements();

        Element parentElement = element.parent();
        if(parentElement == null) return new Elements();

        Element parentParentElement = parentElement.parent();
        if(parentParentElement == null) return new Elements();

        return parentParentElement.select("li");
    }

    /**
     * 获取搜索范围 (判断是否需要登录)
     * @param searchModel SearchModel
     * @param reqkey reqkey
     * @return 搜索范围
     */
    private Observable<ResponseBody> getSearchFieldsResponseBody(@NonNull SearchModel searchModel,
                                                                 @Nullable String reqkey) {
        return api.getSearchFields(searchModel.getSearchFiledPath(reqkey))
                .flatMap(LoginUtil.checkLoginHandleObservable(() ->
                        getSearchFieldsResponseBody(searchModel, reqkey)));
    }
}
