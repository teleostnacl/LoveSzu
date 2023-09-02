package com.teleostnacl.szu.libs.crash;

import android.os.Bundle;
import android.widget.TextView;

import com.teleostnacl.common.android.context.PmAmUtils;
import com.teleostnacl.szu.libs.R;
import com.teleostnacl.szu.libs.activity.BaseActivity;

public class CrashActivity extends BaseActivity {

    public static final String ARG_CRASH_MESSAGE = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        setSupportActionBar(findViewById(R.id.tool_bar));

        TextView textView = findViewById(R.id.text_view);

        // 设置崩溃日志
        textView.setText(getIntent().getStringExtra(ARG_CRASH_MESSAGE));
    }

    /**
     * 推出应用界面时 杀死进程
     */
    @Override
    protected void onStop() {
        finishAndRemoveTask();
        //杀死该应用进程
        PmAmUtils.killSelf();

        super.onStop();
    }
}