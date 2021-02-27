package com.ju.demo.business.aar;

import android.content.Context;

import com.ju.demo.business.aar.widget1.DemoWidget1;
import com.ju.demo.business.aar.widget1.DemoWidgetData1;
import com.ju.demo.business.aar.widget1.DemoWidgetView1;
import com.ju.demo.business.aar.widget2.DemoWidget2;
import com.ju.demo.business.aar.widget2.DemoWidgetData2;
import com.ju.demo.business.aar.widget2.DemoWidgetView2;
import com.ju.widget.api.Constants;
import com.ju.widget.api.Product;
import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetData;
import com.ju.widget.api.WidgetManager;
import com.ju.widget.api.WidgetView;
import com.ju.widget.connector.RemoteBusinessConnector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class DemoWidgetManager extends WidgetManager<RemoteBusinessConnector> {

    private static final String REMOTE_BUSINESS_PACKAGE = "com.ju.demo.business";

    private static final int VERSION = 1;

    public DemoWidgetManager(Context ctx, Product product) {
        super(ctx, product, VERSION);
    }

    @Override
    protected RemoteBusinessConnector createRemoteBusinessConnector() {
        return new RemoteBusinessConnector(mContext, "com.ju.demo.business", VERSION);
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
        final String[] stringArray = payload.split("|");

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

    @Override
    public WidgetView createWidgetView(Context context, Widget widget) {
        // TODO: 根据实际情况创建WidgetView
        if (widget instanceof DemoWidget1) {
            return new DemoWidgetView1(context);
        } else if (widget instanceof DemoWidget2) {
            return new DemoWidgetView2(context);
        } else {
            return null;
        }
    }

    private final Widget randomWidget(String wid) {
        final Random random = new Random();
        if (random.nextBoolean()) {
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
        int span = random.nextInt(3);
        while (span == 0) {
            span = random.nextInt(3);
        }
        return span;
    }

    private final String randomString(String prefix) {
        return prefix + new Random().nextInt();
    }
}
