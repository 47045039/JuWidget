package com.ju.widget.api;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Widget展开菜单的父类，业务可根据需要扩展此类；
 *
 * 建议菜单页面作为child添加到WidgetMenuView中；
 * 多级菜单就作为多个child一层一层的添加；
 *
 * 重写了dispatchKeyEvent以实现如上场景的多级菜单的BACK按键处理；
 */
public class WidgetMenuView<D extends WidgetData> extends FrameLayout {

    private static final String TAG = "WidgetMenuView";

    protected D mData;

    public WidgetMenuView(Context context) {
        super(context);
    }

    public WidgetMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WidgetMenuView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return handleBack(event);
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 设置关联数据
     *
     * @param data
     */
    void setData(D data) {
        mData = data;
        onDataChanged(data);
    }

    /**
     * 数据发生变化的回调
     *
     * @param data
     */
    protected void onDataChanged(D data) {

    }

    /**
     * 处理BACK按键，返回false时，外层PopupWindow将被dismiss；
     *
     * 如果业务需要自行处理多级菜单的BACK按键处理，请重写该方法；
     *
     * @param event
     * @return
     */
    protected boolean handleBack(KeyEvent event) {
        final int childCount = getChildCount();
        if (childCount <= 1) {
            // 传给外层的PopupWindow的DecorView，触发PopupWindow.dismiss()
            return false;
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN /*&& event.getRepeatCount() == 0*/) {
            final DispatcherState state = getKeyDispatcherState();
            if (state != null) {
                state.startTracking(event, this);
            }
            return true;
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            final DispatcherState state = getKeyDispatcherState();
            if (state != null && state.isTracking(event) && !event.isCanceled()) {
                if (removeMenuPage()) {
                    return true;
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    protected boolean removeMenuPage() {
        final int childCount = getChildCount();
        if (childCount <= 1) {
            // 只剩余一级菜单时，不需要移除
            return false;
        }

        final View topChild = getChildAt(childCount - 1);
        // 最上层的菜单无焦点
        if (!topChild.hasFocus()) {
            return false;
        }

        final View nextTopChild = getChildAt(childCount - 2);

        // 先将焦点转移到下一层菜单，然后再移除上一层菜单
        if (nextTopChild.requestFocus()) {
            removeViewAt(childCount - 1);
            return true;
        }
        return false;
    }

}
