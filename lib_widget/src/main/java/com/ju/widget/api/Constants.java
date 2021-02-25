package com.ju.widget.api;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: 常量定义
 */
public class Constants {

    public static final int ORIENTATION_H = 0x00000001;         // 横向
    public static final int ORIENTATION_V = 0x00000010;         // 纵向
    public static final int ORIENTATION_MASK = 0x00000011;      // 横向 + 纵向

    /**
     * 无效的Action
     */
    public static final int ACT_INVALID = -1;

    /**
     * 无效的Product ID
     */
    public static final String PRODUCT_ID_INVALID = "";

    /**
     * 无效的Widget ID
     */
    public static final String WIDGET_ID_INVALID = "";

    /**
     * WidgetService收到的Intent中数据的key常量定义
     */
    public static final String KEY_ACTION = "action";
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_PRODUCT_ID = "product_id";
    public static final String KEY_WIDGET_ID = "widget_id";
    public static final String KEY_PAYLOAD = "payload";


    /**
     * Widget信息发生变化的Action常量定义
     */
    public static final int ACT_ADD_WIDGET = 1001;
    public static final int ACT_REMOVE_WIDGET = 1002;
    public static final int ACT_ADD_WIDGET_LIST = 1003;
    public static final int ACT_REMOVE_WIDGET_LIST = 1004;

    /**
     * Widget数据发生变化的Action常量定义
     */
    public static final int ACT_UPDATE_WIDGET_DATA = 1101;

}
