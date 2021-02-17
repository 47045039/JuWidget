package com.ju.widget.api;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public abstract class WidgetView<D extends Data> extends FrameLayout {

    private final Point mPosition = new Point(-1, -1);
    private final Point mSpan = new Point(-1, -1);
    private D mData;

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

    boolean setPosition(int x, int y) {
        final Point pos = mPosition;
        if (pos.x == x && pos.y == y) {
            return false;
        } else {
            pos.set(x, y);
            return true;
        }
    }

    boolean setSpan(int x, int y) {
        final Point span = mSpan;
        if (span.x == x && span.y == y) {
            return false;
        } else {
            span.set(x, y);
            return true;
        }
    }

    boolean setData(D data) {
        mData = data;
        onDataChanged(data);
        return true;
    }

    /**
     * 数据发生变化的回调，子类需要自己实现界面更新逻辑；
     * 数据为空时，展示默认样式；
     * 数据有效时，根据业务需求展示；
     *
     * @param data
     */
    protected abstract void onDataChanged(D data);

}
