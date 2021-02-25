package com.ju.widget.impl;

import android.app.IntentService;
import android.content.Intent;

import com.ju.widget.util.Log;

import static com.ju.widget.api.Constants.ACT_ADD_WIDGET;
import static com.ju.widget.api.Constants.ACT_ADD_WIDGET_LIST;
import static com.ju.widget.api.Constants.ACT_INVALID;
import static com.ju.widget.api.Constants.ACT_REMOVE_WIDGET;
import static com.ju.widget.api.Constants.ACT_REMOVE_WIDGET_LIST;
import static com.ju.widget.api.Constants.ACT_UPDATE_WIDGET_DATA;
import static com.ju.widget.api.Constants.KEY_ACTION;
import static com.ju.widget.api.Constants.KEY_PACKAGE;
import static com.ju.widget.api.Constants.KEY_PAYLOAD;
import static com.ju.widget.api.Constants.KEY_PRODUCT_ID;
import static com.ju.widget.api.Constants.KEY_WIDGET_ID;

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

        Log.e(TAG, "onHandleIntent: ", action, pkg, pid, wid, payload);

        switch (action) {
            case ACT_ADD_WIDGET:
                WidgetServer.onAddWidget(pkg, pid, payload);
                return;
            case ACT_REMOVE_WIDGET:
                WidgetServer.onRemoveWidget(pkg, pid, payload);
                return;
            case ACT_ADD_WIDGET_LIST:
                WidgetServer.onAddWidgetList(pkg, pid, payload);
                return;
            case ACT_REMOVE_WIDGET_LIST:
                WidgetServer.onRemoveWidgetList(pkg, pid, payload);
                return;
            case ACT_UPDATE_WIDGET_DATA:
                WidgetServer.onUpdateWidgetData(pkg, pid, wid, payload);
                return;
            default:
                Log.e(TAG, "invalid action: ", action);
                return;
        }
    }

    private int getActionFromIntent(Intent intent) {
        return intent.getIntExtra(KEY_ACTION, ACT_INVALID);
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
        return intent.getStringExtra(KEY_PAYLOAD);
    }


}
