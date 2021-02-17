package com.ju.widget.api;

import android.os.HandlerThread;
import android.text.TextUtils;

import com.ju.widget.interfaces.IWidgetCallback;
import com.ju.widget.interfaces.IWidgetManager;
import com.ju.widget.util.Log;
import com.ju.widget.util.Tools;

import java.util.ArrayList;
import java.util.HashMap;

public class WidgetServer {

    private static final String TAG = "WidgetServer";

    private static final HandlerThread sWorker = new HandlerThread("WidgetServer");

    static {
        sWorker.start();
    }

    private static final WidgetCallback sWidgetInfoCallback = new WidgetCallback();
    private static final HashMap<Product, IWidgetManager> sProducts = new HashMap<>();
    private static final HashMap<Product, ArrayList<Widget>> sWidgets = new HashMap<>();

    /**
     * 注册产品分类信息
     *
     * @param product 产品分类信息
     * @return
     */
    public static final boolean registerProduct(final Product product, final IWidgetManager manager) {
        if (product == null || manager == null) {
            Log.e(TAG, "registerProduct with invalid args: ", product, manager);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.e(TAG, "registerProduct in invalid thread: ", product, manager);

            Tools.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    registerProduct(product, manager);
                }
            });

            return false;
        }

        if (sProducts.containsKey(product)) {
            Log.i(TAG, "registerProduct with duplicate args: ", product, manager);
            return true;
        }

        Log.i(TAG, "registerProduct : ", product, manager);

        sProducts.put(product, manager);

        // TODO: 何时setEnable(true)？
        manager.setEnable(false);
        manager.setCallback(sWidgetInfoCallback);
        return true;
    }

    /**
     * 注册单个Widget
     *
     * @param product 产品分类信息
     * @param widget Widget信息
     * @return
     */
    static final boolean registerWidget(final Product product, final Widget widget) {
        if (product == null || widget == null) {
            Log.e(TAG, "registerWidget invalid args: ", product, widget);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.e(TAG, "registerWidget in invalid thread: ", product, widget);

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
     * @param widget Widget信息
     * @return
     */
    static final boolean unregisterWidget(final Product product, final Widget widget) {
        if (product == null || widget == null) {
            Log.e(TAG, "unregisterWidget invalid args: ", product, widget);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.e(TAG, "unregisterWidget in invalid thread: ", product, widget);

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
            Log.i(TAG, "unregisterWidget with empty cache: ", product);
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
     * @param list Widget信息列表
     * @return
     */
    static final boolean registerWidget(final Product product, final ArrayList<Widget> list) {
        if (product == null || list == null || list.size() == 0) {
            Log.e(TAG, "registerWidget invalid args: ", product, list);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.e(TAG, "registerWidget in invalid thread: ", product, list);

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
                Log.i(TAG, "registerWidget with cache: ", product, list.get(i));
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
     * @param list Widget信息列表
     * @return
     */
    static final boolean unregisterWidget(final Product product, final ArrayList<Widget> list) {
        if (product == null || list == null || list.size() == 0) {
            Log.e(TAG, "unregisterWidget invalid args: ", product, list);
            return false;
        }

        if (!Tools.isMainThread()) {
            Log.e(TAG, "unregisterWidget in invalid thread: ", product, list);

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
     * 根据id查询Widget信息
     *
     * @param id
     * @return
     */
    public static final Widget findWidget(String id) {
        if (id == null || TextUtils.isEmpty(id)) {
            Log.e(TAG, "findWidget invalid args: ", id);
            return null;
        }

        if (!Tools.isMainThread()) {
            Log.e(TAG, "findWidget in invalid thread: ", id);
            return null;
        }

        String tempID = null;
        for (ArrayList<Widget> list : sWidgets.values()) {
            if (list != null) {
                for (Widget widget : list) {
                    tempID = widget.getID();
                    if (TextUtils.equals(tempID, id)) {
                        Log.d(TAG, "findWidget: ", id, widget);
                        return widget;
                    }
                }
            }
        }

        return null;
    }

    static final class WidgetCallback implements IWidgetCallback {

        @Override
        public void onWidgetAdded(Product product, Widget widget) {
            registerWidget(product, widget);
        }

        @Override
        public void onWidgetRemoved(Product product, Widget widget) {
            unregisterWidget(product, widget);
        }

        @Override
        public void onWidgetAdded(Product product, ArrayList<Widget> list) {
            registerWidget(product, new ArrayList<>(list));
        }

        @Override
        public void onWidgetRemoved(Product product, ArrayList<Widget> list) {
            unregisterWidget(product, new ArrayList<>(list));
        }
    }

}
