package com.ju.widget.interfaces;

import com.ju.widget.api.Product;
import com.ju.widget.api.Widget;

import java.util.ArrayList;

/**
 * Widget信息发生变化的回调接口
 */
public interface IWidgetCallback {

    /**
     * 新增单个Widget
     *
     * @param product
     * @param widget
     */
    void onWidgetAdded(Product product, Widget widget);

    /**
     * 删除单个Widget
     *
     * @param product
     * @param widget
     */
    void onWidgetRemoved(Product product, Widget widget);

    /**
     * 新增一些Widget
     *
     * @param product
     * @param list
     */
    void onWidgetAdded(Product product, ArrayList<Widget> list);

    /**
     * 删除一些Widget
     *
     * @param product
     * @param list
     */
    void onWidgetRemoved(Product product, ArrayList<Widget> list);

}
