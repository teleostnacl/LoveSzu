package com.teleostnacl.szu.library.view.recyclerview;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.graphics.Rect;
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class VerticalStaggeredGridLayoutManager extends RecyclerView.LayoutManager {

    //竖直偏移量 每次换行时，要根据这个offset判断
    private int mVerticalOffset;

    //屏幕可见的第一个View的Position
    private int mFirstVisiblePos;

    //屏幕可见的最后一个View的Position
    private int mLastVisiblePos;

    //key 是View的position，保存View的bounds 和 显示标志，
    private final SparseArray<Rect> mItemRects;

    public VerticalStaggeredGridLayoutManager() {
        mItemRects = new SparseArray<>();
    }

    @Override
    public boolean isAutoMeasureEnabled() { return true; }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            //没有Item，界面空着吧
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {
            //state.isPreLayout()是支持动画的
            return;
        }
        //onLayoutChildren方法在RecyclerView 初始化时 会执行两遍
        detachAndScrapAttachedViews(recycler);
        //初始化
        mVerticalOffset = 0;
        mFirstVisiblePos = 0;
        mLastVisiblePos = getItemCount();

        onLayout(recycler,  0);
    }

    /**
     * 填充childView的核心方法,应该先填充，再移动。
     * 在填充时，预先计算dy的在内，如果View越界，回收掉。
     * 一般情况是返回dy，如果出现View数量不足，则返回修正后的dy.
     * @param dy    RecyclerView给我们的位移量,+,显示底端， -，显示头部
     * @return 修正以后真正的dy（可能剩余空间不够移动那么多了 所以return <|dy|）
     */
    private int onLayout(RecyclerView.Recycler recycler, int dy) {

        int topOffset = getPaddingTop();

        //回收越界子View
        if (getChildCount() > 0) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                assert child != null;
                //需要回收当前屏幕，上越界的View
                if (dy > 0) {
                    if (getDecoratedBottom(child) - dy < topOffset) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiblePos++;
                    }
                }
                //回收当前屏幕，下越界的View
                else if (dy < 0) {
                    if (getDecoratedTop(child) - dy > getHeight() - getPaddingBottom()) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos--;
                    }
                }
            }
            //detachAndScrapAttachedViews(recycler);
        }

        int leftOffset = getPaddingLeft();
        int lineMaxHeight = 0;
        //布局子View阶段
        //向下滑动
        if (dy >= 0) {
            int minPos = mFirstVisiblePos;
            mLastVisiblePos = getItemCount() - 1;
            if (getChildCount() > 0) {
                View lastView = getChildAt(getChildCount() - 1);
                assert lastView != null;
                minPos = getPosition(lastView) + 1;//从最后一个View+1开始吧
                topOffset = getDecoratedTop(lastView);
                leftOffset = getDecoratedRight(lastView);
                lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(lastView));
            }
            //顺序addChildView
            for (int i = minPos; i <= mLastVisiblePos; i++) {
                // 找recycler要一个childItemView,我们不管它是从scrap里取，
                // 还是从RecyclerViewPool里取，亦或是onCreateViewHolder里拿。
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, 0, 0);
                //计算宽度 包括margin
                if (leftOffset + getDecoratedMeasurementHorizontal(child) <= getHorizontalSpace()) {
                    //当前行还排列的下
                    layoutDecoratedWithMargins(child, leftOffset, topOffset,
                            leftOffset + getDecoratedMeasurementHorizontal(child),
                            topOffset + getDecoratedMeasurementVertical(child));

                    //保存Rect供逆序layout用
                    Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset,
                            leftOffset + getDecoratedMeasurementHorizontal(child),
                            topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                    mItemRects.put(i, rect);

                    //改变 left  lineHeight
                    leftOffset += getDecoratedMeasurementHorizontal(child);
                    lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                }
                //当前行排列不下
                else {
                    //改变top  left  lineHeight
                    leftOffset = getPaddingLeft();
                    topOffset += lineMaxHeight;
                    lineMaxHeight = 0;

                    //新起一行的时候要判断一下边界
                    if (topOffset - dy > getHeight() - getPaddingBottom()) {
                        //越界了 就回收
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePos = i - 1;
                    }
                    else {
                        layoutDecoratedWithMargins(child, leftOffset, topOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child));

                        //保存Rect供逆序layout用
                        Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset, leftOffset + getDecoratedMeasurementHorizontal(child), topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset);
                        mItemRects.put(i, rect);

                        //改变 left  lineHeight
                        leftOffset += getDecoratedMeasurementHorizontal(child);
                        lineMaxHeight = Math.max(lineMaxHeight, getDecoratedMeasurementVertical(child));
                    }
                }
            }

            //添加完后，判断是否已经没有更多的ItemView，并且此时屏幕仍有空白，则需要修正dy
            View lastChild = getChildAt(getChildCount() - 1);
            assert lastChild != null;
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    dy -= gap;
                }
            }
        }
        //向上滑动
        else {
            //屏幕显示的第一个View的索引
            int maxPos = getItemCount() - 1;
            mFirstVisiblePos = 0;
            if (getChildCount() > 0) {
                maxPos = getPosition(Objects.requireNonNull(getChildAt(0))) - 1;
            }
            for (int i = maxPos; i >= mFirstVisiblePos; i--) {
                //利用Rect保存子View边界 正序排列时，保存每个子View的Rect，逆序时，直接拿出来layout。
                Rect rect = mItemRects.get(i);

                if (rect.bottom - mVerticalOffset - dy < getPaddingTop()) {
                    mFirstVisiblePos = i + 1;
                    break;
                } else {
                    View child = recycler.getViewForPosition(i);
                    addView(child, 0);//将View添加至RecyclerView中，childIndex为1，但是View的位置还是由layout的位置决定
                    measureChildWithMargins(child, 0, 0);

                    layoutDecoratedWithMargins(child, rect.left,
                            rect.top - mVerticalOffset, rect.right,
                            rect.bottom - mVerticalOffset);
                }
            }
        }

        return dy;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //位移0、没有子View 当然不移动
        if (dy == 0 || getChildCount() == 0) {
            return 0;
        }

        //实际滑动的距离， 可能会在边界处被修复
        int realOffset = dy;
        //边界修复代码
        if (mVerticalOffset + realOffset < 0) {
            //上边界
            realOffset = -mVerticalOffset;
        }
        //下边界
        else if (realOffset > 0) {
            //利用最后一个子View比较修正
            View lastChild = getChildAt(getChildCount() - 1);
            assert lastChild != null;
            if (getPosition(lastChild) == getItemCount() - 1) {
                int gap = getHeight() - getPaddingBottom() - getDecoratedBottom(lastChild);
                if (gap > 0) {
                    realOffset = -gap;
                } else if (gap == 0) {
                    realOffset = 0;
                } else {
                    realOffset = Math.min(realOffset, -gap);
                }
            }
        }

        realOffset = onLayout(recycler, realOffset);//先填充，再位移。

        mVerticalOffset += realOffset;//累加实际滑动距离

        offsetChildrenVertical(-realOffset);//滑动

        return realOffset;
    }

    /**
     * 获取某个childView在水平方向所占的空间
     */
    public int getDecoratedMeasurementHorizontal(@NonNull View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    /**
     * 获取某个childView在竖直方向所占的空间
     */
    public int getDecoratedMeasurementVertical(@NonNull View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    //禁止在水平方向滚动
    @Override
    public boolean canScrollHorizontally() {  return false; }

    //允许在垂直方向滚动
    @Override
    public boolean canScrollVertically() { return true; }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }
}

