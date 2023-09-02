package com.teleostnacl.szu.libs;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.android.context.BaseApplication;
import com.teleostnacl.common.android.context.ToastUtils;
import com.teleostnacl.szu.libs.crash.CrashHandler;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class MyApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        RxJavaPlugins.setErrorHandler(throwable -> {
            ToastUtils.makeToast(com.teleostnacl.common.android.R.string.unknown_error);
            Logger.e(throwable);
        });
        CrashHandler.init();
    }
}
