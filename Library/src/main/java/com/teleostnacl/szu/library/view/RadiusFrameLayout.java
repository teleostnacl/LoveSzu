package com.teleostnacl.szu.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.teleostnacl.common.android.context.ResourcesUtils;
import com.teleostnacl.szu.library.R;

/**
 * 实现任意一个角的圆角化
 */
public class RadiusFrameLayout extends FrameLayout {

    //仅边框
    private final static int STROKE = 2;
    //填充
    private final static int FILL = 1;

    //绘制模式,仅边框或者填充
    private final int mode;
    private final float strokeSize;

    //设置各个角的半径,默认为12dp
    private float topLeftRadius;
    private float topRightRadius;
    private float bottomRightRadius;
    private float bottomLeftRadius;

    private final Path path = new Path();

    private int color;

    public RadiusFrameLayout(Context context) {
        this(context, null);
    }

    public RadiusFrameLayout(Context context, float topLeftRadius, float topRightRadius,
                             float bottomRightRadius, float bottomLeftRadius) {
        this(context);
        setRadius(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
    }

    public RadiusFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadiusFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //从attr中取出圆角值, 优先级为, 各个角 > 整体圆角 > 单个角 > 单条边的两个角
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RadiusFrameLayout);

        if (array.hasValue(R.styleable.RadiusFrameLayout_cornersRadius)) {
            setCornersRadius(array.getString(R.styleable.RadiusFrameLayout_cornersRadius));
        }

        //如果设置了整体的圆角,则将所有圆角赋值为该值
        else if (array.hasValue(R.styleable.RadiusFrameLayout_radius)) {
            setRadius(array.getDimension(R.styleable.RadiusFrameLayout_radius, ResourcesUtils.dp_float_12));
        }

        //否则对各个角或边进行取值
        else {
            //按照上右下左顺时针旋转的优先级进行设置,从优先级低的开始设置,高优先级覆盖低优先级
            topLeftRadius = bottomLeftRadius =          //左
                    array.getDimension(R.styleable.RadiusFrameLayout_leftRadius, ResourcesUtils.dp_float_12);
            bottomLeftRadius = bottomRightRadius =      //下
                    array.getDimension(R.styleable.RadiusFrameLayout_bottomRadius, ResourcesUtils.dp_float_12);
            topRightRadius = bottomRightRadius =        //右
                    array.getDimension(R.styleable.RadiusFrameLayout_bottomRadius, ResourcesUtils.dp_float_12);
            topLeftRadius = topRightRadius =            //上
                    array.getDimension(R.styleable.RadiusFrameLayout_topRadius, ResourcesUtils.dp_float_12);

            //设置角,如果有值才进行覆盖
            if (array.hasValue(R.styleable.RadiusFrameLayout_topLeftRadius))
                topLeftRadius = array.getDimension(R.styleable.RadiusFrameLayout_topLeftRadius,
                        ResourcesUtils.dp_float_12);
            if (array.hasValue(R.styleable.RadiusFrameLayout_topRightRadius))
                topRightRadius = array.getDimension(R.styleable.RadiusFrameLayout_topRightRadius,
                        ResourcesUtils.dp_float_12);
            if (array.hasValue(R.styleable.RadiusFrameLayout_bottomRightRadius))
                bottomLeftRadius = array.getDimension(R.styleable.RadiusFrameLayout_bottomRightRadius,
                        ResourcesUtils.dp_float_12);
            if (array.hasValue(R.styleable.RadiusFrameLayout_bottomLeftRadius))
                bottomRightRadius = array.getDimension(R.styleable.RadiusFrameLayout_bottomLeftRadius,
                        ResourcesUtils.dp_float_12);
        }

        //获取绘制模式,默认为填充
        mode = array.getInt(R.styleable.RadiusFrameLayout_mode, FILL);

        strokeSize = array.getDimension(R.styleable.RadiusFrameLayout_strokeSize, 1);

