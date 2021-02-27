package com.ju.widget.interfaces.connector;

import android.content.Intent;

/**
 * 远端业务发送数据到Widget框架的接口定义
 */
public interface IWidgetServiceConnector {

    String INTENT_ACTION = Connector.INTENT_ACTION_WIDGET_SERVICE;

    /**
     * WidgetService收到的Intent数据中的Action常量定义
     */
    int ACT_ADD_WIDGET = 2001;
    int ACT_REMOVE_WIDGET = 2002;
    int ACT_ADD_WIDGET_LIST = 2003;
    int ACT_REMOVE_WIDGET_LIST = 2004;

    /**
     * Widget数据发生变化的Action常量定义
     */
    int ACT_UPDATE_WIDGET_DATA = 2101;

    /**
     * 发送数据到WidgetService；
     *
     * 注意：Intent的封装要和WidgetService里的解析Intent对应；
     *
     * @param intent 数据封装
     */
    void sendData(Intent intent);

}
