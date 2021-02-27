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
    private boolean mShowing = false;

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

    /**
     * 设置扩展的跨度，仅用于自动补位场景；
     */
    boolean setEnlargeCellSpan(int x, int y) {
        final Point span = mEnlargeCellSpan;
        if (span.x == x && span.y == y) {
            return false;
        } else {
            span.set(x, y);
            return true;
        }
    }

    /**
     * 获取扩展跨度
     */
    Point getEnlargeCellSpan() {
        return new Point(mEnlargeCellSpan);
    }

    /**
     * 获取原始跨度
     */
    Point getOriginCellSpan() {
        if (mWidget != null) {
            return mWidget.getCellSpan();
        }
        return new Point(-1, -1);
    }

    /**
     * 进入、退出编辑模式
     */
    void setEditMode(boolean edit) {
        if (edit != mEditMode) {
            mEditMode = edit;

            invalidate();
            requestLayout();
        }
    }

    /**
     * 设置可见性；
     *
     * 在WidgetContainer滚动时：
     * 1、WidgetHostView处于视口内，showing = true
     * 2、WidgetHostView处于视口外，showing = false
     */
    void setVisible(boolean showing) {
        if (showing != mShowing) {
            mShowing = showing;

            if (mWidgetView != null) {
                mWidgetView.onVisibleChanged(showing);
            }

            invalidate();
            requestLayout();
        }
    }

    /**
     * 获取当前关联的Widget
     */
    Widget getWidget() {
        return mWidget;
    }

    /**
     * 获取当前关联的WidgetView
     */
    WidgetView getWidgetView() {
        return mWidgetView;
    }

    /**
     * 关联Widget、WidgetView信息，并将WidgetView加入到view层级
     */
    boolean attach(Widget widget, WidgetView view) {
        if (widget == null || view == null) {
            return false;
        }

        if (getChildCount() > 0) {
            removeAllViewsInLayout();
        }

        mWidget = widget;
        mWidgetView = view;

        addView(view, -1, PARAMS);

        view.onVisibleChanged(mShowing);

        return true;
    }

    /**
     * 解除Widget、WidgetView关联关系，并将WidgetView移除
     */
    boolean detach(/*W widget, V view*/) {
        /*if (mWidget != widget || mWidgetView != view) {
            return false;
        }*/

        if (mWidgetView != null) {
            mWidgetView.onVisibleChanged(false);
        }

        mWidget = null;
        mWidgetView = null;
        removeAllViews();
        return true;
    }

}
