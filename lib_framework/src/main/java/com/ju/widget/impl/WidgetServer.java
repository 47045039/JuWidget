package com.ju.widget.impl;

import android.text.TextUtils;

import com.ju.widget.api.Product;
import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetData;
import com.ju.widget.api.WidgetView;
import com.ju.widget.interfaces.IWidgetCallback;
import com.ju.widget.interfaces.IWidgetManager;
import com.ju.widget.util.Log;
import com.ju.widget.util.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WidgetServer {

    private static final String TAG = "WidgetServer";

    private static final WidgetCallback sWidgetCallback = new WidgetCallback();
    private static final HashMap<Product, IWidgetManager> sProducts = new HashMap<>();
    private static final HashMap<Product, ArrayList<Widget>> sWidgets = new HashMap<>();
    private static final HashMap<Widget, ArrayList<WidgetView>> sViews = new HashMap<>();

    /**
     * 注册产品分类信息
     *
     * @param product 产品分类信息
     * @return
     */
    static final boolean registerProduct(final Product product, final IWidgetManager manager) {
        if (product == null || manager == null) {
            Log.e(TAG, "registerProduct with invalid args: ", product, manager);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.w(TAG, "registerProduct in invalid thread: ", product, manager);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    registerProduct(product, manager);
                }
            });

            return false;
        }

        if (sProducts.containsKey(product)) {
            Log.w(TAG, "registerProduct with duplicate args: ", product, manager);
            return true;
        }

        Log.i(TAG, "registerProduct : ", product, manager);

        sProducts.put(product, manager);

        // TODO: 何时setEnable(true)？
        manager.setEnable(true);
        manager.setCallback(sWidgetCallback);
        return true;
    }

    /**
     * 注册单个Widget
     *
     * @param product 产品分类信息
     * @param widget  Widget信息
     * @return
     */
    static final boolean registerWidget(final Product product, final Widget widget) {
        if (product == null || widget == null) {
            Log.e(TAG, "registerWidget invalid args: ", product, widget);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.w(TAG, "registerWidget in invalid thread: ", product, widget);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    registerWidget(product, widget);
                }
            });

            return false;
        }

        Log.i(TAG, "registerWidget: ", product, widget);

        ArrayList<Widget> widgets = sWidgets.get(product);
        if (widgets == null) {
            widgets = new ArrayList<>();
            sWidgets.put(product, widgets);
        }

        if (widgets.contains(widget)) {
            Log.w(TAG, "registerWidget with cache: ", product, widget);
            return true;
        }

        widgets.add(widget);

        // TODO: 通知到WidgetContainer，刷新Widget无效状态
        return true;
    }

    /**
     * 反注册单个Widget
     *
     * @param product 产品分类信息
     * @param widget  Widget信息
     * @return
     */
    static final boolean unregisterWidget(final Product product, final Widget widget) {
        if (product == null || widget == null) {
            Log.e(TAG, "unregisterWidget invalid args: ", product, widget);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.w(TAG, "unregisterWidget in invalid thread: ", product, widget);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    unregisterWidget(product, widget);
                }
            });

            return false;
        }

        Log.i(TAG, "unregisterWidget: ", product, widget);

        final ArrayList<Widget> widgets = sWidgets.get(product);
        if (widgets == null || !widgets.contains(widget)) {
            Log.w(TAG, "unregisterWidget with empty cache: ", product);
            return false;
        }

        widgets.remove(widget);

        // TODO: 通知到WidgetContainer，刷新Widget无效状态
        return true;
    }

    /**
     * 注册多个Widget信息
     *
     * @param product 产品分类信息
     * @param list    Widget信息列表
     * @return
     */
    static final boolean registerWidget(final Product product, final ArrayList<Widget> list) {
        if (product == null || list == null || list.size() == 0) {
            Log.e(TAG, "registerWidget invalid args: ", product, list);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.w(TAG, "registerWidget in invalid thread: ", product, list);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    registerWidget(product, list);
                }
            });

            return false;
        }

        Log.i(TAG, "registerWidget: ", product, list);

        ArrayList<Widget> widgets = sWidgets.get(product);
        if (widgets == null) {
            widgets = new ArrayList<>();
            sWidgets.put(product, widgets);
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            if (widgets.contains(list.get(i))) {
                Log.w(TAG, "registerWidget with cache: ", product, list.get(i));
                list.remove(i);
            }
        }

        widgets.addAll(list);

        // TODO: 通知到WidgetContainer，刷新Widget无效状态
        return true;
    }

    /**
     * 反注册多个Widget信息
     *
     * @param product 产品分类信息
     * @param list    Widget信息列表
     * @return
     */
    static final boolean unregisterWidget(final Product product, final ArrayList<Widget> list) {
        if (product == null || list == null || list.size() == 0) {
            Log.e(TAG, "unregisterWidget invalid args: ", product, list);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.w(TAG, "unregisterWidget in invalid thread: ", product, list);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    unregisterWidget(product, list);
                }
            });

            return false;
        }

        Log.i(TAG, "unregisterWidget: ", product, list);

        final ArrayList<Widget> widgets = sWidgets.get(product);
        if (widgets == null) {
            Log.i(TAG, "unregisterWidget with empty cache: ", product);
            return false;
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            if (!widgets.contains(list.get(i))) {
                Log.i(TAG, "unregisterWidget with empty cache: ", product, list.get(i));
                list.remove(i);
            }
        }

        widgets.removeAll(list);

        // TODO: 通知到WidgetContainer，刷新Widget无效状态
        return true;
    }

    /**
     * 通知WidgetView刷新界面显示
     *
     * @param widget Widget信息
     * @param data   Widget数据
     * @return
     */
    static final boolean notifyWidgetDataUpdate(Widget widget, WidgetData data) {
        if (widget == null) {
            Log.e(TAG, "notifyWidgetDataUpdate invalid args: ", widget, data);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.w(TAG, "notifyWidgetDataUpdate in invalid thread: ", widget, data);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    notifyWidgetDataUpdate(widget, data);
                }
            });

            return false;
        }


        final ArrayList<WidgetView> views = findWidgetView(widget.getID());
        if (views != null) {
            for (WidgetView view : views) {
                view.setWidgetData(data);
            }

            return true;
        }

        return false;
    }

    /**
     * 记录Widget和WidgetView的绑定关系
     *
     * @param widget Widget信息
     * @param view   WidgetView
     * @return
     */
    public static final boolean attachWidgetView(Widget widget, WidgetView view) {
        Log.i(TAG, "attachWidgetView: ", widget, view);
        if (widget == null || view == null) {
            return false;
        }

        ArrayList<WidgetView> views = sViews.get(widget);
        if (views == null) {
            views = new ArrayList<>(1);
            views.add(view);
            sViews.put(widget, views);
            return true;
        }

        if (!views.contains(view)) {
            views.add(view);
        }

        return true;
    }

    /**
     * 记录Widget和WidgetView的绑定关系
     *
     * @param widget Widget信息
     * @param view   WidgetView
     * @return
     */
    public static final boolean detachWidgetView(Widget widget, WidgetView view) {
        Log.i(TAG, "detachWidgetView: ", widget, view);
        if (widget == null || view == null) {
            return false;
        }

        final ArrayList<WidgetView> views = sViews.get(widget);
        if (views == null || !views.contains(view)) {
            Log.w(TAG, "detachWidgetView with empty cache: ", widget, view);
            return true;
        }

        views.remove(view);

        if (views.isEmpty()) {
            sViews.remove(widget);
        }

        return true;
    }

    /**
     * 根据id查询IWidgetManager
     *
     * @param pid 产品ID
     * @return
     */
    public static final IWidgetManager findWidgetManager(String pid) {
        if (pid == null || TextUtils.isEmpty(pid)) {
            Log.e(TAG, "findWidgetManager invalid args: ", pid);
            return null;
        }

//        if (!Tools.isMainThread()) {
//            Log.e(TAG, "findWidgetManager in invalid thread: ", id);
//            return null;
//        }

        String tempID = null;
        for (Map.Entry<Product, IWidgetManager> entry : sProducts.entrySet()) {
            tempID = entry.getKey().mID;
            if (TextUtils.equals(tempID, pid)) {
                Log.d(TAG, "findWidgetManager: ", pid, entry.getValue());
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 根据ID查询Product信息
     *
     * @param pid 产品ID
     * @return
     */
    static final Product findProduct(String pid) {
        if (pid == null || TextUtils.isEmpty(pid)) {
            Log.e(TAG, "findProduct invalid args: ", pid);
            return null;
        }

//        if (!Tools.isMainThread()) {
//            Log.e(TAG, "findProduct in invalid thread: ", id);
//            return null;
//        }

        String tempID = null;
        for (Product product : sProducts.keySet()) {
            tempID = product.mID;
            if (TextUtils.equals(tempID, pid)) {
                Log.d(TAG, "findProduct: ", pid, product);
                return product;
            }
        }

        return null;
    }

    /**
     * 根据id查询Widget信息
     *
     * @param wid Widget ID
     * @return
     */
    public static final Widget findWidget(String wid) {
        if (wid == null || TextUtils.isEmpty(wid)) {
            Log.e(TAG, "findWidget invalid args: ", wid);
            return null;
        }

//        if (!Tools.isMainThread()) {
//            Log.e(TAG, "findWidget in invalid thread: ", id);
//            return null;
//        }

        String tempID = null;
        for (ArrayList<Widget> list : sWidgets.values()) {
            if (list != null) {
                for (Widget widget : list) {
                    tempID = widget.getID();
                    if (TextUtils.equals(tempID, wid)) {
                        Log.d(TAG, "findWidget: ", wid, widget);
                        return widget;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 根据id查询Widget View
     *
     * @param wid Widget ID
     * @return
     */
    public static final ArrayList<WidgetView> findWidgetView(String wid) {
        if (wid == null || TextUtils.isEmpty(wid)) {
            Log.e(TAG, "findWidgetView invalid args: ", wid);
            return null;
        }

//        if (!Tools.isMainThread()) {
//            Log.e(TAG, "findWidgetView in invalid thread: ", wid);
//            return null;
//        }

        String tempID = null;
        ArrayList<WidgetView> views = null;
        for (Map.Entry<Widget, ArrayList<WidgetView>> entry : sViews.entrySet()) {
            tempID = entry.getKey().getID();
            if (TextUtils.equals(tempID, wid)) {
                views = entry.getValue();
                Log.d(TAG, "findWidgetView: ", wid, views);
                return views;
            }
        }

        return null;
    }

    static final class WidgetCallback implements IWidgetCallback {

        @Override
        public boolean onAddWidget(Product product, Widget widget) {
            WidgetLoader.saveWidget(product, widget);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    registerWidget(product, widget);
                }
            });
            return true;
        }

        @Override
        public boolean onRemoveWidget(Product product, Widget widget) {
            WidgetLoader.deleteWidget(product, widget);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    unregisterWidget(product, widget);
                }
            });
            return true;
        }

        @Override
        public boolean onAddWidgetList(Product product, ArrayList<Widget> list) {
            WidgetLoader.saveWidget(product, list);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    registerWidget(product, list);
                }
            });
            return true;
        }

        @Override
        public boolean onRemoveWidgetList(Product product, ArrayList<Widget> list) {
            WidgetLoader.deleteWidget(product, list);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    unregisterWidget(product, list);
                }
            });
            return true;
        }

        @Override
        public boolean onUpdateWidgetData(Widget widget, WidgetData data) {
            WidgetLoader.saveWidgetData(widget, data);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    notifyWidgetDataUpdate(widget, data);
                }
            });
            return true;
        }
    }

}
