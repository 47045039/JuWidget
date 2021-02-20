package com.ju.widget.impl;

import android.content.Context;
import android.graphics.Bitmap;

import com.ju.widget.api.Product;
import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetData;
import com.ju.widget.impl.cache.CachedWidget;

import java.util.ArrayList;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description:
 * 从assert里读取各个模块的WidgetManager；
 * 从数据库中读取缓存的widget信息；
 * 从数据库中读取缓存的widget数据；
 * 保存widget信息；
 * 保存widget数据；
 */
public class WidgetLoader {

    private static Context sContext;

    /**
     * 初始化
     * @param context
     */
    public static final void doInit(Context context) {
        if (sContext == null) {
            sContext = context;
            loadWidgetManager(context);
            readAllWidget(context);
            readAllWidgetData(context);
        }
    }

    private static final void loadWidgetManager(Context context) {
        Product product = new Product(0, "教育", "教育Description");
        String manager = "com.ju.widget.edu.EduWidgetManager";
        WidgetServer.registerProduct(product, new WidgetManagerProxy(context.getClassLoader(), manager));

        product = new Product(0, "视频", "视频Description");
        manager = "com.ju.widget.vod.VodWidgetManager";
        WidgetServer.registerProduct(product, new WidgetManagerProxy(context.getClassLoader(), manager));
    }

    private static final void readAllWidget(Context context) {
        // TODO：从数据库中读取所有缓存的widget信息
        Product product = new Product(0, "教育", "教育Description");
        ArrayList<Widget> list = new ArrayList<>(2);
        list.add(new CachedWidget("CachedWidget_1", 1, 1));
        list.add(new CachedWidget("CachedWidget_2", 1, 2));

        WidgetServer.registerWidget(product, list);
    }

    private static final void readAllWidgetData(Context context) {
        // TODO：从数据库中读取所有缓存的widget数据，注意不要读取缩略图，避免内存消耗
    }

    static final void saveWidget(Product product, Widget widget) {
        // TODO：存入widget信息
    }

    static final void saveWidget(Product product, ArrayList<Widget> widgets) {
        // TODO：存入widget信息
    }

    static final void deleteWidget(Product product, Widget widget) {
        // TODO：删除widget信息
    }

    static final void deleteWidget(Product product, ArrayList<Widget> widgets) {
        // TODO：删除widget信息
    }

    static final void saveWidgetData(Widget widget, WidgetData data) {
        // TODO：存入widget数据
    }

    static final void deleteWidgetData(Widget widget, WidgetData data) {
        // TODO：删除widget数据
    }

    public static final void saveWidgetBackground(WidgetData data, Bitmap bitmap) {
        // TODO：存储widget view的缩略图，用于应用启动时的快速显示，减少内存消耗
    }

    public static final Bitmap readWidgetBackground(WidgetData data) {
        // TODO：读取widget view的缩略图
        return null;
    }

}
