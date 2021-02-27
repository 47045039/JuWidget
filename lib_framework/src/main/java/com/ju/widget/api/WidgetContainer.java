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

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof WidgetHostView) {
            final WidgetHostView host = (WidgetHostView) child;
            WidgetServer.attachWidgetView(host.getWidget(), host.getWidgetView());
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child instanceof WidgetHostView) {
            final WidgetHostView host = (WidgetHostView) child;
            WidgetServer.detachWidgetView(host.getWidget(), host.getWidgetView());
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

    /**
     * 是否编辑模式
     */
    public boolean isEditMode() {
        return mEditMode;
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

        addView(host, getLayoutPrams(findBestPosition(widget), widget.getCellSpan()));
        return true;
    }

    public boolean removeWidget(WidgetHostView host) {
        if (indexOfChild(host) < 0) {
            return false;
        }

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
        final LayoutParams params = new LayoutParams(span.x * 160, span.y * 160);
        params.leftMargin = pos.x * 10;
        params.topMargin = pos.y * 10;
        return params;
    }
}
