package com.ju.widget.connector;

import android.app.IntentService;
import android.content.Intent;

import com.ju.widget.interfaces.connector.Connector;
import com.ju.widget.interfaces.connector.IRemoteBusinessConnector;
import com.ju.widget.util.Log;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/25
 * @Description: 远端业务实现的基类
 */
public abstract class AbsRemoteBusiness extends IntentService {

    private static final String TAG = "AbsRemoteBusiness";

    protected final WidgetServiceConnector mWidgetService;

    public AbsRemoteBusiness(String name) {
        super(name);
        mWidgetService = new WidgetServiceConnector(getApplication());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int command = Connector.getCommand(intent);
        final String params = Connector.getParams(intent);

        Log.i(TAG, "onHandleIntent: ", command, params);

        switch (command) {
            case IRemoteBusinessConnector.CMD_UPDATE_WIDGET_DATA:
                updateWidgetData(params);
                return;
            default:
                Log.e(TAG, "invalid command: ", command);
                return;
        }
    }

    /**
     * 根据业务需求，实现异步更新Widget数据的逻辑
     */
    protected abstract void updateWidgetData(String params);

    /**
     * Widget数据更新完成后，回传数据给WidgetService
     *
     * @param data
     */
    protected void onWidgetDataUpdated(String data) {
        mWidgetService.notifyWidgetDataUpdated(data);
    }
}
