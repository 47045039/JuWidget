package com.ju.widget.util;

import android.os.Handler;
import android.os.Looper;

import static com.ju.widget.api.Constants.ORIENTATION_H;
import static com.ju.widget.api.Constants.ORIENTATION_V;

public class Tools {

    private static final Looper sMainLooper = Looper.getMainLooper();
    private static final Handler sMainHandler = new Handler(sMainLooper);

    public static final boolean isMainThread() {
        return sMainLooper == Looper.myLooper();
    }

    public static final void runOnMainThread(Runnable task) {
        if (task == null) {
            return;
        }

        if (isMainThread()) {
            task.run();
        } else {
            sMainHandler.post(task);
        }
    }

    public static final boolean isHorizontal(int orientation) {
        return (orientation & ORIENTATION_H) == ORIENTATION_H;
    }

    public static final boolean isVertical(int orientation) {
        return (orientation & ORIENTATION_V) == ORIENTATION_V;
    }

}
