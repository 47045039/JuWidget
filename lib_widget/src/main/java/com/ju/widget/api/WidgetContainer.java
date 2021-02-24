package com.ju.widget.api;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ju.widget.impl.WidgetServer;
import com.ju.widget.interfaces.IWidgetManager;
import com.ju.widget.util.Log;

public class WidgetContainer extends FrameLayout {

    private static final String TAG = "WidgetContainer";

    public WidgetContainer(Context context) {
        super(context);
    }

    public WidgetContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean addWidget(Widget widget) {
        final Context context = getContext();
        final WidgetView view = createWidgetView(context, widget);
        if (view == null) {
            return false;
        }

        final WidgetHostView host = new WidgetHostView(context);
        if (host.attach(widget, view)) {
            WidgetServer.attachWidgetView(widget, view);
            addView(host, getLayoutPrams(findBestPosition(widget), widget.getCellSpan()));
            return true;
        } else {
            return false;
        }
    }

    public boolean removeWidget(WidgetHostView host) {
        if (indexOfChild(host) < 0) {
            return false;
        } else {
            WidgetServer.detachWidgetView(host.getWidget(), host.getWidgetView());
            removeView(host);
            return host.detach();
        }
    }

    private WidgetView createWidgetView(Context context, Widget widget) {
        final IWidgetManager manager = WidgetServer.findWidgetManager(widget.getProductID());
        if (manager != null) {
            return manager.createWidgetView(context, widget);
        } else {
            Log.e(TAG, "Can not find IWidgetManager: ", widget);
            return null;
        }
    }

    private Point findBestPosition(Widget widget) {
        // TODO：根据当前布局和Widget span，查找合适的位置放置Widget
        return new Point(1, 1);
    }

    private LayoutParams getLayoutPrams(Point pos, Point span) {
        // TODO：根据Widget position和span，计算合适的坐标
        final LayoutParams params = new LayoutParams(pos.x * 100, pos.y * 100);
        params.width = span.x * 200;
        params.height = span.y * 200;
        return params;
    }
}
