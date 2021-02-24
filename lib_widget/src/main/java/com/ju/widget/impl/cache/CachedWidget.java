package com.ju.widget.impl.cache;

import com.ju.widget.api.Widget;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: 缓存的widget，不能更新数据
 */
public class CachedWidget extends Widget<CachedWidgetData, CachedWidgetView> {

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
}
