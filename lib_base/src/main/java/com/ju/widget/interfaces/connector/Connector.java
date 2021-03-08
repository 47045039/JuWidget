package com.ju.widget.interfaces.connector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ju.widget.util.Log;

public class Connector {

    private static final String TAG = "Connector";

    /**
     * 框架WidgetService intent action
     */
    public static final String INTENT_ACTION_WIDGET_SERVICE = "com.ju.widget.intent.action.WIDGET_SERVICE";

    /**
     * 框架WidgetService permission
     */
    public static final String PERMISSION_WIDGET_SERVICE = "com.ju.widget.permission.WIDGET_SERVICE";

    /**
     * 与远端业务模块通信的intent action前缀（多个业务模块须分开，所以要拼接业务模块的包名）
     */
    public static final String INTENT_ACTION_WIDGET_BUSINESS = "com.ju.widget.intent.action.WIDGET_BUSINESS.";

    /**
     * 与远端业务模块通信的permission前缀（多个业务模块须分开，所以要拼接业务模块的包名）
     */
    public static final String PERMISSION_WIDGET_BUSINESS = "com.ju.widget.permission.WIDGET_BUSINESS.";

    /**
     * Intent中数据的key常量定义
     */
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_VERSION = "version";
    public static final String KEY_COMMAND = "command";
    public static final String KEY_ACTION = "action";
    public static final String KEY_PRODUCT_ID = "product_id";
    public static final String KEY_WIDGET_ID = "widget_id";
    public static final String KEY_PARAMS = "params";
    public static final String KEY_PAYLOAD = "payload";

    /**
     * 无效的远端业务的命令
     */
    public static final int CMD_INVALID = -1;

    /**
     * 无效的Action
     */
    public static final int ACT_INVALID = -1;

    /**
     * 默认Product version
     */
    public static final int VERSION_DEFAULT = 0;

    public static final String getPackage(Intent intent) {
        return intent.getStringExtra(KEY_PACKAGE);
    }

    public static final void putPackage(Intent intent, String pkg) {
        intent.putExtra(KEY_PACKAGE, pkg);
    }

    public static final int getVersion(Intent intent) {
        return intent.getIntExtra(KEY_VERSION, VERSION_DEFAULT);
    }

    public static final void putVersion(Intent intent, int version) {
        intent.putExtra(KEY_VERSION, version);
    }

    public static final int getCommand(Intent intent) {
        return intent.getIntExtra(KEY_COMMAND, CMD_INVALID);
    }

    public static final void putCommand(Intent intent, int cmd) {
        intent.putExtra(KEY_COMMAND, cmd);
    }

    public static final int getAction(Intent intent) {
        return intent.getIntExtra(KEY_ACTION, ACT_INVALID);
    }

    public static final void putAction(Intent intent, int act) {
        intent.putExtra(KEY_ACTION, act);
    }

    public static final String getParams(Intent intent) {
        return intent.getStringExtra(KEY_PARAMS);
    }

    public static final void putParams(Intent intent, String params) {
        intent.putExtra(KEY_PARAMS, params);
    }

    public static final String getProductId(Intent intent) {
        return intent.getStringExtra(KEY_PRODUCT_ID);
    }

    public static final void putProductId(Intent intent, String pid) {
        intent.putExtra(KEY_PRODUCT_ID, pid);
    }

    public static final String getWidgetId(Intent intent) {
        return intent.getStringExtra(KEY_WIDGET_ID);
    }

    public static final void putWidgetId(Intent intent, String wid) {
        intent.putExtra(KEY_WIDGET_ID, wid);
    }

    public static final String getPayload(Intent intent) {
        return intent.getStringExtra(KEY_PAYLOAD);
    }

    public static final void putPayload(Intent intent, String payload) {
        intent.putExtra(KEY_PAYLOAD, payload);
    }

    public static final void startService(Context context, Intent intent) {
        Log.w(TAG, "startService: ", Build.VERSION.SDK_INT, context, intent);

        // IntentService和startForegroundService有冲突，不能这样调用
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(intent);
        } else {
            context.startForegroundService(intent);
        }
    }

    public static final void startForeground(Service service) {
        Log.w(TAG, "startForeground: ", Build.VERSION.SDK_INT, service);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        final NotificationManager nm = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            Log.e(TAG, "NotificationManager is null.");
            return;
        }

        final String name = service.getClass().getName();
        final Notification.Builder builder = new Notification.Builder(service.getApplicationContext())
                .setContentTitle("通知").setSmallIcon(R.mipmap.widget_base_icon_notification);
        // 解决8.0及以上系统的报错
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(name, name,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            nm.createNotificationChannel(channel);
            builder.setChannelId(name);
        }

        try {
            final Notification notification = builder.build();
            service.startForeground(name.hashCode(), notification);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "startForeground failed:", e);
        }
    }

    public static final void stopForeground(Service service) {
        Log.w(TAG, "stopForeground: ", Build.VERSION.SDK_INT, service);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        try {
            service.stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "stopForeground failed:", e);
        }
    }

}
