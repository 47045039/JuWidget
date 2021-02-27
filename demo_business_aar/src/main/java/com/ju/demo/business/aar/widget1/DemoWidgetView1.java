package com.ju.demo.business.aar.widget1;

import android.content.Context;
import android.util.AttributeSet;

import com.ju.widget.api.WidgetView;

public class DemoWidgetView1 extends WidgetView<DemoWidgetData1> {

    public DemoWidgetView1(Context context) {
        super(context);
    }

    public DemoWidgetView1(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DemoWidgetView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DemoWidgetView1(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDataChanged(DemoWidgetData1 data) {

    }

    @Override
    protected void onVisibleChanged(boolean showing) {

    }
}
