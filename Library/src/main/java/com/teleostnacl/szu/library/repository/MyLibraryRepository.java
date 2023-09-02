package com.teleostnacl.szu.library.repository;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.paging.model.PagingModel;
import com.teleostnacl.common.android.retrofit.QueryFieldMap;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.library.model.user.BorrowHistoryModel;
import com.teleostnacl.szu.library.model.user.CollectionModel;
import com.teleostnacl.szu.library.model.user.CurrentBorrowModel;
import com.teleostnacl.szu.library.model.user.MyLibraryModel;
import com.teleostnacl.szu.library.model.user.MyLibraryUserInfoModel;
import com.teleostnacl.szu.library.model.user.ReservationModel;
import com.teleostnacl.szu.library.retrofit.LibraryRetrofit;
import com.teleostnacl.szu.library.retrofit.MyLibraryApi;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.ResponseBody;

public class MyLibraryRepository {
    private final MyLibraryApi api = LibraryRetrofit.getInstance().getMyLibraryApi();

    /**
     * 获取个人信息
     *
     * @param myLibraryModel MyLibraryModel
     * @return 是否获取成功
     */
    public Single<Boolean> getUserInfo(MyLibraryModel myLibraryModel) {
        return getUserInfoResponseBody().map(responseBody -> {
            Element body = Jsoup.parse(responseBody.string()).body();

            myLibraryModel.userInfoModel = new MyLibraryUserInfoModel(
                    Objects.requireNonNull(body.getElementById("userInfoContent"))
                            .getElementsByClass("inforight"));

            return true;
        });
    }

    /**
     * 修改联系方式
     *
     * @return 是否成功
     */
    public Observable<Boolean> modifyUserInfo(@NonNull MyLibraryUserInfoModel userInfoModel) {
        return api.getModifyUserInfo().flatMap((Function<ResponseBody, ObservableSource<Boolean>>) responseBody -> {
            Element body = Jsoup.parse(responseBody.string()).body();

            // 获取修改联系方式时的表单信息
            userInfoModel.__VIEWSTATE =
                    Objects.requireNonNull(body.getElementById("__VIEWSTATE")).val();
            userInfoModel.__EVENTVALIDATION =
                    Objects.requireNonNull(body.getElementById("__EVENTVALIDATION")).val();
            userInfoModel.__VIEWSTATEGENERATOR =
                    Objects.requireNonNull(body.getElementById("__VIEWSTATEGENERATOR")).val();

            return api.modifyUserInfo(userInfoModel.getFieldMap()).map(responseBody1 ->
                    Objects.requireNonNull(Jsoup.parse(responseBody1.string()).body().getElementById("modifySuccess"))
                            .text()
                            .equals("个人信息修改成功，点击返回"));
        }).onErrorReturnItem(false);
    }

    /**
     * 获取个人信息 并检查是否需要登录
     *
     * @return 个人信息的请求结果
     */
    private Single<ResponseBody> getUserInfoResponseBody() {
        return api.getUserInfo().flatMap(LoginUtil.checkLoginHandleSingle(this::getUserInfoResponseBody));
    }

    /**
     * 获取当前借阅的图书
     *
     * @return 当前借阅的图书
     */
    public Single<List<CurrentBorrowModel>> getCurrentBorrowedBooks() {
        return api.getCurrentBorrowedBooks().map(responseBody -> {
            List<CurrentBorrowModel> list = new ArrayList<>();
            for (Element e : Objects.requireNonNull(Jsoup.parse(responseBody.string()).body().getElementById("borrowedcontent"))
                    .select("tbody").select("tr")) {
                list.add(new CurrentBorrowModel(e));
            }
            return list;
        });
    }

    /**
     * 获取预约信息
     *
     * @return 预约的图书
     */
    public Single<List<ReservationModel>> getReservationModels() {
        return api.getReservation().map(responseBody -> {

            Element body = Jsoup.parse(responseBody.string()).body();

            //创建post的表单
            Map<String, String> map = new QueryFieldMap();
            map.put("__VIEWSTATE", Objects.requireNonNull(body.getElementById("__VIEWSTATE")).val());
            map.put("__VIEWSTATEGENERATOR", Objects.requireNonNull(body.getElementById("__VIEWSTATEGENERATOR")).val());

            Element e1 = body.getElementById("__EVENTVALIDATION");
            if (e1 != null) {
                map.put("__EVENTVALIDATION", e1.val());
            }

            Element e2 = body.getElementById("ctl00_cpRight_btnCan");
            if (e2 != null) {
                map.put("ctl00$cpRight$btnCan", e2.val());
            }

            List<ReservationModel> list = new ArrayList<>();

            for (Element element : body.select("tbody").select("tr")) {
                list.add(new ReservationModel(element, map));
            }
            return list;
        });
    }

