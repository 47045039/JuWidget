package com.ju.widget.interfaces.connector;

import android.content.Intent;

public class Connector {

    /**
     * 框架WidgetService intent action
     */
    public static final String INTENT_ACTION_WIDGET_SERVICE = "ju.intent.action.WIDGET_SERVICE";

    /**
     * 与远端业务模块通信的intent action前缀（多个业务模块须分开，所以要拼接业务模块的包名）
     */
    public static final String INTENT_ACTION_REMOTE_BUSINESS = "ju.intent.action.WIDGET_REMOTE_BUSINESS.";

    /**
     * Intent中数据的key常量定义
     */
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_COMMAND = "command";
    public static final String KEY_PARAMS = "params";
    public static final String KEY_ACTION = "action";
    public static final String KEY_PRODUCT_ID = "product_id";
    public static final String KEY_WIDGET_ID = "widget_id";
    public static final String KEY_PAYLOAD = "payload";

    /**
     * 无效的远端业务的命令
     */
    public static final int CMD_INVALID = -1;

    /**
     * 远端业务的命令常量定义
     */
    public static final int CMD_UPDATE_WIDGET_DATA = 1001;

    /**
     * 无效的Action
     */
    public static final int ACT_INVALID = -1;

    /**
     * WidgetService收到的Intent数据中的Action常量定义
     */
    public static final int ACT_ADD_WIDGET = 2001;
    public static final int ACT_REMOVE_WIDGET = 2002;
    public static final int ACT_ADD_WIDGET_LIST = 2003;
    public static final int ACT_REMOVE_WIDGET_LIST = 2004;

    /**
     * Widget数据发生变化的Action常量定义
     */
    public static final int ACT_UPDATE_WIDGET_DATA = 2101;


    public static final String getPackage(Intent intent) {
        return intent.getStringExtra(KEY_PACKAGE);
    }

    public static final void putPackage(Intent intent, String pkg) {
        intent.putExtra(KEY_PACKAGE, pkg);
    }

    public static final int getCommand(Intent intent) {
        return intent.getIntExtra(KEY_COMMAND, CMD_INVALID);
    }

    public static final void putCommand(Intent intent, int cmd) {
        intent.putExtra(KEY_COMMAND, cmd);
    }

    public static final String getParams(Intent intent) {
        return intent.getStringExtra(KEY_PARAMS);
    }

    public static final void putParams(Intent intent, String params) {
        intent.putExtra(KEY_PARAMS, params);
    }

    public static final int getAction(Intent intent) {
        return intent.getIntExtra(KEY_ACTION, ACT_INVALID);
    }

    public static final void putAction(Intent intent, int act) {
        intent.putExtra(KEY_ACTION, act);
    }

    public static final String getProductId(Intent intent) {
        return intent.getStringExtra(KEY_PRODUCT_ID);
    }

    public static final void putProductId(Intent intent, String pid) {
        intent.putExtra(KEY_PRODUCT_ID, pid);
    }

    public static final String getWidgetId(Intent intent) {
        return intent.getStringExtra(KEY_WIDGET_ID);
    }

    public static final void putWidgetId(Intent intent, String wid) {
        intent.putExtra(KEY_WIDGET_ID, wid);
    }

    public static final String getPayload(Intent intent) {
        return intent.getStringExtra(KEY_PAYLOAD);
    }

    public static final void putPayload(Intent intent, String payload) {
        intent.putExtra(KEY_PAYLOAD, payload);
    }
    
}
