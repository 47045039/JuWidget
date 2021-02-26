package com.ju.widget.interfaces;

import com.ju.widget.api.Product;
import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetData;

import java.util.ArrayList;

/**
 * Widget信息、数据的回调
 */
public interface IWidgetCallback {

    boolean onAddWidget(Product product, Widget widget);

    boolean onRemoveWidget(Product product, Widget widget);

    boolean onAddWidgetList(Product product, ArrayList<Widget> list);

    boolean onRemoveWidgetList(Product product, ArrayList<Widget> list);

    boolean onUpdateWidgetData(Widget widget, WidgetData data);

}
