package com.ju.demo.business.aar.widget1;

import com.ju.widget.api.Widget;

public class DemoWidget1 extends Widget<DemoWidgetData1, DemoWidgetView1> {

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
}
