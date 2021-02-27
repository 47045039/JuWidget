package com.ju.widget.api;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.ju.widget.impl.WidgetLoader;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: Widget框架环境
 */
public class WidgetEnv {

    private static final HandlerThread sWorker = new HandlerThread("WidgetEnv");
    static {
        sWorker.start();
    }

    private static final Handler sHandler = new Handler(sWorker.getLooper());
    private static volatile boolean sInited = false;

    /**
     * 初始化接口
     *
     * @param context
     */
    public static final void init(Context context) {
        if (!sInited) {
            sInited = true;
            doInit(context.getApplicationContext());
        }
    }

    private static final void doInit(Context context) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                WidgetLoader.doInit(context);
            }
        });
    }

    public static final Handler getWorkHandler() {
        return sHandler;
    }

    public static final Handler newWorkHandler() {
        return new Handler(sWorker.getLooper());
    }
}
