package com.ju.widget;

import android.view.ViewGroup;

import java.util.List;

public interface IWidgetServer {
    void register(List<IWidgetView> iWidgetViewList);

    /**
     * @param parent widget容器
     */
    /*显示widget 列表*/
    void show(ViewGroup parent);

    List<IWidgetView> getShownWidgetList();
}
