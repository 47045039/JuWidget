package com.ju.demo.business.aar.widget2;

import android.content.Context;

import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetContainer;
import com.ju.widget.api.WidgetMenuView;

public class DemoWidget2 extends Widget<DemoWidgetData2, DemoWidgetView2, WidgetMenuView> {

    private static final String TAG = "DemoWidget2";

    public DemoWidget2(String id, String pid) {
        super(id, pid);
    }

    public DemoWidget2(String id, String pid, int orientation) {
        super(id, pid, orientation);
    }

    public DemoWidget2(String id, String pid, int orientation, int interval) {
        super(id, pid, orientation, interval);
    }

    public DemoWidget2(String id, String pid, int spanX, int spanY, int interval) {
        super(id, pid, spanX, spanY, interval);
    }

    public DemoWidget2(String id, String pid, int spanX, int spanY, int orientation, int interval) {
        super(id, pid, spanX, spanY, orientation, interval);
    }

    @Override
    public DemoWidgetView2 createWidgetView(Context context, WidgetContainer container) {
        return new DemoWidgetView2(context);
    }

    @Override
    public WidgetMenuView createWidgetMenuView(Context context) {
        // 不展示二级菜单
        return null;
    }

    @Override
    public boolean doJump(Context context) {
        return false;
    }

    @Override
    public boolean needShowMenuWhenClick() {
        // 不支持二级菜单
        return false;
    }
}
