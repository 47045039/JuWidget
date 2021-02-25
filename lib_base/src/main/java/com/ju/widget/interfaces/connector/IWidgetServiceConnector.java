package com.ju.widget.interfaces.connector;

import android.content.Intent;

/**
 * 远端业务发送数据到Widget框架的接口定义
 */
public interface IWidgetServiceConnector extends IConnector {

    String INTENT_ACTION = IConnector.INTENT_ACTION_WIDGET_SERVICE;

    /**
     * WidgetService收到的Intent中数据的key常量定义
     */
    String KEY_ACTION = IConnector.KEY_ACTION;
    String KEY_PACKAGE = IConnector.KEY_PACKAGE;
    String KEY_PRODUCT_ID = IConnector.KEY_PRODUCT_ID;
    String KEY_WIDGET_ID = IConnector.KEY_WIDGET_ID;
    String KEY_PAYLOAD = IConnector.KEY_PAYLOAD;

    /**
     * 无效的Action
     */
    int ACT_INVALID = IConnector.ACT_INVALID;

    /**
     * WidgetService收到的Intent数据中的Action常量定义
     */
    int ACT_ADD_WIDGET = IConnector.ACT_ADD_WIDGET;
    int ACT_REMOVE_WIDGET = IConnector.ACT_REMOVE_WIDGET;
    int ACT_ADD_WIDGET_LIST = IConnector.ACT_ADD_WIDGET_LIST;
    int ACT_REMOVE_WIDGET_LIST = IConnector.ACT_REMOVE_WIDGET_LIST;

    /**
     * Widget数据发生变化的Action常量定义
     */
    int ACT_UPDATE_WIDGET_DATA = IConnector.ACT_UPDATE_WIDGET_DATA;

    /**
     * 发送数据到WidgetService；
     *
     * 注意：Intent的封装要和WidgetService里的解析Intent对应；
     *
     * @param intent 数据封装
     */
    void sendData(Intent intent);

}
