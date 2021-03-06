package com.ju.widget.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import static com.ju.widget.api.Constants.ORIENTATION_H;
import static com.ju.widget.api.Constants.ORIENTATION_V;

public class Tools {

    private static final HandlerThread sWorker = new HandlerThread("WidgetEnv");
    static {
        sWorker.start();
    }

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static final Handler sWorkHandler = new Handler(sWorker.getLooper());

    public static final boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static final boolean isWorkThread() {
        return sWorker.getLooper() == Looper.myLooper();
    }

    public static final void runOnMainThread(Runnable task) {
        runOnMainThread(task, 0);
    }

    public static final void runOnMainThread(Runnable task, int delay) {
        if (task == null) {
            return;
        }

        if (delay <= 0 && isMainThread()) {
            task.run();
        } else {
            sMainHandler.postDelayed(task, delay);
        }
    }

    public static final void runOnWorkThread(Runnable task) {
        runOnWorkThread(task, 0);
    }

    public static final void runOnWorkThread(Runnable task, int delay) {
        if (task == null) {
            return;
        }

        if (delay <= 0 && isWorkThread()) {
            task.run();
        } else {
            sWorkHandler.post(task);
        }
    }

    public static final Handler newWorkHandler() {
        return new Handler(sWorker.getLooper());
    }

    public static final boolean isHorizontal(int orientation) {
        return (orientation & ORIENTATION_H) == ORIENTATION_H;
    }

    public static final boolean isVertical(int orientation) {
        return (orientation & ORIENTATION_V) == ORIENTATION_V;
    }

}
