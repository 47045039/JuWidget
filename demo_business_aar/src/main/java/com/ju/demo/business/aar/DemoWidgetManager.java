package com.ju.demo.business.aar;

import android.content.Context;
import android.os.Handler;

import com.ju.demo.business.aar.widget1.DemoWidget1;
import com.ju.demo.business.aar.widget1.DemoWidgetData1;
import com.ju.demo.business.aar.widget2.DemoWidget2;
import com.ju.demo.business.aar.widget2.DemoWidgetData2;
import com.ju.widget.api.Constants;
import com.ju.widget.api.Product;
import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetData;
import com.ju.widget.api.WidgetManager;
import com.ju.widget.connector.RemoteBusinessConnector;
import com.ju.widget.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class DemoWidgetManager extends WidgetManager<RemoteBusinessConnector> {

    private static final String TAG = "DemoWidgetManager";

    private static final String REMOTE_BUSINESS_PACKAGE = "com.ju.demo.business";
    private static final String REMOTE_BUSINESS_CLASS = "com.ju.demo.business.DemoBusiness";

    private static final int VERSION = 1;

    private final Handler mHandler = new Handler();

    public DemoWidgetManager(Context ctx, Product product) {
        super(ctx, product, VERSION);
        testLocalWidget();
    }

    private void testLocalWidget() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "test: ================ 1111: ", DemoWidgetManager.this);
                onAddWidget(mCallback, parseWidget(1, "DemoBusiness_Widget_1"));
                onAddWidget(mCallback, parseWidget(1, "DemoBusiness_Widget_2"));
                onAddWidget(mCallback, parseWidget(1, "DemoBusiness_Widget_3"));

                onAddWidgetList(mCallback, parseWidgetList(1,
                        "DemoBusiness_Widget_4|DemoBusiness_Widget_5|DemoBusiness_Widget_6"));
            }
        }, 3000);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "test: ================ 2222: ", DemoWidgetManager.this);

                onRemoveWidget(mCallback, parseWidget(1, "DemoBusiness_Widget_2"));
                onRemoveWidget(mCallback, parseWidget(1, "DemoBusiness_Widget_3"));

                onRemoveWidgetList(mCallback, parseWidgetList(1,
                        "DemoBusiness_Widget_5|DemoBusiness_Widget_6"));
            }
        }, 6000);
    }

    @Override
    protected RemoteBusinessConnector createRemoteBusinessConnector() {
        return new RemoteBusinessConnector(mContext, VERSION,
                REMOTE_BUSINESS_PACKAGE, REMOTE_BUSINESS_CLASS);
    }

    @Override
    protected boolean notifyUpdateWidgetData(Widget widget) {
        mConnector.notifyUpdateWidgetData(widget);
        return true;
    }

    @Override
    protected Widget parseWidget(int remoteVersion, String payload) {
        // TODO: 根据实际情况解析Widget信息，注意和远端业务模块传递的数据格式匹配
        return randomWidget(payload);
    }

    @Override
    protected ArrayList<Widget> parseWidgetList(int remoteVersion, String payload) {
        // TODO: 根据实际情况解析Widget信息，注意和远端业务模块传递的数据格式匹配
        final ArrayList<Widget> list = new ArrayList<>();
        final String[] stringArray = payload.split("\\|");

        if (stringArray != null) {
            for (String wid : stringArray) {
                list.add(randomWidget(wid));
            }
        }

        return list;
    }

    @Override
    protected WidgetData parseWidgetData(int remoteVersion, Widget widget, String payload) {
        // TODO: 根据实际情况解析Widget数据，注意和远端业务模块传递的数据格式匹配
        try {
            JSONObject object = new JSONObject(payload);
            String key = (remoteVersion == 2 ? "version2" : "version1");

            String title = object.optString(key);

            if (widget instanceof DemoWidget1) {
                return new DemoWidgetData1(title, randomString("演示字符串"));
            } else if (widget instanceof DemoWidget2) {
                return new DemoWidgetData2(title, randomString("http://开头的字符串"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private final Widget randomWidget(String wid) {
        final Random random = new Random();
        if (wid.endsWith("1")) {
            return new DemoWidget1(wid, mProduct.mID,
                    randomSpan(random), randomSpan(random),
                    Constants.ORIENTATION_MASK, -1);
        } else {
            return new DemoWidget2(wid, mProduct.mID,
                    randomSpan(random), randomSpan(random),
                    Constants.ORIENTATION_MASK, -1);
        }
    }

    private final int randomSpan(Random random) {
        int span = random.nextInt(4);
        while (span < 1) {
            span = random.nextInt(4);
        }
        return span;
    }

    private final String randomString(String prefix) {
        return prefix + new Random().nextInt();
    }
}
