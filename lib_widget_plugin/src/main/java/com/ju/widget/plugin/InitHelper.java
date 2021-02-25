package com.ju.widget.plugin;

import android.content.Context;

import com.ju.widget.IWidgetManager;
import com.ju.widget.IWidgetServer;
import com.ju.widget.IWidgetView;
import com.ju.widget.time.WidgetManager;

import java.util.List;

/**
 * 插件初始化
 */
public class InitHelper {
    private static IWidgetServer iWidgetServer;
    private static IWidgetManager iWidgetManager;

    /**
     * @param iWidgetServer 加载plugin时传递进来的widget管理器
     */
    public static void init(Context context, IWidgetServer iWidgetServer){
        InitHelper.iWidgetServer = iWidgetServer;
        iWidgetManager = new WidgetManager();
        List<IWidgetView> iWidgetViews = iWidgetManager.getWidgetList();
        /*通过iWidgetManager获取Widget列表*/
        InitHelper.iWidgetServer.register(iWidgetViews);
    }
}
