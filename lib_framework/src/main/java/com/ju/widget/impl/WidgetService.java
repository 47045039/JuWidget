package com.ju.widget.impl;

import android.app.IntentService;
import android.content.Intent;

import com.ju.widget.interfaces.connector.IWidgetServiceConnector;
import com.ju.widget.util.Log;

import static com.ju.widget.interfaces.connector.IWidgetServiceConnector.KEY_PACKAGE;
import static com.ju.widget.interfaces.connector.IWidgetServiceConnector.KEY_PRODUCT_ID;
import static com.ju.widget.interfaces.connector.IWidgetServiceConnector.KEY_WIDGET_ID;

/**
 * 负责接收远端App或者业务模块的数据：
 * 1、Widget的增加、删除；
 * 2、Widget数据刷新等；
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

        final int action = getActionFromIntent(intent);
        final String pkg = getPackageFromIntent(intent);
        final String pid = getProductIdFromIntent(intent);
        final String wid = getWidgetIdFromIntent(intent);
        final String payload = getPayloadFromIntent(intent);

        Log.i(TAG, "onHandleIntent: ", action, pkg, pid, wid, payload);

        switch (action) {
            case IWidgetServiceConnector.ACT_ADD_WIDGET:
                WidgetServer.onAddWidget(pkg, pid, payload);
                return;
            case IWidgetServiceConnector.ACT_REMOVE_WIDGET:
                WidgetServer.onRemoveWidget(pkg, pid, payload);
                return;
            case IWidgetServiceConnector.ACT_ADD_WIDGET_LIST:
                WidgetServer.onAddWidgetList(pkg, pid, payload);
                return;
            case IWidgetServiceConnector.ACT_REMOVE_WIDGET_LIST:
                WidgetServer.onRemoveWidgetList(pkg, pid, payload);
                return;
            case IWidgetServiceConnector.ACT_UPDATE_WIDGET_DATA:
                WidgetServer.onUpdateWidgetData(pkg, pid, wid, payload);
                return;
            default:
                Log.e(TAG, "invalid action: ", action);
                return;
        }
    }

    private int getActionFromIntent(Intent intent) {
        return intent.getIntExtra(IWidgetServiceConnector.KEY_ACTION,
                IWidgetServiceConnector.ACT_INVALID);
    }

    private String getPackageFromIntent(Intent intent) {
        return intent.getStringExtra(KEY_PACKAGE);
    }

    private String getProductIdFromIntent(Intent intent) {
        return intent.getStringExtra(KEY_PRODUCT_ID);
    }

    private String getWidgetIdFromIntent(Intent intent) {
        return intent.getStringExtra(KEY_WIDGET_ID);
    }

    private String getPayloadFromIntent(Intent intent) {
        return intent.getStringExtra(IWidgetServiceConnector.KEY_PAYLOAD);
    }


}
