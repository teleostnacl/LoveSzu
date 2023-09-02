package com.teleostnacl.common.android.view;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ViewUtils {
    private static final long duration = 144;

    public static void setVisible(@NonNull View view) {
        if (!isVisible(view)) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                view.setVisibility(VISIBLE);
            } else {
                view.post(() -> view.setVisibility(VISIBLE));
            }
        }
    }

    public static void setInvisible(@NonNull View view) {
        if (!isInvisible(view)) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                view.setVisibility(INVISIBLE);
            } else {
                view.post(() -> view.setVisibility(INVISIBLE));
            }
        }
    }

    public static void setGone(@NonNull View view) {
        if (!isGone(view)) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                view.setVisibility(GONE);
            } else {
                view.post(() -> view.setVisibility(GONE));
            }
        }
    }

    public static void setVisibleWithAnimation(@NonNull View view) {
        setVisibleWithAnimation(view, null);
    }

    public static void setInvisibleWithAnimation(@NonNull View view) {
        setInvisibleWithAnimation(view, null);
    }

    public static void setGoneWithAnimation(@NonNull View view) {
        setGoneWithAnimation(view, null);
    }

    public static void setVisibleWithAnimation(@NonNull View view, @Nullable Runnable runnable) {
        // 不可见时才执行动画 使其变得可见
        if (!isVisible(view)) {
            view.post(() -> {
                ObjectAnimator animator = ObjectAnimator.ofFloat(
                        view, "alpha", 0, 1);
                animator.setDuration(duration);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (runnable != null) runnable.run();
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        view.setVisibility(VISIBLE);
                    }
                });
                // 执行动画期间应该使其可见
                animator.addUpdateListener(animation -> {
                    if (isVisible(view)) view.setVisibility(VISIBLE);
                });
                animator.start();
            });
        } else if (runnable != null) runnable.run();
    }

    public static void setInvisibleWithAnimation(@NonNull View view, @Nullable Runnable runnable) {
        // 可见时才执行动画 使其变得InVisible
        if (isVisible(view)) {
            float alpha = view.getAlpha();
            view.post(() -> {
                ObjectAnimator animator = ObjectAnimator.ofFloat(
                        view, "alpha", 1, 0);
                animator.setDuration(duration);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(INVISIBLE);
                        view.setAlpha(alpha);
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                });
                // 执行动画期间应该使其可见
                animator.addUpdateListener(animation -> {
                    if (isVisible(view)) view.setVisibility(VISIBLE);
                });
                animator.start();
            });
        } else if (runnable != null) runnable.run();
    }

    public static void setGoneWithAnimation(@NonNull View view, @Nullable Runnable runnable) {
        // 可见时才执行动画 使其变得Gone
        if (isVisible(view)) {
            float alpha = view.getAlpha();
            view.post(() -> {
                ObjectAnimator animator = ObjectAnimator.ofFloat(
                        view, "alpha", 1, 0);
                animator.setDuration(duration);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(GONE);
                        view.setAlpha(alpha);
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                });
                // 执行动画期间应该使其可见
                animator.addUpdateListener(animation -> {
                    if (isVisible(view)) view.setVisibility(VISIBLE);
                });
                animator.start();
            });
        } else if (runnable != null) runnable.run();
    }

    public static boolean isGone(@NonNull View view) {
        return view.getVisibility() == GONE;
    }

    public static boolean isVisible(@NonNull View view) {
        return view.getVisibility() == VISIBLE;
    }

    public static boolean isInvisible(@NonNull View view) {
        return view.getVisibility() == INVISIBLE;
    }
}
