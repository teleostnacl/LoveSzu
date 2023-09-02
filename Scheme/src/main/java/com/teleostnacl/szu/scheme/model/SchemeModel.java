package com.teleostnacl.szu.scheme.model;

import com.teleostnacl.common.android.retrofit.QueryFieldMap;
import com.teleostnacl.szu.libs.ehall.model.EHallModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SchemeModel extends EHallModel {
    // 记录全部可查询年级的List
    public final List<YearModel> yearModels = new ArrayList<>();

    public Map<String, String> getAllSchemesByYearMap(int yearIndex, int pageNumber) {
        Map<String, String> map = new QueryFieldMap();

        // 全选
        if (yearIndex == 0) {
            map.put("querySetting", "[{\"name\":\"FAZTDM\",\"caption\":\"\",\"builder\":\"equal\",\"linkOpt\":\"AND\",\"value\":\"99\"}]");
        }
        // 指定年
        else {
            map.put("querySetting", "[{\"name\":\"NJDM\",\"caption\":\"年级\",\"linkOpt\":\"AND\",\"builderList\":\"cbl_String\",\"builder\":\"equal\"," +
                    "\"value\":\"" + yearModels.get(yearIndex - 1).id +
                    "\",\"value_display\":\"" + yearModels.get(yearIndex - 1).name + "\"}," +
                    "{\"name\":\"FAZTDM\",\"caption\":\"\",\"builder\":\"equal\",\"linkOpt\":\"AND\",\"value\":\"99\"}]");
        }

        map.put("*order", "-NJDM, +DWDM, +ZYDM");
        map.put("pageSize", "12");
        map.put("pageNumber", String.valueOf(pageNumber));

        return map;
    }
}
