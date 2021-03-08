package com.ju.widget.connector;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ju.widget.api.Widget;
import com.ju.widget.interfaces.connector.Connector;
import com.ju.widget.interfaces.connector.IRemoteBusinessConnector;
import com.ju.widget.util.Log;

public class RemoteBusinessConnector implements IRemoteBusinessConnector {

    private static final String TAG = "RemoteBusinessConnector";

    private final Context mContext;
    private final int mVersion;

    private final String mRemotePackage;
    private final String mRemoteClass;
    private final String mIntentAction;

    public RemoteBusinessConnector(Context ctx, int version, String pkg, String cls) {
        mContext = ctx.getApplicationContext();
        mVersion = version;

        mRemotePackage = pkg;
        mRemoteClass = cls;
        mIntentAction = INTENT_ACTION_PREFIX + pkg;
    }

    /**
     * 通知远端业务模块更新Widget数据
     *
     * @param widget
     */
    public void notifyUpdateWidgetData(Widget widget) {
        final Intent intent = new Intent(mIntentAction);
        intent.setClassName(mRemotePackage, mRemoteClass);

        Connector.putPackage(intent, mContext.getPackageName());
        Connector.putVersion(intent, mVersion);
        Connector.putCommand(intent, CMD_UPDATE_WIDGET_DATA);
        Connector.putProductId(intent, widget.getProductID());
        Connector.putWidgetId(intent, widget.getID());
        Connector.putParams(intent, widget.getID());

        sendCommand(intent);
    }

    @Override
    public void sendCommand(Intent intent) {
        Log.i(TAG, "sendCommand: ", intent);
        Connector.startService(mContext, intent);
    }
}
