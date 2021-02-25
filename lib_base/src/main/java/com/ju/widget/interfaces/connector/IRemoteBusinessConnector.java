package com.ju.widget.interfaces.connector;

import android.content.Intent;

/**
 * WidgetManager和远端业务模块的通信接口定义
 */
public interface IRemoteBusinessConnector extends IConnector {

    /**
     * 与远端业务模块通信的intent action前缀（多个业务模块须分开，所以要拼接业务模块的包名）
     */
    String INTENT_ACTION_PREFIX = IConnector.INTENT_ACTION_REMOTE_BUSINESS;

    /**
     * 发送到远端业务模块的的Intent中数据的key常量定义
     */
    String KEY_COMMAND = IConnector.KEY_COMMAND;
    String KEY_PARAMS = IConnector.KEY_PARAMS;

    /**
     * 无效的远端业务的命令
     */
    int CMD_INVALID = IConnector.CMD_INVALID;

    /**
     * 远端业务的命令常量定义
     */
    int CMD_UPDATE_WIDGET_DATA = IConnector.CMD_UPDATE_WIDGET_DATA;

    /**
     * 发送命令到远端业务模块
     *
     * @param intent 数据
     */
    void sendCommand(Intent intent);

}
