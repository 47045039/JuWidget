package com.ju.demo.business.aar.widget2;

import android.content.Context;
import android.util.AttributeSet;

import com.ju.widget.api.WidgetView;

public class DemoWidgetView2 extends WidgetView<DemoWidgetData2> {

    public DemoWidgetView2(Context context) {
        super(context);
    }

    public DemoWidgetView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DemoWidgetView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DemoWidgetView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDataChanged(DemoWidgetData2 data) {
        // TODO: 更新界面
    }

    @Override
    protected void onVisibleChanged(boolean showing) {

    }
}
