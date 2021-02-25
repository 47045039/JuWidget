package com.ju.widget.api;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.ju.widget.impl.WidgetServer;

/**
 * Widget容器；
 *
 * 需要实现Widget的自动布局、移位、补位、删除等逻辑；
 */
public class WidgetContainer extends FrameLayout {

    private static final String TAG = "WidgetContainer";

    private boolean mEditMode = false;

    public WidgetContainer(Context context) {
        this(context, null, 0);
    }

    public WidgetContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidgetContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mEditMode) {
            // TODO: 绘制Cell框格
        }
    }

    /**
     * 进入、退出编辑模式
     *
     * @param edit
     */
    public void setEditMode(boolean edit) {
        if (edit != mEditMode) {
            mEditMode = edit;

            View view = null;
            for (int i = getChildCount() - 1; i >= 0; i--) {
                view = getChildAt(i);
                if (view instanceof WidgetHostView) {
                    ((WidgetHostView) view).setEditMode(edit);
                }
            }
        }
    }

    public boolean addWidget(Widget widget) {
        final Context context = getContext();
        final WidgetView view = WidgetServer.createWidgetView(context, widget);
        if (view == null) {
            return false;
        }

        final WidgetHostView host = new WidgetHostView(context);
        if (!host.attach(widget, view)) {
            return false;
        }

        WidgetServer.attachWidgetView(widget, view);
        addView(host, getLayoutPrams(findBestPosition(widget), widget.getCellSpan()));
        return true;
    }

    public boolean removeWidget(WidgetHostView host) {
        if (indexOfChild(host) < 0) {
            return false;
        }

        WidgetServer.detachWidgetView(host.getWidget(), host.getWidgetView());
        removeView(host);
        return host.detach();
    }

    public boolean replaceWidget(WidgetHostView host, Widget widget) {
        if (indexOfChild(host) < 0) {
            return false;
        }

        WidgetServer.detachWidgetView(host.getWidget(), host.getWidgetView());
        host.detach();

        final WidgetView view = WidgetServer.createWidgetView(getContext(), widget);
        if (view == null) {
            return false;
        }

        if (host.attach(widget, view)) {
            WidgetServer.attachWidgetView(widget, view);
        }

        return true;
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
