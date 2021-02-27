package com.ju.widget.impl;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.ju.widget.api.Product;
import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetData;
import com.ju.widget.impl.cache.CachedWidget;
import com.ju.widget.interfaces.IWidgetManager;
import com.ju.widget.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: Widget的数据工具
 * <p>
 * 从assert里读取各个模块的WidgetManager；
 * 从数据库中读取缓存的widget信息；
 * 从数据库中读取缓存的widget数据；
 * 保存widget信息；
 * 保存widget数据；
 */
public class WidgetLoader {

    private static final String TAG = "WidgetLoader";

    private static Context sContext;

    /**
     * 初始化
     *
     * @param context
     */
    public static final void doInit(Context context) {
        if (sContext == null) {
            sContext = context.getApplicationContext();
            loadWidgetManager(sContext);
            readAllWidget(sContext);
            readAllWidgetData(sContext);
        }
    }

    private static final void loadWidgetManager(Context context) {
        final AssetManager assets = context.getAssets();
        try {
            JSONObject object = null;
            Product product = null;
            IWidgetManager manager = null;

            for (String file : assets.list("widget")) {
                object = readConfig(assets, "widget" + File.separator + file);

                Log.i(TAG, "read config: ", file, object);

                if (object == null) {
                    continue;
                }

                product = readProduct(object);

                if (product == null) {
                    continue;
                }

                manager = readManager(context, product, object);
                if (manager == null) {
                    continue;
                }

                WidgetServer.registerProduct(product, manager);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final void readAllWidget(Context context) {
        // TODO：从数据库中读取所有缓存的widget信息
        Product edu = new Product("1", "教育", "教育Description");
        ArrayList<Widget> eduList = new ArrayList<>(2);
        eduList.add(new CachedWidget("CachedWidget_1", "1", 1, 1));
        eduList.add(new CachedWidget("CachedWidget_2", "1", 1, 2));

        Product vod = new Product("2", "视频", "视频Description");
        ArrayList<Widget> vodList = new ArrayList<>(2);
        vodList.add(new CachedWidget("CachedWidget_VOD_1", "2", 1, 1));
        vodList.add(new CachedWidget("CachedWidget_VOD_2", "2", 1, 2));

        WidgetServer.registerWidget(edu, eduList);
        WidgetServer.registerWidget(vod, vodList);
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

    private static final JSONObject readConfig(AssetManager assets, String file) {
        InputStream is = null;
        byte[] buffer = null;
        try {
            is = assets.open(file);
            buffer = new byte[is.available()];
            is.read(buffer);

            return new JSONObject(new String(buffer));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final Product readProduct(JSONObject obj) {
        if (obj == null) {
            return null;
        }

        try {
            obj = obj.getJSONObject("product");
            if (obj == null) {
                return null;
            }

            return new Product(obj.getString("id"),
                    obj.getString("title"),
                    obj.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static final IWidgetManager readManager(Context ctx, Product product, JSONObject obj) {
        if (obj == null) {
            return null;
        }

        try {
            obj = obj.getJSONObject("manager");
            if (obj == null) {
                return null;
            }

            String clazz = obj.getString("class");
            if (TextUtils.isEmpty(clazz)) {
                return null;
            }

            return new WidgetManagerProxy(ctx, product, clazz);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
