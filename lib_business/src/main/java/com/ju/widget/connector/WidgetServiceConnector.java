package com.ju.widget.connector;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ju.widget.interfaces.connector.Connector;
import com.ju.widget.interfaces.connector.IWidgetServiceConnector;
import com.ju.widget.util.Log;

public class WidgetServiceConnector implements IWidgetServiceConnector {

    private static final String TAG = "WidgetServiceConnector";

    private final Context mContext;
    private final String mRemotePackage;

    public WidgetServiceConnector(Context ctx, String pkg) {
        mContext = ctx;
        mRemotePackage = pkg;
    }

    /**
     * 通知WidgetService新增Widget信息
     *
     * @param version 本地业务模块的兼容版本号
     * @param pid     Product ID
     * @param widget  Widget信息字串
     */
    public void notifyWidgetAdded(int version, String pid, String widget) {
        final Intent intent = createIntent(ACT_ADD_WIDGET, version, pid, "", widget);
        sendData(intent);
    }

    /**
     * 通知WidgetService移除Widget信息
     *
     * @param version 本地业务模块的兼容版本号
     * @param pid     Product ID
     * @param widget  Widget信息字串
     */
    public void notifyWidgetRemoved(int version, String pid, String widget) {
        final Intent intent = createIntent(ACT_REMOVE_WIDGET, version, pid, "", widget);
        sendData(intent);
    }

    /**
     * 通知WidgetService新增Widget list
     *
     * @param version 本地业务模块的兼容版本号
     * @param pid     Product ID
     * @param list    Widget信息列表字串
     */
    public void notifyWidgetListAdded(int version, String pid, String list) {
        final Intent intent = createIntent(ACT_ADD_WIDGET_LIST, version, pid, "", list);
        sendData(intent);
    }

    /**
     * 通知WidgetService移除Widget list
     *
     * @param version 本地业务模块的兼容版本号
     * @param pid     Product ID
     * @param list    Widget信息列表字串
     */
    public void notifyWidgetListRemoved(int version, String pid, String list) {
        final Intent intent = createIntent(ACT_REMOVE_WIDGET_LIST, version, pid, "", list);
        sendData(intent);
    }

    /**
     * 通知WidgetService Widget数据有更新
     *
     * @param version 本地业务模块的兼容版本号
     * @param pid     Product ID
     * @param wid     Widget ID
     * @param data    Widget数据字串
     */
    public void notifyWidgetDataUpdated(int version, String pid, String wid, String data) {
        final Intent intent = createIntent(ACT_UPDATE_WIDGET_DATA, version, pid, wid, data);
        sendData(intent);
    }

    /**
     * 通用的组装Intent工具方法
     */
    protected Intent createIntent(int action, int version, String pid, String wid, String data) {
        final Intent intent = new Intent(INTENT_ACTION);
        intent.setPackage(mRemotePackage);
        Connector.putPackage(intent, mContext.getPackageName());
        Connector.putVersion(intent, version);
        Connector.putAction(intent, action);
        Connector.putProductId(intent, pid);
        Connector.putWidgetId(intent, wid);
        Connector.putPayload(intent, data);
        return intent;
    }

    @Override
    public void sendData(Intent intent) {
        Log.i(TAG, "sendData: ", intent);
        Connector.startService(mContext, intent);
    }

}
