package com.ju.widget.util;

import android.os.Handler;
import android.os.Looper;

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

}
