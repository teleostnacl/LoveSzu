package com.teleostnacl.szu.examination.model;

import com.teleostnacl.szu.libs.ehall.model.EHallModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExaminationModel extends EHallModel {

    // 记录学年学期信息
    public final List<XNXQModel> xnxqModels = new ArrayList<>();

    // 考试安排
    public final Map<String, List<ExaminationDataModel>> examinationDatas = new HashMap<>();


    /**
     * 获取未安排考试的POST时querySetting的值
     */
    public String getExaminationDataQuerySetting(String XNXQ) {
        return "[{\"name\":\"XH\",\"linkopt\":\"AND\",\"builder\":\"equal\",\"value\":\"" + no + "\"}," +
                "{\"name\":\"XNXQDM\",\"linkopt\":\"AND\",\"builder\":\"equal\",\"value\":\"" + XNXQ + "\"}]";
    }
}