        color = array.getColor(R.styleable.RadiusFrameLayout_backgroundColor, Color.WHITE);

        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量完成之后设置边界与背景
        setOutLineAndBackground();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        //进行裁剪圆角
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
    }


    //设置边界与背景
    private void setOutLineAndBackground() {
        //设置边界
        RectF rectF = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        //定义圆角,根据
        float[] radius = new float[]{this.topLeftRadius, this.topLeftRadius,
                this.topRightRadius, this.topRightRadius,
                this.bottomRightRadius, this.bottomRightRadius,
                this.bottomLeftRadius, this.bottomLeftRadius};

        //重置路径,并设置新路径
        path.reset();
        path.addRoundRect(rectF, radius, Path.Direction.CCW);

        //修改背景
        setBackground(new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                canvas.clipPath(path);
                if (mode == FILL) {
                    canvas.drawColor(color);
                } else if (mode == STROKE) {
                    Path path1 = new Path();
                    RectF rectF1 = new RectF(strokeSize, strokeSize,
                            getMeasuredWidth() - strokeSize, getMeasuredHeight() - strokeSize);
                    float[] radius1 = new float[]{
                            RadiusFrameLayout.this.topLeftRadius - strokeSize,
                            RadiusFrameLayout.this.topLeftRadius - strokeSize,

                            RadiusFrameLayout.this.topRightRadius - strokeSize,
                            RadiusFrameLayout.this.topRightRadius - strokeSize,

                            RadiusFrameLayout.this.bottomRightRadius - strokeSize,
                            RadiusFrameLayout.this.bottomRightRadius - strokeSize,

                            RadiusFrameLayout.this.bottomLeftRadius - strokeSize,
                            RadiusFrameLayout.this.bottomLeftRadius - strokeSize
                    };

                    path1.addRoundRect(rectF1, radius1, Path.Direction.CCW);
                    canvas.clipOutPath(path1);
                    canvas.drawColor(color);
                }

            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSPARENT;
            }
        });

    }

    //左上 右上 右下 坐下顺序进行排列
    public void setRadius(float topLeftRadius, float topRightRadius,
                          float bottomRightRadius, float bottomLeftRadius) {
        this.topLeftRadius = topLeftRadius;
        this.topRightRadius = topRightRadius;
        this.bottomRightRadius = bottomRightRadius;
        this.bottomLeftRadius = bottomLeftRadius;

        requestLayout();
    }

    //根据字符串设置每个角的圆角
    public void setCornersRadius(@NonNull String s) {
        if (s.matches("\\d+,\\s*\\d+,\\s*\\d+,\\s*\\d+")) {

            String[] corners = s.split(",");

            setRadius(Float.parseFloat(corners[0].trim()),
                    Float.parseFloat(corners[1].trim()),
                    Float.parseFloat(corners[2].trim()),
                    Float.parseFloat(corners[3].trim()));
        } else {
            setRadius(ResourcesUtils.dp_float_12);
        }
    }

    //设置整体圆角
    public void setRadius(float radius) {
        setRadius(radius, radius, radius, radius);
    }

    //设置单个角的圆角
    public void setTopLeftRadius(float topLeftRadius) {
        setRadius(topLeftRadius, getTopRightRadius(), getBottomRightRadius(), getBottomLeftRadius());
    }

    public void setTopRightRadius(float topRightRadius) {
        setRadius(getTopLeftRadius(), topRightRadius, getBottomRightRadius(), getBottomLeftRadius());
    }

    public void setBottomRightRadius(float bottomRightRadius) {
        setRadius(getTopLeftRadius(), getTopRightRadius(), bottomRightRadius, getBottomLeftRadius());
    }

    public void setBottomLeftRadius(float bottomLeftRadius) {
        setRadius(getTopLeftRadius(), getTopRightRadius(), getBottomRightRadius(), bottomLeftRadius);
    }

    //设置单条边上两个角的圆角值
    public void setBottomRadius(float bottomRadius) {
        setRadius(getTopLeftRadius(), getTopRightRadius(), bottomRadius, bottomRadius);
    }

    public void setTopRadius(float topRadius) {
        setRadius(topRadius, topRadius, getBottomRightRadius(), getBottomLeftRadius());
    }

    public void setLeftRadius(float leftRadius) {
        setRadius(leftRadius, getTopRightRadius(), getBottomRightRadius(), leftRadius);
    }

    public void setRightRadius(float rightRadius) {
        setRadius(getBottomLeftRadius(), rightRadius, rightRadius, getBottomLeftRadius());
    }

    //设置背景颜色
    public void setCardBackgroundColor(@ColorInt int color) {
        this.color = color;
        requestLayout();
    }

    public float getTopLeftRadius() {
        return topLeftRadius;
    }

    public float getTopRightRadius() {
        return topRightRadius;
    }

    public float getBottomRightRadius() {
        return bottomRightRadius;
    }

    public float getBottomLeftRadius() {
        return bottomLeftRadius;
    }

}
