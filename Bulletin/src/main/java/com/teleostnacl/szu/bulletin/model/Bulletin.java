package com.teleostnacl.szu.bulletin.model;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.bulletin.R;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * 一条公文通
 */
public class Bulletin extends BaseObservable {
    // 类别
    public String infoType;
    // 发文单位
    public String from;
    // 公文通标题
    public String title;
    // 公文通标题的颜色
    public int titleColor = 0xFFC70E5C;
    // 公文通标题是否加粗
    public boolean titleBold = false;
    // 公文通是否置顶
    public boolean top = false;
    // 发文时间
    public String time;
    // 公文通的id
    public String id;

    public String content;

    public final List<BulletinContentModel> contents = new ArrayList<>();

    /**
     * 从网络上获取公文通条目
     *
     * @param element Element
     * @param list    带加入的公文通列表
     */
    public Bulletin(@NonNull Element element, List<Bulletin> list) {
        infoType = element.child(1).text();
        from = element.child(2).text();
        time = element.child(5).text();

        title = element.child(3).text();
        // 获取标题样式
        Element element1 = element.child(3).select("a").first();
        if (element1 != null) {
            // 获取公文通的id
            id = element1.attr("href");
            if (id.matches("view.asp\\?id=\\d+")) {
                id = id.replace("view.asp?id=", "");
                // <a/>不为空才是有效的公文通, 且能获取到id才是有效的公文通
                list.add(this);
            }

            // 判断是否置顶
            if (title.startsWith("|置顶|")) {
                top = true;
            }

            // 替换开头的字符
            title = title.replace("|置顶|", "").replace("· ", "");


            // 是否加粗
            if (element1.selectFirst("b") != null) {
                titleBold = true;
            }

            // 字体颜色
            Element color = element1.selectFirst("font");
            if (color != null) {
                switch (color.attr("color")) {
                    case "red":
                        titleColor = Color.RED;
                        break;
                    case "black":
                        titleColor = Color.BLACK;
                        break;
                }
            }
        }
    }

    public String getInfoTypeTitle() {
        return infoType + " | " + (top ? "(置顶) " : "") + title;
    }

    public Spanned getInfoTypeTime() {
        return Html.fromHtml(ResourcesUtils.getString(R.string.bulletin_main_fragment_info_type_item_time, from, time), FROM_HTML_MODE_COMPACT);
    }
}
