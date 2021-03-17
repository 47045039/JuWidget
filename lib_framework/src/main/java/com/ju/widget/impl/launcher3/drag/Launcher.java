package com.ju.widget.impl.launcher3.drag;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.util.ArrayMap;

import com.ju.widget.impl.launcher3.CellLayout;
import com.ju.widget.impl.launcher3.drag.DragLayer;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/3/16
 * @Description: 管理CellLayout\DragLayer等相关资源，替换原生launcher3里的Launcher类
 */
public class Launcher {

    private static final ActivityLifecycleCallbacks LIFECYCLE_CALLBACKS = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

        @Override
        public void onActivityStarted(Activity activity) {}

        @Override
        public void onActivityResumed(Activity activity) {}

        @Override
        public void onActivityPaused(Activity activity) {}

        @Override
        public void onActivityStopped(Activity activity) {}

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

        @Override
        public void onActivityDestroyed(Activity activity) {
            detachDragLayer(activity);
            detachCellLayout(activity);
        }
    };

    private static final ArrayMap<Context, DragLayer> sDragLayerCaches = new ArrayMap<>();
    private static final ArrayMap<Context, CellLayout> sCellLayoutCaches = new ArrayMap<>();

    public static boolean attachDragLayer(Context context, DragLayer layout) {
        if (sDragLayerCaches.containsKey(context)) {
            return false;
        } else {
            sDragLayerCaches.put(context, layout);
            return true;
        }
    }

    public static boolean detachDragLayer(Context context) {
        if (sDragLayerCaches.containsKey(context)) {
            sDragLayerCaches.remove(context);
        }
        return true;
    }

    public static final DragLayer getDragLayer(Context context) {
        return sDragLayerCaches.get(context);
    }

    public static boolean attachCellLayout(Context context, CellLayout layout) {
        if (sCellLayoutCaches.containsKey(context)) {
            return false;
        } else {
            sCellLayoutCaches.put(context, layout);
            return true;
        }
    }

    public static boolean detachCellLayout(Context context) {
        if (sCellLayoutCaches.containsKey(context)) {
            sCellLayoutCaches.remove(context);
        }
        return true;
    }

    public static CellLayout getCellLayout(Context context) {
        return sCellLayoutCaches.get(context);
    }

    public static final boolean isVerticalBarLayout(Context context) {
        return false;
    }

}
