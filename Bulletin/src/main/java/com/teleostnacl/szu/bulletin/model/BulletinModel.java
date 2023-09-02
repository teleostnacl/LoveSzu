package com.teleostnacl.szu.bulletin.model;

import com.teleostnacl.common.android.retrofit.QueryFieldMap;
import com.teleostnacl.common.java.util.StringUtils;
import com.teleostnacl.common.android.utils.UrlUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class BulletinModel {
    // region 获取10天内所查询类别的公文通 类别 infoType
    // 全部
    public static final String INFO_TYPE_ALL = "";
    // 学术讲座
    public static final String INFO_TYPE_LECTURE = "%BD%B2%D7%F9";
    // 教务教学
    public static final String INFO_TYPE_EDUCTION = "%BD%CC%CE%F1";
    // 科研活动
    public static final String INFO_TYPE_SCIENTIFIC = "%BF%C6%D1%D0";
    // 党务行政
    public static final String INFO_TYPE_ADMINISTRATION = "%D0%D0%D5%FE";
    // 学生工作
    public static final String INFO_TYPE_STUDENT = "%D1%A7%B9%A4";
    // 校园生活
    public static final String INFO_TYPE_LIVING = "%C9%FA%BB%EE";
    // endregion

    // 搜索时间
    public Map<String, String> dayyMap = new LinkedHashMap<>();

    // 发文单位
    public Map<String, String> fromUsernameMap = new LinkedHashMap<>();

    // 搜索关键字
    public String searchKey;

    // 所选择的搜索时间
    public String dayy;

    // 所选择的发文单位
    public String fromUsername;

    /**
     * @return 搜索公文通所使用的FieldMap
     */
    public Map<String, String> getSearchMap() {
        Map<String, String> map = new QueryFieldMap();

        map.put("dayy", dayy);
        map.put("from_username", fromUsername);

        map.put("keyword", UrlUtils.encode(StringUtils.getOrBlank(searchKey), "GBK"));
        // 搜索经UrlEncode, 编码为GBK
        map.put("searchb1", "%CB%D1%CB%F7");

        return map;
    }

}
