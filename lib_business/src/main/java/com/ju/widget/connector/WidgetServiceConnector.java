package com.ju.widget.connector;

import android.content.Context;
import android.content.Intent;

import com.ju.widget.interfaces.connector.IWidgetServiceConnector;

public class WidgetServiceConnector implements IWidgetServiceConnector {

    private final Context mContext;

    public WidgetServiceConnector(Context ctx) {
        mContext = ctx.getApplicationContext();
    }

    /**
     * 通知WidgetService新增Widget信息
     *
     * @param widget
     */
    public void notifyWidgetAdded(String widget) {
        final Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(KEY_PACKAGE, mContext.getPackageName());
        intent.putExtra(KEY_ACTION, ACT_ADD_WIDGET);
        intent.putExtra(KEY_PAYLOAD, widget);
        sendData(intent);
    }

    /**
     * 通知WidgetService移除Widget信息
     *
     * @param widget
     */
    public void notifyWidgetRemoved(String widget) {
        final Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(KEY_PACKAGE, mContext.getPackageName());
        intent.putExtra(KEY_ACTION, ACT_REMOVE_WIDGET);
        intent.putExtra(KEY_PAYLOAD, widget);
        sendData(intent);
    }

    /**
     * 通知WidgetService新增Widget list
     *
     * @param list
     */
    public void notifyWidgetListAdded(String list) {
        final Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(KEY_PACKAGE, mContext.getPackageName());
        intent.putExtra(KEY_ACTION, ACT_ADD_WIDGET_LIST);
        intent.putExtra(KEY_PAYLOAD, list);
        sendData(intent);
    }

    /**
     * 通知WidgetService移除Widget list
     *
     * @param list
     */
    public void notifyWidgetListRemoved(String list) {
        final Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(KEY_PACKAGE, mContext.getPackageName());
        intent.putExtra(KEY_ACTION, ACT_REMOVE_WIDGET_LIST);
        intent.putExtra(KEY_PAYLOAD, list);
        sendData(intent);
    }

    /**
     * 通知WidgetService Widget数据有更新
     *
     * @param data
     */
    public void notifyWidgetDataUpdated(String data) {
        final Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(KEY_PACKAGE, mContext.getPackageName());
        intent.putExtra(KEY_ACTION, ACT_UPDATE_WIDGET_DATA);
        intent.putExtra(KEY_PAYLOAD, data);
        sendData(intent);
    }

    @Override
    public void sendData(Intent intent) {
        mContext.startService(intent);
    }

}
