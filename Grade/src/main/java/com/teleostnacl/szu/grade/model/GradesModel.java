package com.teleostnacl.szu.grade.model;

import com.teleostnacl.szu.libs.ehall.model.EHallModel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用于记录成绩查询的参数
 */
public class GradesModel extends EHallModel {
    // 全部成绩
    public final Map<String, SemesterGradesModel> grades = new LinkedHashMap<>();
}
