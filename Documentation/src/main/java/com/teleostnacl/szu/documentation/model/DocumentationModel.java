package com.teleostnacl.szu.documentation.model;

import com.teleostnacl.common.android.retrofit.QueryFieldMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentationModel {
    public String userId;

    public String name;

    // 可下载的证明文件列表
    public final List<ItemModel> itemModels = new ArrayList<>();

    // 历史下载的证明文件列表
    public final Map<String, HistoryFileModel> historyFileModels = new HashMap<>();

    /**
     * @return 获取可下载文件列表的请求Map
     */
    public Map<String, String> getItemListMap() {
        Map<String, String> map = new QueryFieldMap();

        map.put("name", "");
        map.put("state", "1");
        map.put("tagId", "");
        map.put("rows", "5000");
        map.put("page", "1");
        map.put("", "");

        return map;
    }

    /**
     * @return 获取已下载的文件列表的请求Map
     */
    public Map<String, String> getHistoryFileListMap() {
        Map<String, String> map = new QueryFieldMap();

        map.put("schoolCode", "10590");
        map.put("type", "1");
        map.put("userId", userId);
        map.put("itemName", "");

        return map;
    }

    /**
     * 获取文件下载链接时请求的Map
     *
     * @param itemId 下载的文件id
     * @return 请求的Map
     */
    public Map<String, String> getDownloadLinkMap(String itemId, String fileUrl) {
        Map<String, String> map = new QueryFieldMap();
        map.put("itemId", itemId);
        map.put("copies", "1");
        map.put("schoolCode", "10590");
        map.put("orderType", "5");
        map.put("apiType", "2");
        map.put("reportFormValue", "");
        map.put("takeType", "2");
        map.put("takeCodeLength", "10");
        map.put("fileUrl", fileUrl);
        map.put("", "");

        return map;
    }
}
