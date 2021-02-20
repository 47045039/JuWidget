package com.ju.widget.impl.cache;

import android.content.Context;

import com.ju.widget.api.Widget;
import com.ju.widget.interfaces.ICommonCallback;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: 缓存的widget，不能更新数据
 */
public class CachedWidget extends Widget<CachedWidgetData, CachedWidgetView> {

    public CachedWidget(String id) {
        super(id);
    }

    public CachedWidget(String id, int orientation) {
        super(id, orientation);
    }

    public CachedWidget(String id, int spanX, int spanY) {
        super(id, spanX, spanY, -1);
    }

    public CachedWidget(String id, int spanX, int spanY, int orientation) {
        super(id, spanX, spanY, orientation, -1);
    }

    @Override
    protected boolean doUpdate(Context context, ICommonCallback callback) {
        return false;
    }
}
