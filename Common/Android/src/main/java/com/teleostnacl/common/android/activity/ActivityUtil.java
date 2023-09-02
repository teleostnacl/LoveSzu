package com.teleostnacl.common.android.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 统一管理Activity的工具类
 */
public final class ActivityUtil {
    private static final List<ActivityWeakReference> activities = new ArrayList<>();

    public static void add(AppCompatActivity activity) {
        activities.add(new ActivityWeakReference(activity));
    }

    public static void remove(AppCompatActivity activity) {
        activities.remove(new ActivityWeakReference(activity));
    }

    /**
     * 结束所有的Activity
     */
    public static void clear() {
        for (ActivityWeakReference activityWeakReference : activities) {
            AppCompatActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.finishAndRemoveTask();
            }
        }

        activities.clear();
    }

    private static class ActivityWeakReference extends WeakReference<AppCompatActivity> {
        public ActivityWeakReference(AppCompatActivity referent) {
            super(referent);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof ActivityWeakReference)) {
                return false;
            }

            ActivityWeakReference that = (ActivityWeakReference) obj;

            return this.get() == that.get();
        }
    }
}
