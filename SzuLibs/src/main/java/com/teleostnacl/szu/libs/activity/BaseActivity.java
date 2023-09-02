package com.teleostnacl.szu.libs.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.teleostnacl.common.android.activity.ActivityUtil;
import com.teleostnacl.common.android.activity.BaseLogActivity;
import com.teleostnacl.common.android.context.ContextUtils;
import com.teleostnacl.common.android.view.edittext.IRelationView;

/**
 * 用于统一管理activity的一些行为
 */
public abstract class BaseActivity extends BaseLogActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUtil.add(this);

        //设置沉浸式导航栏
        Window window = getWindow();
        View decorView = window.getDecorView();

        int option = decorView.getSystemUiVisibility();
        option |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        //不在深色模式,使用亮色下的导航栏图标
//        if(this.getApplicationContext().getResources().getConfiguration().uiMode != 0x21)
        option |= (View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        decorView.setSystemUiVisibility(option);

        // 禁用横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    @CallSuper
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        boolean dispatched = super.dispatchTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            //点击其他位置清除edit text的焦点
            if (v instanceof EditText) {
                int rawX = (int) event.getRawX();
                int rawY = (int) event.getRawY();
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                // 触碰点不在edittext上
                if (!outRect.contains(rawX, rawY)
                        // 若edittext有关联的view 则需落点不在关联的view中
                        && !(v instanceof IRelationView &&
                        ((IRelationView) v).isInRelation(rawX, rawY))) {
                    v.clearFocus();
                    v.postDelayed(() -> {
                        // 当前焦点的非EditText 则需要隐藏输入法
                        if (!(getWindow().getDecorView().findFocus() instanceof EditText)) {
                            ContextUtils.hideSoftInputFromWindow(v);
                        }
                    }, 100);
                }
            }
        }

        return dispatched;
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        super.onDestroy();

        ActivityUtil.remove(this);
    }
}

