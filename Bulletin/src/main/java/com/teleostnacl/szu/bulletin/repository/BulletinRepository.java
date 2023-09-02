package com.teleostnacl.szu.bulletin.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.teleostnacl.szu.bulletin.model.Bulletin;
import com.teleostnacl.szu.bulletin.model.BulletinModel;
import com.teleostnacl.szu.bulletin.retrofit.BulletinApi;
import com.teleostnacl.szu.bulletin.retrofit.BulletinRetrofit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.ResponseBody;

public class BulletinRepository {
    private final BulletinApi bulletinApi = BulletinRetrofit.getInstance().bulletinApi();

    /**
     * 获取指定类别的公文通条目
     *
     * @param infoType 指定类别
     * @return 公文通条目
     */
    public Single<List<Bulletin>> getBulletinsByInfoType(String infoType) {
        return bulletinApi.getBulletinsByInfoType(infoType).map(responseBody -> {
            List<Bulletin> list = new ArrayList<>();

            Document document = Jsoup.parse(responseBody.byteStream(), "GB2312", "");
            Elements elements = document.getElementsByClass("tbcolor13").parents().select("tr");

            // 预期值, 大于2才进行解析
            if (elements.size() > 2) {
                // 跳过标题
                for (Element element : elements.subList(2, elements.size())) {
                    if (element.childrenSize() == 6) {
                        Bulletin bulletin = new Bulletin(element, list);
                        // 如果为讲座 则特殊处理
                        if (BulletinModel.INFO_TYPE_LECTURE.equals(infoType)) {
                            bulletin.infoType = element.child(2).text();
                            bulletin.from = element.child(4).text();
                            bulletin.time = element.child(1).text();
                        }
                    }
                }
            }

            return list;
        });
    }

    /**
     * 用于获取所有可搜索时间和可选的发文单位
     *
     * @return 是否成功
     */
    public Single<Boolean> getInfo(BulletinModel bulletinModel) {
        return bulletinApi.getInfo().map(responseBody -> {
            Element element = Jsoup.parse(responseBody.byteStream(), "GB2312", "").body();

            Element dayy = element.getElementsByAttributeValueMatching("name", "dayy").first();
            Element from_username = element.getElementsByAttributeValueMatching("name", "from_username").first();

            assert dayy != null;
            assert from_username != null;

            for (Element element1 : dayy.children()) {
                bulletinModel.dayyMap.put(element1.text(), element1.val());
            }

            for (Element element1 : from_username.children()) {
                bulletinModel.fromUsernameMap.put(element1.text(), element1.val());
            }

            return true;
        });
    }

    /**
     * 用于进行搜索公文通
     *
     * @return 公文通条目
     */
    public Single<List<Bulletin>> getBulletinsBySearch(@NonNull BulletinModel bulletinModel) {
        if (TextUtils.isEmpty(bulletinModel.searchKey)) {
            return Single.just(new ArrayList<>());
        }

        return bulletinApi.searchBulletin(bulletinModel.getSearchMap()).map(responseBody -> {
            List<Bulletin> list = new ArrayList<>();

            Document document = Jsoup.parse(responseBody.byteStream(), "GB2312", "");
            Elements elements = document.getElementsByClass("tbcolor13").parents().select("tr");

            // 预期值, 大于2才进行解析
            if (elements.size() > 2) {
                // 跳过标题
                for (Element element : elements.subList(2, elements.size())) {
                    if (element.childrenSize() == 6) {
                        new Bulletin(element, list);
                    }
                }
            }

            return list;
        });
    }

//    /**
//     * 获取公文通详细信息
//     *
//     * @param bulletin 公文通
//     * @return 是否成功
//     */
//    public Single<Boolean> getBulletinContent(@NonNull Bulletin bulletin) {
//        return bulletinApi.getBulletinContent(bulletin.id)
//                .map(responseBody -> {
//                    Document document = Jsoup.parse(responseBody.byteStream(), "GB2312", "");
//
//                    Element element = document.getElementsByAttributeValue("height", "400").first();
//
//                    if (element != null) {
//                        bulletin.content = element.html();
//
//                        bulletin.contents.clear();
//                        for (Element p : element.children()) {
//                            // </p>的处理
//                            if (p.is("p")) {
//                                BulletinContentModel contentModel = new BulletinContentModel();
//
//                                StringBuilder stringBuilder = new StringBuilder();
//
//                                String style = p.attr("style");
//
//                                // 含有缩进符, 空两格
//                                if (style.contains("text-indent")) {
//                                    stringBuilder.append("\t\t\t\t");
//                                }
//
//                                String align = p.attr("align");
//
//                                // 是否靠右显示
//                                if (style.contains("text-align:right") || "right".equals(align)) {
//                                    contentModel.rtl = true;
//                                }
//
//                                for (Element e : p.children()) {
//                                    // 是否为加粗
//                                    if (e.is("b")) {
//                                        stringBuilder.append("<b>").append(e.wholeText()).append("<\\b>");
//                                    } else {
//                                        stringBuilder.append(e.wholeText());
//                                    }
//                                }
//
//                                contentModel.content = stringBuilder.toString();
//
//                                Log.i("BulletinRepository[" + hashCode() + "]", "getBulletinContent:\n" +
//                                        "contentModel.content = " + contentModel.content);
//
//                                bulletin.contents.add(contentModel);
//                            }
//                            // </div>处理
//                            else if (p.is("div")) {
//
//                            }
//                        }
//                        return true;
//                    }
//
//                    return false;
//                });
//    }
}
