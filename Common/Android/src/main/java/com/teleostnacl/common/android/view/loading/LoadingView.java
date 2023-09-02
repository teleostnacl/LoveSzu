package com.teleostnacl.common.android.view.loading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.teleostnacl.common.android.R;
import com.teleostnacl.common.android.log.Logger;
import com.teleostnacl.common.java.util.NumberUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LoadingView extends LinearLayout {

    private final String TAG = LoadingView.class.getSimpleName() + "[" + hashCode() + "]";

    private final TextView textView;

    private final AnimationView animationView;

    private boolean showLog = false;

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

        setGravity(Gravity.CENTER);
        setClipChildren(false);
        setClipToPadding(false);
        setClipToOutline(false);

        animationView = new AnimationView(context);
        animationView.setLayoutParams(new LinearLayout.LayoutParams(LoadingViewUtils.ANIMATION_VIEW_LENGTH, LoadingViewUtils.ANIMATION_VIEW_LENGTH));
        animationView.setLoadingView(this);

        textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setPadding(LoadingViewUtils.TEXT_VIEW_PADDING, 0, LoadingViewUtils.TEXT_VIEW_PADDING, 0);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        addView(animationView);
        addView(textView);

        // 设置标题
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        String content = typedArray.getString(R.styleable.LoadingView_text);

        if (typedArray.hasValue(R.styleable.LoadingView_android_textSize)) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, typedArray.getDimensionPixelSize(
                    R.styleable.LoadingView_android_textSize, 0));
        } else {
            textView.setTextSize(LoadingViewUtils.TEXT_SIZE);
        }
        setTitle(content);
        typedArray.recycle();
    }

    @Override
    @CallSuper
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        log("onVisibilityChanged:\n" + " visibility = " + visibility);
        log("onVisibilityChanged:\n" + " getVisibility() = " + getVisibility());

        if (visibility == VISIBLE && getVisibility() == VISIBLE) {
            animationView.resume();
        } else {
            animationView.pause();
        }
    }

    @Override
    public boolean isInEditMode() {
        return true;
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

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    private void log(String s) {
        if (showLog) Logger.v(TAG, s);
    }

    public static class AnimationView extends View {
        private LoadingView loadingView;

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
        // 视图的高宽
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

        public AnimationView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public AnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public AnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);

            setKeepScreenOn(true);

            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setAntiAlias(true);
            paint.setDither(true);

            // 初始化绘制动画的初值
            List<Integer> list = NumberUtils.getDifferentRandom(
                    0, LoadingViewUtils.getColors().length, LoadingViewUtils.circleNum);
            for (int i = 0; i < LoadingViewUtils.circleNum; i++) {
                paintAngles[i] = 0;
                circleColors[i] = LoadingViewUtils.getColors()[list.get(i)];
                circleVisible[i] = false;
            }
        }

        public void setLoadingView(LoadingView loadingView) {
            this.loadingView = loadingView;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            this.height = (float) getHeight() / 2;
            this.width = (float) getWidth() / 2;
            radius = Math.min(this.height, this.width) - LoadingViewUtils.circleRadius;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //绘制圆圈
            if (reverse) {
                for (int i = LoadingViewUtils.circleNum - 1; i >= 0; i--) {
                    //可见则进行绘制
                    if (circleVisible[i]) {
                        paint.setColor(circleColors[i]);
                        float[] location = calLocation(paintAngles[i]);
                        canvas.drawCircle(location[0], location[1], LoadingViewUtils.circleRadius, paint);
                    }
                }
            } else {
                for (int i = 0; i < LoadingViewUtils.circleNum; i++) {
                    //可见则进行绘制
                    if (circleVisible[i]) {
                        paint.setColor(circleColors[i]);
                        float[] location = calLocation(paintAngles[i]);
                        canvas.drawCircle(location[0], location[1], LoadingViewUtils.circleRadius, paint);
                    }
                }
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();

            cancelTimer();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();

            startTimer();
        }

        public void startTimer() {
            loadingView.log("AnimationView[" + hashCode() + "]: startTimer");
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
            loadingView.log("AnimationView[" + hashCode() + "]: cancelTimer");
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }

            pause();
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

            postInvalidate();
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
    }
}
