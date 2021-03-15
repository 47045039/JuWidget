package com.ju.widget.api;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.ju.widget.api.WidgetHostView.WidgetClickListener;
import com.ju.widget.impl.PopupWindow.OnDismissListener;
import com.ju.widget.impl.WidgetCellLayout;
import com.ju.widget.impl.WidgetServer;
import com.ju.widget.util.Log;

/**
 * Widget容器；
 * <p>
 * 需要实现Widget的自动布局、移位、补位、删除等逻辑；
 */
public class WidgetContainer extends WidgetCellLayout implements OnDismissListener, WidgetClickListener {

    private static final String TAG = "WidgetContainer";

    private final Config mCellConfig = new Config();

    // 菜单的外层封装
    private WidgetMenuContainer mMenuContainer = null;

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
            WidgetServer.attachWidgetHostView(host.getWidget(), host);
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child instanceof WidgetHostView) {
            final WidgetHostView host = (WidgetHostView) child;
            WidgetServer.detachWidgetHostView(host.getWidget(), host);
        }
    }

    @Override
    public void onDismiss() {
        // TODO:
    }

    @Override
    public void onHostClick(WidgetHostView host, Widget widget, WidgetView view) {
        if (widget == null) {
            return;
        }

        if (!WidgetEnv.canShowWidgetMenu() || !widget.needShowMenuWhenClick()) {
            widget.doJump(getContext());
        } else {
            final WidgetMenuView menu = widget.createWidgetMenuView(getContext());
            if (menu == null) {
                Log.w(TAG, "Need show menu when click, but can not create WidgetMenuView: ", widget);
                widget.doJump(getContext());
            } else {
                showWidgetMenu(host, widget, menu);
            }
        }
    }

    @Override
    public void onHostLongClick(WidgetHostView host, Widget widget, WidgetView view) {
        if (!isEditMode()) {
            setEditMode(true);
        }
    }

    public void setLayoutConfig(Config config) {
        super.setOriginCellConfig(config.mCellWidth, config.mCellHeight, config.mCellCountX, config.mCellCountY);
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
        final WidgetView view = widget.createWidgetView(context, this);
        if (view == null) {
            return false;
        }

        final WidgetHostView host = new WidgetHostView(context);
        final Point pos = findBestPosition(widget);
        host.setCellPosition(pos.x, pos.y);
        host.setWidgetClickListener(this);

        if (!host.attach(widget, view)) {
            return false;
        }

        final Point span = widget.getCellSpan();
        final LayoutParams params = new LayoutParams(pos.x, pos.y, span.x, span.y);
        addViewToCells(host, -1, -1, params, true);
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

        WidgetServer.detachWidgetHostView(host.getWidget(), host);
        host.detach();

        if (widget == null) {
            return false;
        }

        final WidgetView view = widget.createWidgetView(getContext(), this);
        if (view == null) {
            return false;
        }

        if (host.attach(widget, view)) {
            WidgetServer.attachWidgetHostView(widget, host);
        }

        return true;
    }

    private Point findBestPosition(Widget widget) {
        final int[] pos = new int[]{0, 0};
        if (getVacantCell(pos, widget.mCellSpan.x, widget.mCellSpan.y)) {
            return new Point(pos[0], pos[1]);
        } else {
            // TODO: 扩充高度，并重新获取位置
            return new Point(2, 0);
        }
    }

    private void showWidgetMenu(WidgetHostView host, Widget widget, WidgetMenuView menu) {
        menu.setData(widget.getData());

        final WidgetMenuContainer menuContainer = getMenuContainer();
        menuContainer.setContentView(menu);

        // TODO: 计算坐标和宽高
        final Config config = mCellConfig;
        final int w = getWidth() - 4 * config.mCellWidth;
        final int h = getHeight() - 4 * config.mCellHeight;

        menuContainer.setWidth(w);
        menuContainer.setHeight(h);

        // TODO: 动画效果
        menuContainer.showAsDropDown(host, 0, 0);
    }

    private WidgetMenuContainer getMenuContainer() {
        if (mMenuContainer == null) {
            mMenuContainer = new WidgetMenuContainer(getContext(), this);
        }
        return mMenuContainer;
    }

}
