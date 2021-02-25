package com.ju.widget.api;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Widget界面的外层框，用于和WidgetContainer交互；
 *
 * 处理点击事件、编辑模式等
 *
 */
public class WidgetHostView extends FrameLayout {

    private static final String TAG = "WidgetHostView";

    private static final LayoutParams PARAMS = new LayoutParams(MATCH_PARENT, MATCH_PARENT);

    private final Point mEnlargeCellSpan = new Point(-1, -1);   // 扩展后的CellSpan

    private boolean mEditMode = false;


    private Widget mWidget;
    private WidgetView mWidgetView;

    public WidgetHostView(Context context) {
        this(context, null, 0, 0);
    }

    public WidgetHostView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public WidgetHostView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WidgetHostView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(false);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mEditMode) {
            // TODO: 绘制删除角标；
            // TODO: 焦点状态绘制方向图标
        }
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

    /**
     * 进入、退出编辑模式
     *
     * @param edit
     */
    void setEditMode(boolean edit) {
        if (edit != mEditMode) {
            mEditMode = edit;
            invalidate();
            requestLayout();
        }
    }


    Widget getWidget() {
        return mWidget;
    }

    WidgetView getWidgetView() {
        return mWidgetView;
    }

    boolean attach(Widget w, WidgetView view) {
        if (w == null || view == null) {
            return false;
        }

        if (getChildCount() > 0) {
            removeAllViewsInLayout();
        }

        mWidget = w;
        mWidgetView = view;

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

}
