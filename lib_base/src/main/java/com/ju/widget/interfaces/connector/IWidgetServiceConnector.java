package com.ju.widget.interfaces.connector;

import android.content.Intent;

/**
 * 远端业务发送数据到Widget框架的接口定义
 */
public interface IWidgetServiceConnector {

    String INTENT_ACTION = Connector.INTENT_ACTION_WIDGET_SERVICE;

    /**
     * WidgetService收到的Intent中数据的key常量定义
     */
    String KEY_PACKAGE = Connector.KEY_PACKAGE;
    String KEY_ACTION = Connector.KEY_ACTION;
    String KEY_PRODUCT_ID = Connector.KEY_PRODUCT_ID;
    String KEY_WIDGET_ID = Connector.KEY_WIDGET_ID;
    String KEY_PAYLOAD = Connector.KEY_PAYLOAD;

    /**
     * 无效的Action
     */
    int ACT_INVALID = Connector.ACT_INVALID;

    /**
     * WidgetService收到的Intent数据中的Action常量定义
     */
    int ACT_ADD_WIDGET = Connector.ACT_ADD_WIDGET;
    int ACT_REMOVE_WIDGET = Connector.ACT_REMOVE_WIDGET;
    int ACT_ADD_WIDGET_LIST = Connector.ACT_ADD_WIDGET_LIST;
    int ACT_REMOVE_WIDGET_LIST = Connector.ACT_REMOVE_WIDGET_LIST;

    /**
     * Widget数据发生变化的Action常量定义
     */
    int ACT_UPDATE_WIDGET_DATA = Connector.ACT_UPDATE_WIDGET_DATA;

    /**
     * 发送数据到WidgetService；
     *
     * 注意：Intent的封装要和WidgetService里的解析Intent对应；
     *
     * @param intent 数据封装
     */
    void sendData(Intent intent);

}
