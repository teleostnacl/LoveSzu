package com.teleostnacl.szu.scheme.repository;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.retrofit.RetrofitUtils;
import com.teleostnacl.szu.libs.ehall.repository.EHallRepository;
import com.teleostnacl.szu.scheme.json.SchemeJson;
import com.teleostnacl.szu.scheme.model.SchemeDetailGroup;
import com.teleostnacl.szu.scheme.model.SchemeDetailLesson;
import com.teleostnacl.szu.scheme.model.SchemeDetailModel;
import com.teleostnacl.szu.scheme.model.SchemeModel;
import com.teleostnacl.szu.scheme.retrofit.SchemeApi;
import com.teleostnacl.szu.scheme.retrofit.SchemeRetrofit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;

public class SchemeRepository {
    private final SchemeApi schemeApi = SchemeRetrofit.getInstance().schemeApi();

    /**
     * 获取所有可查询培养方案的年份
     *
     * @return 是否成功
     */
    public Single<Boolean> getAllYear(SchemeModel schemeModel) {
        return EHallRepository.getEHallApp(schemeApi).flatMap(responseBodyResponse -> {
            // 获取referer
            schemeModel.referer = RetrofitUtils.getUrlFromResponse(responseBodyResponse);

            // 获取所有年份
            return schemeApi.getAllYears(schemeModel.referer);
        }).map(yearJson -> {
            schemeModel.yearModels.clear();
            schemeModel.yearModels.addAll(yearJson.datas.code.rows);

            return schemeModel.yearModels.size() != 0;
        });
    }

    /**
     * 获取指定年级 指定页面 的 培养方案列表
     *
     * @param yearIndex  指定年级的索引
     * @param pageNumber 指定页面
     * @return 培养方案列表
     */
    public Single<SchemeJson> getAllSchemeByYear(@NonNull SchemeModel schemeModel, int yearIndex, int pageNumber) {
        return schemeApi.getAllSchemesByYear(schemeModel.referer, schemeModel.getAllSchemesByYearMap(yearIndex, pageNumber));
    }

    /**
     * 获取指定培养方案的所有课程模块 课程组 和所有课程
     */
    public Single<Boolean> getSchemeDetailGroups(@NonNull SchemeModel schemeModel, @NonNull SchemeDetailModel schemeDetailModel) {
        return schemeApi.getSchemeDetailGroups(schemeModel.referer, schemeDetailModel.pyfadm)
                // 分类培养方案的课程组到对应的课程模块
                .map(schemeDetailGroupJson -> {
                    schemeDetailModel.schemeDetailModule.clear();

                    List<SchemeDetailGroup> list = schemeDetailGroupJson.datas.kzcx.rows;

                    // 记录课程模块的id和课程模块
                    Map<String, SchemeDetailGroup> KZHModule = new HashMap<>();

                    // 课程组别
                    List<SchemeDetailGroup> group = new ArrayList<>();

                    // 获取所有课程模块
                    for (SchemeDetailGroup model : list) {
                        if (model.kzlxdmDisplay.equals("平台")) {
                            schemeDetailModel.schemeDetailModule.add(model);
                            KZHModule.put(model.kzh, model);
                        } else {
                            group.add(model);
                        }
                    }

                    Map<String, SchemeDetailGroup> groupMap = new HashMap<>();

                    // 将课程组别分类到课程模块中
                    for (SchemeDetailGroup model : group) {

                        groupMap.put(model.kzh, model);

                        KZHModule.computeIfPresent(model.fkzh, (s, schemeDetailGroup) -> {
                            schemeDetailGroup.schemeDetailGroups.add(model);
                            return schemeDetailGroup;
                        });
                    }

                    return groupMap;
                })
                // 获取该方案的所有课程 并分到正确的组别
                .flatMap(schemeDetailGroups -> schemeApi.getSchemeDetailLessons(schemeModel.referer, schemeDetailModel.pyfadm).map(
                        schemeDetailGroupJson -> {
                            // 遍历所有课程添 并分到正确的组别
                            for (SchemeDetailLesson lesson : schemeDetailGroupJson.datas.kzkccx.rows) {
                                schemeDetailGroups.computeIfPresent(lesson.kzh, (s, schemeDetailGroup) -> {
                                    schemeDetailGroup.schemeDetailLessons.add(lesson);
                                    return schemeDetailGroup;
                                });
                            }

                            return true;
                        }
                ));
    }
}
