package com.teleostnacl.common.android.context;

import android.app.Application;

public abstract class BaseApplication extends Application {
    private static BaseApplication app;

    public BaseApplication() {app = this;}

    protected static BaseApplication getInstance() {return app;}
}
