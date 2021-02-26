package com.ju.widget.impl;

import android.app.IntentService;
import android.content.Intent;

import com.ju.widget.interfaces.IWidgetManager;
import com.ju.widget.interfaces.connector.Connector;
import com.ju.widget.util.Log;

/**
 * 负责接收远端App或者业务模块的数据，并转发给对应的WidgetManager处理；
 *
 * 各业务WidgetManager需要处理好以下场景：
 * 1、Widget的增加、删除；
 * 2、Widget的数据刷新；
 * 3、其他业务自定义的业务逻辑；
 */
public class WidgetService extends IntentService {

    private static final String TAG = "WidgetService";

    public WidgetService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        final int action = Connector.getAction(intent);
        final String pkg = Connector.getPackage(intent);
        final String pid = Connector.getProductId(intent);
        final String wid = Connector.getWidgetId(intent);
        final String payload = Connector.getPayload(intent);

        Log.i(TAG, "onHandleIntent: ", action, pkg, pid, wid, payload);

        final IWidgetManager mgr = WidgetServer.findWidgetManager(pid);
        if (mgr == null) {
            Log.e(TAG, "onHandleIntent can not find IWidgetManager: ", pkg, pid);
            return;
        }

        mgr.onHandleIntent(intent);
    }

}
