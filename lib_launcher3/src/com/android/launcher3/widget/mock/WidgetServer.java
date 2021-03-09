package com.android.launcher3.widget.mock;

import android.content.ComponentName;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WidgetServer {
    private static WidgetServer sWidgetServer;
    private Context context;

    public WidgetServer(Context context) {
        this.context = context;
    }

    public static WidgetServer getInstance(Context context) {
        if (sWidgetServer == null) {
            synchronized (WidgetServer.class) {
                if (sWidgetServer == null) {
                    sWidgetServer = new WidgetServer(context);
                }
            }
        }
        return sWidgetServer;
    }

    public List<Widget> widgetList() {
        ArrayList<Widget> widgetArrayList = new ArrayList<>();
        Widget widget = mockWidget();
        widgetArrayList.add(widget);
        return widgetArrayList;
    }

    private Widget mockWidget() {
        Widget widget = new Widget();
        AppWidgetProviderInfo appWidgetProviderInfo = new AppWidgetProviderInfo();
        appWidgetProviderInfo.widgetId = 1000;
        appWidgetProviderInfo.label = "hello!!!";
        appWidgetProviderInfo.initialKeyguardLayout = 0x8888;
        appWidgetProviderInfo.initialLayout = 0x8888;
        appWidgetProviderInfo.autoAdvanceViewId = 0x8888;
        appWidgetProviderInfo.configure = null;
        appWidgetProviderInfo.icon = 0x0;
        appWidgetProviderInfo.minHeight = 30;
        appWidgetProviderInfo.minWidth = 60;
        appWidgetProviderInfo.minResizeWidth = 120;
        appWidgetProviderInfo.minResizeHeight = 60;
        appWidgetProviderInfo.previewImage = 0;
        appWidgetProviderInfo.provider = new ComponentName(context.getPackageName(), "com.Fake");
        appWidgetProviderInfo.providerInfo = null;
        appWidgetProviderInfo.resizeMode = AppWidgetProviderInfo.RESIZE_BOTH;
        appWidgetProviderInfo.updatePeriodMillis = 1000;
        appWidgetProviderInfo.widgetCategory = AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN;
        appWidgetProviderInfo.widgetFeatures = AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE;

        widget.set(appWidgetProviderInfo);
        return widget;
    }

    public View getWidgetView(int widgetId) {
        List<Widget> widgetList = widgetList();
        for(Widget widget: widgetList){
            if(widget.getAppWidgetProviderInfo().widgetId==widgetId)
                return widget.getView(context);
        }
        TextView textView = new TextView(context);
        textView.setText("待更新");
        return textView;
    }

    public AppWidgetProviderInfo appWidgetProviderInfo(int widgetId){
        List<Widget> widgetList = widgetList();
        for(Widget widget: widgetList){
            if(widget.getAppWidgetProviderInfo().widgetId==widgetId)
                return widget.getAppWidgetProviderInfo();
        }
        // TODO: 2021/3/1 这里 需要完善
        return new AppWidgetProviderInfo();
    }
}
