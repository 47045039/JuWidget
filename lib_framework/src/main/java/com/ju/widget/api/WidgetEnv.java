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

    private static Context sHostContext;
    private static Context sPluginContext;

    /**
     * 初始化接口
     *
     * @param hostContext  宿主Context，Widget框架位于宿主中
     * @param pluginContext 插件Context，页面、aar等位于插件中
     */
    public static final void init(Context hostContext, Context pluginContext) {
        if (!sInited) {
            sInited = true;

            sHostContext = hostContext;
            sPluginContext = pluginContext;
            doInit(pluginContext.getApplicationContext());
        }
    }

    public static final boolean canShowWidgetMenu() {
        if (sHostContext == null) {
            return false;
        }

        // TODO: 为了测试方便，默认总是可以展示Widget菜单
        return true;//"com.jamdeo.tv.vod".equals(sHostContext.getPackageName());
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
