package com.teleostnacl.common.android.view.loading;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.java.util.NumberUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * PopupWindow形式的加载动画
 */
public class LoadingPopupWindow extends PopupWindow {

    private final String TAG = "LoadingPopupWindow[" + hashCode() + "]";

    private final View parentView;

    private final LoadingView rootView;

    private boolean showLog = false;

    // 控制是否展示
    private boolean isShow;

    @SuppressLint("InflateParams")
    public LoadingPopupWindow(@NonNull View parentView) {
        this(parentView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @SuppressLint("InflateParams")
    public LoadingPopupWindow(@NonNull View parentView, int width, int height) {
        super(new LoadingView(parentView.getContext()), width, height);
        this.parentView = parentView;
        this.rootView = (LoadingView) getContentView();

        rootView.setLoadingPopupWindow(this);
    }

    public TextView getTextView() {
        return rootView.getTextView();
    }

    public LinearLayout getRootView() {
        return rootView;
    }

    public LoadingPopupWindow setTitle(String title) {
        rootView.setTitle(title);
        return this;
    }

    public LoadingPopupWindow setTitle(@StringRes int resId) {
        rootView.setTitle(resId);
        return this;
    }

    public String getTitle() {
        return rootView.getTitle();
    }

    public LoadingPopupWindow setBackground(Drawable background) {
        rootView.setBackground(background);
        return this;
    }

    public LoadingPopupWindow setBackgroundColor(@ColorInt int color) {
        rootView.setBackgroundColor(color);
        return this;
    }

    public void postShow() {
        log("postShow");
        isShow = true;
        parentView.post(() -> {
            if (isShow && !isShowing()) {
                show();
            }
        });
    }

    public void show() {
        isShow = true;
        log("show!");
        showAtLocation(parentView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void dismiss() {
        log("dismiss!");
        isShow = false;
        super.dismiss();
    }

    private void log(String s) {
        if (showLog) Logger.v(TAG, s);
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public static class LoadingView extends LinearLayout {

        private LoadingPopupWindow loadingPopupWindow;

        private final TextView textView;
        private final AnimationView animationView;

        public LoadingView(Context context) {
            this(context, null);
        }

        public LoadingView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public LoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);

            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            setGravity(Gravity.CENTER);
            setOrientation(HORIZONTAL);

            animationView = new AnimationView(context);
            animationView.setLayoutParams(new LinearLayout.LayoutParams(
                    LoadingViewUtils.ANIMATION_VIEW_LENGTH, LoadingViewUtils.ANIMATION_VIEW_LENGTH));

            textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setPadding(LoadingViewUtils.TEXT_VIEW_PADDING, 0,
                    LoadingViewUtils.TEXT_VIEW_PADDING, 0);
            textView.setTextSize(LoadingViewUtils.TEXT_SIZE);
            textView.setTypeface(Typeface.DEFAULT_BOLD);

            addView(animationView);
            addView(textView);
        }

        public TextView getTextView() {
            return textView;
        }

        public LinearLayout getRootView() {
            return this;
        }

        public LoadingView setTitle(String title) {
            textView.setText(title);
            return this;
        }

        public LoadingView setTitle(@StringRes int resId) {
            textView.setText(resId);
            return this;
        }

        public String getTitle() {
            return textView.getText().toString();
        }

        public void setLoadingPopupWindow(LoadingPopupWindow loadingPopupWindow) {
            this.loadingPopupWindow = loadingPopupWindow;
            animationView.setLoadingPopupWindow(loadingPopupWindow);
        }

        @Override
        @CallSuper
        protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
            super.onVisibilityChanged(changedView, visibility);

            if (loadingPopupWindow != null) {
                loadingPopupWindow.log("onVisibilityChanged:\n" +
                        " visibility = " + visibility);
                loadingPopupWindow.log("onVisibilityChanged:\n" +
                        " getVisibility() = " + getVisibility());
            }

            if (visibility == VISIBLE && getVisibility() == VISIBLE) {
                animationView.resume();
            } else {
                animationView.pause();
            }
        }
    }

    public static class AnimationView extends SurfaceView implements SurfaceHolder.Callback {

        private LoadingPopupWindow loadingPopupWindow;

        private final SurfaceHolder mSurfaceHolder;

        // 画笔
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 定义每只画笔绘制的角度
        private final float[] paintAngles = new float[LoadingViewUtils.circleNum];
        // 定义每个圈圈的颜色
        private final int[] circleColors = new int[LoadingViewUtils.circleNum];
        // 定义每个圈圈可见性
        private final boolean[] circleVisible = new boolean[LoadingViewUtils.circleNum];
        // 定义加载圈的半径
        private float radius;
        // 记录视图高宽的一半值
        private float height, width;

        // 用于播放动画的定时器
        private final Timer timer = new Timer();

        // 执行动画的任务
        private transient TimerTask timerTask;

        // 是否处于播放状态
        private boolean running = false;

        // 显示顺序
        private boolean reverse = true;

        public AnimationView(Context context) {
            this(context, null);
        }

        public AnimationView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public AnimationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);

            mSurfaceHolder = getHolder();
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
            setZOrderOnTop(true);

            setKeepScreenOn(true);

            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setAntiAlias(true);
            paint.setDither(true);
        }

        public void setLoadingPopupWindow(LoadingPopupWindow loadingPopupWindow) {
            this.loadingPopupWindow = loadingPopupWindow;
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            log("AnimationView[" + hashCode() + "]: surfaceCreated");
            // 初始化绘制动画的初值
            List<Integer> list = NumberUtils.getDifferentRandom(
                    0, LoadingViewUtils.getColors().length, LoadingViewUtils.circleNum);
            for (int i = 0; i < LoadingViewUtils.circleNum; i++) {
                paintAngles[i] = 0;
                circleColors[i] = LoadingViewUtils.getColors()[list.get(i)];
                circleVisible[i] = false;
            }

            startTimer();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            log("AnimationView[" + hashCode() + "]: surfaceChanged");
            this.height = (float) getHeight() / 2;
            this.width = (float) getWidth() / 2;
            radius = Math.min(this.height, this.width) - LoadingViewUtils.circleRadius;
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            log("AnimationView[" + hashCode() + "]: surfaceDestroyed");
            cancelTimer();
        }

        public void startTimer() {
            log("AnimationView[" + hashCode() + "]: startTimer");
            cancelTimer();

            resume();
            timer.scheduleAtFixedRate(timerTask = new TimerTask() {
                // 记录是一次动画中的第几次
                private int i = 0;

                @Override
                public void run() {
                    // 处于播放状态才进行绘制
                    if (running) {
                        cal(i++ / (LoadingViewUtils.rate * LoadingViewUtils.animationRate));

                        reverse = i <= LoadingViewUtils.rate;

                        if (i > LoadingViewUtils.rate * LoadingViewUtils.animationRate * (1 + LoadingViewUtils.lastTimeDelta)) {
                            i = 0;
                        }
                    } else {
                        i = 0;
                    }
                }
            }, 0, (long) (1000 * LoadingViewUtils.frequency));
        }

        public void cancelTimer() {
            log("AnimationView[" + hashCode() + "]: cancelTimer");
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }

            pause();

            Canvas mCanvas = null;
            try {
                mCanvas = mSurfaceHolder.lockCanvas();

                if (mCanvas != null) {
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                }
            } catch (Exception e) {
                Logger.e(e);
            } finally {
                if (mCanvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }
        }

        /**
         * 根据当前所在的百分比 计算每个圆圈所在位置对应的角度
         *
         * @param i 当前动画进行的百分比
         */
        private void cal(float i) {
            for (int j = 0; j < paintAngles.length; j++) {
                float T = 1 + i - LoadingViewUtils.lastTimeDelta / paintAngles.length * j;
                // 落在[1, 2]范围内
                if (T >= 1 && T <= 2) {
                    paintAngles[j] = 360 * LoadingViewUtils.interpolator.getInterpolation(T);
                    circleVisible[j] = true;
                } else if (T < 1) {
                    paintAngles[j] = 0;
                    circleVisible[j] = false;
                } else if (T > 2) {
                    paintAngles[j] = 360;
                    circleVisible[j] = true;
                }
            }

            draw();
        }

        /**
         * 计算给定角度所在的坐标值
         *
         * @param angle 角度,单位为角度值
         * @return 坐标值
         */
        @NonNull
        private float[] calLocation(float angle) {
            float[] location = new float[2];

            location[0] = radius * ((float) Math.cos((angle + 90) * Math.PI / 180)) + width;
            location[1] = -radius * ((float) Math.sin((angle + 90) * Math.PI / 180)) + height;

            return location;
        }

        /**
         * 绘制图形
         */
        private void draw() {
            Canvas mCanvas = null;
            try {
                mCanvas = mSurfaceHolder.lockCanvas();

                if (mCanvas != null) {
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    //绘制六个圆圈
                    if (reverse) {
                        for (int i = LoadingViewUtils.circleNum - 1; i >= 0; i--) {
                            //可见则进行绘制
                            if (circleVisible[i]) {
                                paint.setColor(circleColors[i]);
                                float[] location = calLocation(paintAngles[i]);
                                mCanvas.drawCircle(location[0], location[1], LoadingViewUtils.circleRadius, paint);
                            }
                        }
                    } else {
                        for (int i = 0; i < LoadingViewUtils.circleNum; i++) {
                            //可见则进行绘制
                            if (circleVisible[i]) {
                                paint.setColor(circleColors[i]);
                                float[] location = calLocation(paintAngles[i]);
                                mCanvas.drawCircle(location[0], location[1], LoadingViewUtils.circleRadius, paint);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.e(e);
            } finally {
                if (mCanvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }
        }

        /**
         * 动画暂停
         */
        public void pause() {
            running = false;
        }

        /**
         * 动画继续
         */
        public void resume() {
            running = true;
        }

        private void log(String s) {
            if (loadingPopupWindow != null) loadingPopupWindow.log(s);
        }
    }
}
