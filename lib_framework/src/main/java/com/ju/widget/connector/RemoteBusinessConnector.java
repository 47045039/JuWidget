package com.ju.widget.connector;

import android.content.Context;
import android.content.Intent;

import com.ju.widget.api.Widget;
import com.ju.widget.interfaces.connector.Connector;
import com.ju.widget.interfaces.connector.IRemoteBusinessConnector;

public class RemoteBusinessConnector implements IRemoteBusinessConnector {

    private final Context mContext;
    private final String mPackage;
    private final String mIntentAction;

    public RemoteBusinessConnector(Context ctx, String pkg) {
        mContext = ctx.getApplicationContext();
        mPackage = pkg;
        mIntentAction = INTENT_ACTION_PREFIX + pkg;
    }

    /**
     * 通知远端业务模块更新Widget数据
     *
     * @param widget
     */
    public void notifyUpdateWidgetData(Widget widget) {
        final Intent intent = new Intent(mIntentAction);
        Connector.putPackage(intent, mPackage);
        Connector.putCommand(intent, CMD_UPDATE_WIDGET_DATA);
        Connector.putProductId(intent, widget.getProductID());
        Connector.putWidgetId(intent, widget.getID());
        Connector.putParams(intent, widget.getID());
        sendCommand(intent);
    }

    @Override
    public void sendCommand(Intent intent) {
        mContext.startService(intent);
    }
}
