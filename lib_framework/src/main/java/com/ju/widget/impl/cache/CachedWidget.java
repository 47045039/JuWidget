package com.ju.widget.impl.cache;

import android.content.Context;

import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetContainer;
import com.ju.widget.api.WidgetMenuView;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: 缓存的widget，不能更新数据
 */
public class CachedWidget extends Widget<CachedWidgetData, CachedWidgetView, WidgetMenuView> {

    public CachedWidget(String id, String pid) {
        super(id, pid);
    }

    public CachedWidget(String id, String pid, int orientation) {
        super(id, pid, orientation);
    }

    public CachedWidget(String id, String pid, int spanX, int spanY) {
        super(id, pid, spanX, spanY, -1);
    }

    public CachedWidget(String id, String pid, int spanX, int spanY, int orientation) {
        super(id, pid, spanX, spanY, orientation, -1);
    }

    @Override
    public CachedWidgetView createWidgetView(Context context, WidgetContainer container) {
        return new CachedWidgetView(context);
    }

    @Override
    public WidgetMenuView createWidgetMenuView(Context context) {
        return new WidgetMenuView(context);
    }

    @Override
    public boolean doJump(Context context) {
        if (mData == null) {
            return false;
        } else {
            // TODO: 使用cache数据跳转
            return true;
        }
    }

    @Override
    public boolean needShowMenuWhenClick() {
        return false;
    }
}
