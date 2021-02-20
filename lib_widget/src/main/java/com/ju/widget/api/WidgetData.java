package com.ju.widget.api;

/**
 * Widget数据，业务根据实际需要扩展自己的实现
 */
public abstract class WidgetData {

    protected final String mWidgetID;   // widget id
    protected final String mTitle;      // 文字

    protected Object mJump;       // 跳转数据
    protected Object mLog;        // 日志数据
    protected Object mShowing;    // 显示数据

    public WidgetData(String id, String title) {
        mWidgetID = id;
        mTitle = title;
    }

    public String getWidgetID() {
        return mWidgetID;
    }

    public String getTitle() {
        return mTitle;
    }
}
