package com.teleostnacl.szu.examination.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.teleostnacl.common.android.retrofit.RetrofitUtils;
import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.common.java.util.NumberUtils;
import com.teleostnacl.szu.examination.R;
import com.teleostnacl.szu.examination.model.ExaminationDataModel;
import com.teleostnacl.szu.examination.model.ExaminationModel;
import com.teleostnacl.szu.examination.retrofit.ExaminationApi;
import com.teleostnacl.szu.examination.retrofit.ExaminationRetrofit;
import com.teleostnacl.szu.libs.ehall.repository.EHallRepository;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;


public class ExaminationRepository {
    private final ExaminationApi api = ExaminationRetrofit.getInstance().examinationApi();

    /**
     * 初始化并获取学号
     *
     * @return 是否成功
     */
    public Single<Boolean> init(ExaminationModel examinationModel) {
        return EHallRepository.getEHallApp(api).flatMap(responseBodyResponse -> {
            ResponseBody responseBody = RetrofitUtils.getBodyFromResponse(responseBodyResponse);
            if (responseBody == null) {
                return Single.just(false);
            }

            // 获取referer
            examinationModel.referer = RetrofitUtils.getUrlFromResponse(responseBodyResponse);

            // 获取学号
            examinationModel.no = responseBody.string()
                    .split("\"USERID\":\"")[1].split("\"")[0];

            return Single.zip(
                    // 获取学年学期信息
                    getXNXQ(examinationModel),
                    // 获取当前的学年学期并获取当前的数据
                    getDQXNXQ(examinationModel),
                    (o, s) -> {
                        long xqxmDm = NumberUtils.parseLong(s.replaceAll("-", ""), 0);
                        examinationModel.xnxqModels.removeIf(xnxqModel -> xnxqModel.getXQXMDm() > xqxmDm);
                        return true;
                    });
        });
    }

    /**
     * @return 获取学年学期
     */
    private Single<Object> getXNXQ(@NonNull ExaminationModel examinationModel) {
        return api.getXNXQ(examinationModel.referer, "-PX,-DM").map(xnxqModelJson -> {
            examinationModel.xnxqModels.clear();
            examinationModel.xnxqModels.addAll(xnxqModelJson.datas.xnxqcx.rows);

            return new Object();
        });
    }

    /**
     * @return 获取当前学年学期及考试安排
     */
    private Single<String> getDQXNXQ(@NonNull ExaminationModel examinationModel) {
        return api.getDQXNXQ(examinationModel.referer, "-XNXQDM", "1", "1")
                // 获取当前的学年学期
                .map(responseBody -> responseBody.string().split("\"XNXQDM\":\"")[1].split("\"")[0])
                .flatMap(s -> getExaminationData(examinationModel, s)
                        .map(examinationDataModels -> s));
    }

    /**
     * @param xnxq 指定的学年学期
     * @return 获取指定的学年学期的考试安排
     */
    public Single<List<ExaminationDataModel>> getExaminationData(@NonNull ExaminationModel examinationModel, String xnxq) {
        return Single.zip(
                api.getExaminationData(examinationModel.referer,
                        "-KSRQ", "1", xnxq),

                api.getExaminationData(examinationModel.referer,
                        examinationModel.getExaminationDataQuerySetting(xnxq)),

                api.getTime(examinationModel.referer),

                (scheduledJson, unscheduledJson, responseBody) -> {
                    // 记录服务器的时间
                    String time = responseBody.string().split("\"CURRTIME\":\"")[1].split("\"")[0];
                    long currentTime = Objects.requireNonNull(new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(time)).getTime();

                    // 从未安排的考试列表中移除已安排的考试列表, 并将未完成的添加进列表中
                    unscheduledJson.datas.wapks.rows.removeAll(scheduledJson.datas.wdksap.rows);
                    scheduledJson.datas.wdksap.rows.addAll(unscheduledJson.datas.wapks.rows);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                    // 设置每个的状态
                    for (ExaminationDataModel model : scheduledJson.datas.wdksap.rows) {
                        if (TextUtils.isEmpty(model.jasmc) || TextUtils.isEmpty(model.kssjms)) {
                            model.status = ResourcesUtils.getString(R.string.examination_lesson_status_unscheduled);
                        } else {
                            long time1 = Objects.requireNonNull(simpleDateFormat.parse(model.kssjms.split("～")[0])).getTime();
                            model.status = ResourcesUtils.getString(time1 < currentTime ? R.string.examination_lesson_status_finished : R.string.examination_lesson_status_unfinished);
                        }
                    }

                    // 储存数据
                    examinationModel.examinationDatas.put(xnxq, scheduledJson.datas.wdksap.rows);


                    return scheduledJson.datas.wdksap.rows;
                });
    }
}
