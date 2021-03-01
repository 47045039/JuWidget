package com.ju.demo.business.aar.widget1;

import android.content.Context;

import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetContainer;
import com.ju.widget.util.Log;

public class DemoWidget1 extends Widget<DemoWidgetData1, DemoWidgetView1, DemoWidgetMenuView1> {

    private static final String TAG = "DemoWidget1";

    public DemoWidget1(String id, String pid) {
        super(id, pid);
    }

    public DemoWidget1(String id, String pid, int orientation) {
        super(id, pid, orientation);
    }

    public DemoWidget1(String id, String pid, int orientation, int interval) {
        super(id, pid, orientation, interval);
    }

    public DemoWidget1(String id, String pid, int spanX, int spanY, int interval) {
        super(id, pid, spanX, spanY, interval);
    }

    public DemoWidget1(String id, String pid, int spanX, int spanY, int orientation, int interval) {
        super(id, pid, spanX, spanY, orientation, interval);
    }

    @Override
    public DemoWidgetView1 createWidgetView(Context context, WidgetContainer container) {
        final DemoWidgetView1 view =  new DemoWidgetView1(context);
        Log.v(TAG, "createWidgetView: ", context, view);
        return view;
    }

    @Override
    public DemoWidgetMenuView1 createWidgetMenuView(Context context) {
        final DemoWidgetMenuView1 view =  new DemoWidgetMenuView1(context);
        Log.v(TAG, "createWidgetMenuView: ", context, view);
        return view;
    }

    @Override
    public boolean doJump(Context context) {
        Log.v(TAG, "doJump: ", context);
        // TODO: 执行跳转
        return false;
    }

    @Override
    public boolean needShowMenuWhenClick() {
        Log.v(TAG, "needShowMenuWhenClick: ", mData);
        // TODO：根据实际数据，判断是否需要展示二级菜单
        return (mData != null && mData.randomString != null);
    }
}
