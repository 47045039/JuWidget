package com.ju.widget.impl.cache;

import android.graphics.Bitmap;

import com.ju.widget.api.WidgetData;
import com.ju.widget.impl.WidgetLoader;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: 缓存的widget数据
 */
public class CachedWidgetData extends WidgetData {

    private Bitmap mBackground;         // 背景图片

    public CachedWidgetData(String id, String title) {
        super(id, title);
    }

    public Bitmap getBackground() {
        if (mBackground == null) {
            // TODO: 从数据库中加载
            return mBackground = WidgetLoader.readWidgetBackground(this);
        } else {
            return mBackground;
        }
    }

}
