package com.ju.widget.interfaces.connector;

public interface IConnector {

    /**
     * 框架WidgetService intent action
     */
    String INTENT_ACTION_WIDGET_SERVICE = "ju.intent.action.WIDGET_SERVICE";

    /**
     * 与远端业务模块通信的intent action前缀（多个业务模块须分开，所以要拼接业务模块的包名）
     */
    String INTENT_ACTION_REMOTE_BUSINESS = "ju.intent.action.WIDGET_REMOTE_BUSINESS.";

    /**
     * Intent中数据的key常量定义
     */
    String KEY_PACKAGE = "package";
    String KEY_COMMAND = "command";
    String KEY_ACTION = "action";
    String KEY_PRODUCT_ID = "product_id";
    String KEY_WIDGET_ID = "widget_id";
    String KEY_PARAMS = "params";
    String KEY_PAYLOAD = "payload";

    /**
     * 无效的远端业务的命令
     */
    int CMD_INVALID = -1;

    /**
     * 远端业务的命令常量定义
     */
    int CMD_UPDATE_WIDGET_DATA = 1001;

    /**
     * 无效的Action
     */
    int ACT_INVALID = -1;

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
    
}
