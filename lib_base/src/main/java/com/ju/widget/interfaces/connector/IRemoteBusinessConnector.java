package com.ju.widget.interfaces.connector;

import android.content.Intent;

/**
 * WidgetManager和远端业务模块的通信接口定义
 */
public interface IRemoteBusinessConnector {

    /**
     * 与远端业务模块通信的intent action前缀（多个业务模块须分开，所以要拼接业务模块的包名）
     */
    String INTENT_ACTION_PREFIX = Connector.INTENT_ACTION_REMOTE_BUSINESS;

    /**
     * 远端业务的命令常量定义
     */
    int CMD_UPDATE_WIDGET_DATA = 1001;

    /**
     * 发送命令到远端业务模块
     *
     * @param intent 数据
     */
    void sendCommand(Intent intent);

}
