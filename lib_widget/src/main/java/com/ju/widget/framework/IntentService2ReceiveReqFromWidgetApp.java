package com.ju.widget.framework;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.ju.widget.IWidgetServer;
import com.ju.widget.IWidgetView;
import com.ju.widget.ReqFromAppBase;
import com.ju.widget.impl.WidgetServer;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * 接收来自widget app的请求
 */
public class IntentService2ReceiveReqFromWidgetApp extends IntentService {

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.ju.widget.demo.action.FOO";
    private static final String ACTION_BAZ = "com.ju.widget.demo.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_REQ_PARAM = "com.ju.widget.framework.extra.request.PARAM";
    private static final String EXTRA_PARAM1 = "com.ju.widget.demo.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.ju.widget.demo.extra.PARAM2";
    private IWidgetServer iWidgetServer;
    private ReqFromAppBase reqFromAppBase;

    public IntentService2ReceiveReqFromWidgetApp() {
        super("IntentService2ReceiveReqFromWidgetAar");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        /*Intent intent = new Intent(context, IntentService2ReceiveReqFromWidgetAar.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);*/
        String pkg = "com.ju.widget.framework";
        String cls = "com.ju.widget.framework.IntentService2ReceiveReqFromWidgetApp";
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(pkg, cls));
        intent.putExtra(EXTRA_REQ_PARAM, param1);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        /*Intent intent = new Intent(context, IntentService2ReceiveReqFromWidgetAar.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);*/
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /*if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }*/
        if (intent == null)
            return;
        /*获取app传递过来的请求，解析请求*/
        String stringExtra = intent.getStringExtra(EXTRA_REQ_PARAM);
        reqFromAppBase = new Gson().fromJson(stringExtra, ReqFromAppBase.class);
        updateWidgetView(reqFromAppBase, stringExtra);
    }

    private void updateWidgetView(ReqFromAppBase reqFromAppBase, String stringExtra) {
        /**/
        String pkgName = reqFromAppBase.widgetAppPkgName;
        String widgetId = reqFromAppBase.widgetId;
        if (iWidgetServer==null) {
            iWidgetServer = WidgetServer.getInstance(getApplicationContext());
        }
        List<IWidgetView> shownWidgetList = iWidgetServer.getShownWidgetList();
        for (IWidgetView iWidgetView: shownWidgetList){
            if(iWidgetView.getPkgName().equals(pkgName)&&iWidgetView.getWidgetId().equals(widgetId))
                iWidgetView.update(stringExtra);
        }
    }

    private void handleTimesWidgetView(String stringExtra) {
    }
}