    /**
     * 取消预约
     *
     * @param reservationModel 需取消预约的图书模型
     * @return 是否成功
     */
    public Observable<Boolean> cancelReserve(@NonNull ReservationModel reservationModel) {
        return api.cancelReserve(reservationModel.getCancelMap()).map(responseBody -> {
            String s = responseBody.string();
            return s.contains("成功取消预约");
        }).onErrorReturnItem(false);
    }

    /**
     * 获取预借信息
     *
     * @return 预借的图书
     */
    public Single<PagingModel<ReservationModel>> getRequestModels(int page) {
        return api.getBorrowAdvance(page).map(responseBody -> {
            PagingModel<ReservationModel> model = new PagingModel<>();

            Element element = Jsoup.parse(responseBody.string()).body();

            Element current = element.getElementById("ctl00_cpRight_Pagination2_dplbl2");
            Element sum = element.getElementById("ctl00_cpRight_Pagination2_gplbl2");

            if (current != null && sum != null) {
                //记录当前页数
                model.current = NumberUtils.parseInt(current.text(), 0);
                //记录总页数
                model.pages = NumberUtils.parseInt(sum.text(), 0);
            }

            for (Element e : element.select("tbody").select("tr")) {
                model.models.add(new ReservationModel(e));
            }

            return model;
        });
    }

    /**
     * 取消预借
     *
     * @param reservationModel 需取消预借的图书模型
     * @return 是否成功
     */
    public Observable<Boolean> cancelBorrowAdvance(@NonNull ReservationModel reservationModel) {
        return api.cancelBorrowAdvance(reservationModel.borrowAdvanceNo, "")
                .map(responseBody -> responseBody.string().equals("1"))
                .onErrorReturnItem(false);
    }

    /**
     * 获取指定页的借阅的图书
     *
     * @param page 指定页
     * @return 指定页的借阅的图书
     */
    public Single<PagingModel<BorrowHistoryModel>> getBorrowHistoryModels(int page) {
        return api.getBookBorrowHistory(page).map(responseBody -> {
            PagingModel<BorrowHistoryModel> model = new PagingModel<>();

            Element element = Jsoup.parse(responseBody.string()).body();
            Element current = element.getElementById("ctl00_cpRight_Pagination1_dplbl2");
            Element sum = element.getElementById("ctl00_cpRight_Pagination1_gplbl2");

            if (current != null && sum != null) {
                //记录当前页数
                model.current = NumberUtils.parseInt(current.text(), 0);
                //记录总页数
                model.pages = NumberUtils.parseInt(sum.text(), 0);
            }

            //获取数据
            for (Element e : element.select("tbody").select("tr")) {
                model.models.add(new BorrowHistoryModel(e));
            }

            return model;
        });
    }

    /**
     * 获取指定页的收藏图书
     *
     * @param page 指定页
     * @return 指定页收藏图书
     */
    public Single<PagingModel<CollectionModel>> getCollectionModels(int page) {
        return api.getCollection(page).map(responseBody -> {
            PagingModel<CollectionModel> model = new PagingModel<>();

            Element element = Jsoup.parse(responseBody.string()).body();

            Element current = element.getElementById("ctl00_cpRight_Pagination2_dplbl2");
            Element sum = element.getElementById("ctl00_cpRight_Pagination2_gplbl2");

            if (current != null && sum != null) {
                //记录当前页数
                model.current = NumberUtils.parseInt(current.text(), 0);
                //记录总页数
                model.pages = NumberUtils.parseInt(sum.text(), 0);
            }

            //获取数据
            for (Element e : element.select("tbody").select("tr")) {
                model.models.add(new CollectionModel(e));
            }

            return model;
        });
    }

    /**
     * 取消收藏图书
     *
     * @param collectionModel 需取消收藏的图书
     * @return 是否成功
     */
    public Observable<Boolean> cancelCollectBook(@NonNull CollectionModel collectionModel) {
        return api.cancelCollectBook(collectionModel.ctrlno, "")
                .map(responseBody -> responseBody.string().equals("1"))
                .onErrorReturnItem(false);
    }
}
