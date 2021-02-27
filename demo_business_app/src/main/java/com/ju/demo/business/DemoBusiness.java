package com.ju.demo.business;

import android.os.Handler;

import com.ju.widget.connector.AbsBusiness;

import org.json.JSONException;
import org.json.JSONObject;

public class DemoBusiness extends AbsBusiness {

    private static final String PRODUCT_ID = "DemoBusinessID";
    private static final int VERSION = 2;

    private final Handler mHandler = new Handler();

    public DemoBusiness() {
        super("DemoBusiness", PRODUCT_ID, VERSION);
        testConnector();
    }

    private void testConnector() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyWidgetAdded("DemoBusiness_Widget_1");
                notifyWidgetAdded("DemoBusiness_Widget_2");
                notifyWidgetAdded("DemoBusiness_Widget_3");

                notifyWidgetListAdded("DemoBusiness_Widget_4|DemoBusiness_Widget_5|DemoBusiness_Widget_6");
            }
        }, 2000);


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyWidgetRemoved("DemoBusiness_Widget_2");
                notifyWidgetListRemoved("DemoBusiness_Widget_5|DemoBusiness_Widget_6");
            }
        }, 10000);
    }

    @Override
    protected void updateWidgetData(int remoteVersion, String pid, String wid, String params) {
        // TODO: 根据widget id查询数据，然后回调；注意处理好多版本的数据兼容；
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final int compatVersion = compatBusinessVersion(remoteVersion, mVersion);
                if (compatVersion == 1) {
                    version1(pid, wid, params);
                } else if (compatVersion == 2) {
                    version2(pid, wid, params);
                } else {
                    version0(pid, wid, params);
                }
            }
        }, 2000);
    }

    private void version1(String pid, String wid, String params) {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("version1", "version 1 data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyWidgetDataUpdated(1, pid, wid, obj.toString());
    }

    private void version2(String pid, String wid, String params) {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("version2", "version 2 data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyWidgetDataUpdated(2, pid, wid, obj.toString());
    }

    private void version0(String pid, String wid, String params) {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("version0", "version 0 data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyWidgetDataUpdated(0, pid, wid, obj.toString());
    }
}
