package com.ju.widget.impl;

import com.ju.widget.api.Query;
import com.ju.widget.api.Widget;
import com.ju.widget.interfaces.IWidgetCallback;
import com.ju.widget.interfaces.IWidgetManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: WidgetManager代理
 *
 * 刚启动时同时加载太多的WidgetManager impl，对应用性能有较大影响；
 * 使用此类做为代理，在setEnable(true)后再加载impl；
 */
public class WidgetManagerProxy implements IWidgetManager {

    private final ClassLoader mClassLoader;         // 插件class loader
    private final String mImplClass;                // 实际类名

    private IWidgetManager mImpl;                   // 实际实现类
    private IWidgetCallback mCallback;              // 回调

    private boolean mEnable;

    WidgetManagerProxy(ClassLoader loader, String implClass) {
        mClassLoader = loader;
        mImplClass = implClass;
    }

    @Override
    public void setEnable(boolean enable) {
        mEnable = enable;

        if (enable) {
            loadImpl();
        }
    }

    @Override
    public void setCallback(IWidgetCallback callback) {
        mCallback = new WidgetCallbackProxy(callback);

        if (mImpl != null) {
            mImpl.setCallback(mCallback);
        }
    }

    @Override
    public ArrayList<Widget> queryWidget(Query query) {
        if (mImpl != null) {
            return mImpl.queryWidget(query);
        } else {
            // TODO: enable前先使用缓存数据
            return null;
        }
    }

    private IWidgetManager loadImpl() {
        try {
            // TODO: 两端IWidgetManager无法对齐如何处理？
            Class<IWidgetManager> clazz = (Class<IWidgetManager>) mClassLoader.loadClass(mImplClass);
            Constructor<IWidgetManager> constructor = clazz.getConstructor();
            constructor.setAccessible(true);

            mImpl = constructor.newInstance();
            mImpl.setEnable(mEnable);
            mImpl.setCallback(mCallback);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
