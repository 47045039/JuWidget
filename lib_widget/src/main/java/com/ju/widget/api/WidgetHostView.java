package com.ju.widget.api;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ju.widget.impl.WidgetServer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class WidgetHostView<D extends WidgetData, V extends WidgetView<D>, W extends Widget<D, V>>
        extends FrameLayout {

    private static final String TAG = "WidgetHostView";

    private static final LayoutParams PARAMS = new LayoutParams(MATCH_PARENT, MATCH_PARENT);

    private final Point mEnlargeCellSpan = new Point(-1, -1);   // 扩展后的CellSpan

    private D mWidgetData;
    private V mWidgetView;
    private W mWidget;

    public WidgetHostView(Context context) {
        super(context);
    }

    public WidgetHostView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetHostView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WidgetHostView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    boolean setEnlargeCellSpan(int x, int y) {
        final Point span = mEnlargeCellSpan;
        if (span.x == x && span.y == y) {
            return false;
        } else {
            span.set(x, y);
            return true;
        }
    }

    Point getEnlargeCellSpan() {
        return new Point(mEnlargeCellSpan);
    }

    Point getOriginCellSpan() {
        if (mWidget != null) {
            return mWidget.getCellSpan();
        }
        return new Point(-1, -1);
    }

    W getWidget() {
        return mWidget;
    }

    V getWidgetView() {
        return mWidgetView;
    }

    boolean attach(W w, V view) {
        if (w == null || view == null) {
            return false;
        }

        if (getChildCount() > 0) {
            removeAllViewsInLayout();
        }

        mWidget = w;
        mWidgetView = view;

        if (mWidgetData != null) {
            setData(mWidgetData);
        }

        addView(view, -1, PARAMS);
        return true;
    }

    boolean detach(/*W widget, V view*/) {
        /*if (mWidget != widget || mWidgetView != view) {
            return false;
        }*/

        mWidget = null;
        mWidgetView = null;
        removeAllViews();
        return true;
    }

    boolean setData(D data) {
        mWidgetData = data;
        if (mWidgetView != null) {
            mWidgetView.setWidgetData(data);
        }
        return true;
    }

}
