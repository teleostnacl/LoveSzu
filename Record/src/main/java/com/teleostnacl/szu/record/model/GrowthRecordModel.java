package com.teleostnacl.szu.record.model;

import android.text.Spanned;

import com.teleostnacl.common.android.utils.HtmlUtils;
import com.teleostnacl.szu.libs.ehall.model.EHallModel;
import com.teleostnacl.szu.record.R;

import java.util.ArrayList;
import java.util.List;

public class GrowthRecordModel extends EHallModel {

    // 全部成绩专业排名
    public int pm;
    // 全部成绩的gpa
    public float gpa;

    public List<SemesterModel> semesterModelList = new ArrayList<>();

    public Spanned getGPAAndPm() {
        return HtmlUtils.fromHtml(R.string.growth_record_gpa_and_pm, gpa, pm);
    }
}
