package com.ju.widget.interfaces.connector;

import android.content.Intent;

/**
 * WidgetManager和远端业务模块的通信接口定义
 */
public interface IRemoteBusinessConnector {

    /**
     * 与远端业务模块通信的intent action前缀（多个业务模块须分开，所以要拼接业务模块的包名）
     */
    String INTENT_ACTION_PREFIX = Connector.INTENT_ACTION_WIDGET_BUSINESS;

    /**
     * 与远端业务模块通信的intent action前缀（多个业务模块须分开，所以要拼接业务模块的包名）
     */
    String PERMISSION_PREFIX = Connector.PERMISSION_WIDGET_BUSINESS;

    /**
     * 远端业务的命令常量定义
     */
    int CMD_QUERY_ALL_WIDGET = 1001;        // 查询所有的Widget信息
    int CMD_UPDATE_WIDGET_DATA = 1101;      // 更新Widget数据

    /**
     * 发送命令到远端业务模块
     *
     * @param intent 数据
     */
    void sendCommand(Intent intent);

}
