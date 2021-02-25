package com.ju.widget.demo;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.ju.widget.time.ActionFromTimesWidgetApp;
import com.ju.widget.time.ActionFromTimesWidgetView;
import com.ju.widget.time.Constant;
import com.ju.widget.time.ReqFromWidgetTimesApp;
import com.ju.widget.time.ReqFromWidgetView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * 接收来自widget view的请求
 */
public class IntentService2ReceiveReqFromWidgetView extends IntentService {

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.ju.widget.demo.action.FOO";
    private static final String ACTION_BAZ = "com.ju.widget.demo.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_REQ_PARAM = "com.ju.widget.demo.extra.request.PARAM";
    public static final String REQUEST_PARAM = "com.ju.widget.framework.extra.request.PARAM";
    private static final String EXTRA_PARAM1 = "com.ju.widget.demo.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.ju.widget.demo.extra.PARAM2";
    public static final String FRAMEWORK_PKG = "com.ju.widget.framework";
    public static final String FRAMEWORK_SERVICE = "com.ju.widget.framework.IntentService2ReceiveReqFromWidgetApp";
    private ComponentName component = new ComponentName(FRAMEWORK_PKG, FRAMEWORK_SERVICE);
    private ReqFromWidgetView reqFromWidgetView;
    private ActionFromTimesWidgetView actionFromTimesWidgetView;
    private ReqFromWidgetTimesApp reqFromWidgetTimesApp;
    private ActionFromTimesWidgetApp actionFromTimesWidgetApp;

    public IntentService2ReceiveReqFromWidgetView() {
        super("IntentService2ReceiveReqFromWidgetView");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, ReqFromWidgetView param1, String param2) {
        /*Intent intent = new Intent(context, IntentService2ReceiveReqFromWidgetAar.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);*/
        String pkg = "com.ju.widget.demo";
        String cls = "com.ju.widget.demo.IntentService2ReceiveReqFromWidgetAar";
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
        /*获取aar WIDGET VIEW传递过来的请求，解析请求*/
        String stringExtra = intent.getStringExtra(EXTRA_REQ_PARAM);
        if(TextUtils.isEmpty(stringExtra))
            return;
        reqFromWidgetView = new Gson().fromJson(stringExtra, ReqFromWidgetView.class);
        if (Constant.WIDGET_TIMES.equals(reqFromWidgetView.widgetId)) {
            actionFromTimesWidgetView = reqFromWidgetView.action;
            if(actionFromTimesWidgetView instanceof ActionFromTimesWidgetView){
                handle(reqFromWidgetView.widgetId, actionFromTimesWidgetView);
            }
        }
    }

    private void handle(String widgetId, ActionFromTimesWidgetView action) {
        boolean updateTime = action.updateTime;
        if (updateTime) {
            updateTime2framework(widgetId);
        }
    }

    private void updateTime2framework(String widgetId) {
        Intent intent = new Intent();
        intent.setComponent(component);
        reqFromWidgetTimesApp = new ReqFromWidgetTimesApp();
        reqFromWidgetTimesApp.widgetAppPkgName = getApplicationContext().getPackageName();
        reqFromWidgetTimesApp.widgetId = widgetId;
        actionFromTimesWidgetApp = new ActionFromTimesWidgetApp();
        actionFromTimesWidgetApp.currentTime = getCurrentTime();
        reqFromWidgetTimesApp.action = actionFromTimesWidgetApp;
        intent.putExtra(REQUEST_PARAM, reqFromWidgetTimesApp);
        getApplication().startService(intent);
    }

    private String getCurrentTime() {
        Date dd=new Date();
        SimpleDateFormat sim=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sim.format(dd);
    }
}