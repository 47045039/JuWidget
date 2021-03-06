package com.ju.widget.api;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Widget的界面展示；
 *
 * 各业务需要以此为父类创建自己的Widget界面；
 * 重写onDataChanged以更新界面，实现业务显示效果；
 *
 * @param <D> WidgetView关联的数据结构
 */
public abstract class WidgetView<D extends WidgetData> extends FrameLayout {

    protected D mWidgetData;

    public WidgetView(Context context) {
        super(context);
    }

    public WidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WidgetView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * 设置关联数据
     *
     * @param data
     */
    void setData(D data) {
        mWidgetData = data;
        onDataChanged(data);
    }

    /**
     * 数据发生变化的回调，子类需要自己实现界面更新逻辑；
     * 数据为空时，展示默认样式；
     * 数据有效时，根据业务需求展示；
     *
     * @param data
     */
    protected abstract void onDataChanged(D data);

    /**
     * showing = true，处于视口内；
     * showing = false，处于视口外；
     *
     * @param showing
     */
    protected abstract void onVisibleChanged(boolean showing);
}
