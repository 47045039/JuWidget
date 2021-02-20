package com.ju.widget.impl;

import com.ju.widget.api.Product;
import com.ju.widget.api.Widget;
import com.ju.widget.interfaces.IWidgetCallback;

import java.util.ArrayList;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: IWidgetCallback代理
 *
 * 用于缓存Widget数据
 */
public class WidgetCallbackProxy implements IWidgetCallback {

    private final IWidgetCallback mBase;

    public WidgetCallbackProxy(IWidgetCallback mBase) {
        this.mBase = mBase;
    }

    @Override
    public void onWidgetAdded(Product product, Widget widget) {
        WidgetLoader.saveWidget(product, widget);
        mBase.onWidgetAdded(product, widget);
    }

    @Override
    public void onWidgetRemoved(Product product, Widget widget) {
        WidgetLoader.deleteWidget(product, widget);
        mBase.onWidgetAdded(product, widget);
    }

    @Override
    public void onWidgetAdded(Product product, ArrayList<Widget> list) {
        WidgetLoader.saveWidget(product, list);
        mBase.onWidgetAdded(product, list);
    }

    @Override
    public void onWidgetRemoved(Product product, ArrayList<Widget> list) {
        WidgetLoader.deleteWidget(product, list);
        mBase.onWidgetRemoved(product, list);
    }
}
