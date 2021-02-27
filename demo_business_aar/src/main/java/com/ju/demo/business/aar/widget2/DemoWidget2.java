package com.ju.demo.business.aar.widget2;

import com.ju.widget.api.Widget;

public class DemoWidget2 extends Widget<DemoWidgetData2, DemoWidgetView2> {

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
}
