package com.teleostnacl.szu.libs.ehall.repository;

import androidx.annotation.NonNull;

import com.teleostnacl.szu.libs.ehall.retrofit.EHallApi;
import com.teleostnacl.szu.libs.utils.activity.LoginUtil;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * 深圳大学网上办事大厅公共repository
 */
public final class EHallRepository {
    /**
     * 打开EHall某个功能模块 并进行判断是否需要进行登录
     *
     * @param api EHall的功能api
     * @return 登录成功后的ResponseBody
     */
    public static Single<Response<ResponseBody>> getEHallApp(@NonNull EHallApi api) {
        return api.get(api.getAppId()).flatMap(LoginUtil.checkLoginHandleSingleResponse(() -> getEHallApp(api)));
    }

}
