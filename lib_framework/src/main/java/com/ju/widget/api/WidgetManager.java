package com.ju.widget.api;

import android.content.Context;
import android.content.Intent;

import com.ju.widget.interfaces.IWidgetCallback;
import com.ju.widget.interfaces.IWidgetManager;
import com.ju.widget.interfaces.connector.Connector;
import com.ju.widget.interfaces.connector.IRemoteBusinessConnector;
import com.ju.widget.interfaces.connector.IWidgetServiceConnector;
import com.ju.widget.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IWidgetManager的实现类，各业务根据自身的业务需求，继承并重写该类
 */
public abstract class WidgetManager<C extends IRemoteBusinessConnector> implements IWidgetManager {

    private static final String TAG = "WidgetManager";

    public static final int MAX_WIDGET_COUNT = 24;

    protected final ConcurrentHashMap<String, Widget> mWidgetCache = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, WidgetData> mDataCache = new ConcurrentHashMap<>();

    protected final Context mContext;
    protected final Product mProduct;
    protected final C mConnector;

    protected IWidgetCallback mCallback;
    protected boolean mEnabled;

    protected WidgetManager(Context ctx, Product product) {
        mContext = ctx;
        mProduct = product;
        mConnector = createRemoteBusinessConnector();
    }

    @Override
    public void setEnable(boolean enable) {
        mEnabled = enable;
    }

    @Override
    public void setCallback(IWidgetCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        final int action = Connector.getAction(intent);
        final String pkg = Connector.getPackage(intent);
        final String pid = Connector.getProductId(intent);
        final String wid = Connector.getWidgetId(intent);
        final String payload = Connector.getPayload(intent);

        Log.i(TAG, "onHandleIntent: ", action, pkg, pid, wid, payload);

        final IWidgetCallback callback = mCallback;
        if (callback == null) {
            Log.e(TAG, "onHandleIntent and WidgetCallback is null，do nothing.");
            return;
        }

        switch (action) {
            case IWidgetServiceConnector.ACT_ADD_WIDGET:
                onAddWidget(callback, payload);
                return;
            case IWidgetServiceConnector.ACT_REMOVE_WIDGET:
                onRemoveWidget(callback, payload);
                return;
            case IWidgetServiceConnector.ACT_ADD_WIDGET_LIST:
                onAddWidgetList(callback, payload);
                return;
            case IWidgetServiceConnector.ACT_REMOVE_WIDGET_LIST:
                onRemoveWidgetList(callback, payload);
                return;
            case IWidgetServiceConnector.ACT_UPDATE_WIDGET_DATA:
                onUpdateWidgetData(callback, wid, payload);
                return;
            default:
                Log.e(TAG, "invalid action: ", action);
                return;
        }
    }

    @Override
    public ArrayList<Widget> queryWidget(Query query) {
        final ArrayList<Widget> list = new ArrayList<>(mWidgetCache.values());
        Collections.sort(list);

        if (query == null) {
            return list;
        }

        final int count = (query.mMaxCount > 0 ? query.mMaxCount : MAX_WIDGET_COUNT);
        final ArrayList<Widget> temp = new ArrayList<>();

        for (Widget widget : list) {
            if (widget.isMatch(query)) {
                temp.add(widget);

                if (temp.size() >= count) {
                    break;
                }
            }
        }

        return temp;
    }

    @Override
    public boolean updateWidgetData(Widget widget) {
        if (!mEnabled) {
            Log.e(TAG, "updateWidgetData when WidgetManager is not enabled: ", this, widget);
            return false;
        }

        if (!widget.update()) {
            Log.e(TAG, "Widget is updating: ", widget);
            return false;
        }

        return notifyUpdateWidgetData(widget);
    }

    /**
     * 创建和远端业务通信的接口对象
     *
     * @return
     */
    protected abstract C createRemoteBusinessConnector();

    /**
     * 通知远端更新Widget数据
     *
     * @param widget
     * @return
     */
    protected abstract boolean notifyUpdateWidgetData(Widget widget);

    /**
     * 解析Widget信息
     *
     * @param payload Widget信息字串
     * @return
     */
    protected abstract Widget parseWidget(String payload);

    /**
     * 解析Widget信息列表
     *
     * @param payload Widget信息列表字串
     * @return
     */
    protected abstract ArrayList<Widget> parseWidgetList(String payload);

    /**
     * 解析Widget数据
     *
     * @param payload Widget数据字串
     * @return
     */
    protected abstract WidgetData parseWidgetData(Widget widget, String payload);

    @Override
    public abstract WidgetView createWidgetView(Context context, Widget widget);

    protected final Widget findWidget(String wid) {
        return mWidgetCache.get(wid);
    }

    protected final void onAddWidget(IWidgetCallback callback, String payload) {
        final Widget widget = parseWidget(payload);
        if (widget != null) {
            mWidgetCache.put(widget.getID(), widget);
            callback.onAddWidget(mProduct, widget);
        }
    }

    protected final void onRemoveWidget(IWidgetCallback callback, String payload) {
        final Widget widget = parseWidget(payload);
        if (widget != null) {
            mWidgetCache.remove(widget.getID());
            callback.onAddWidget(mProduct, widget);
        }
    }

    protected final void onAddWidgetList(IWidgetCallback callback, String payload) {
        final ArrayList<Widget> list = parseWidgetList(payload);
        if (list != null) {
            final ConcurrentHashMap map = mWidgetCache;
            for (Widget widget : list) {
                map.put(widget.getID(), widget);
            }

            callback.onAddWidgetList(mProduct, list);
        }
    }

    protected final void onRemoveWidgetList(IWidgetCallback callback, String payload) {
        final ArrayList<Widget> list = parseWidgetList(payload);
        if (list != null) {
            final ConcurrentHashMap map = mWidgetCache;
            for (Widget widget : list) {
                map.remove(widget.getID());
            }

            callback.onRemoveWidgetList(mProduct, list);
        }
    }

    protected final void onUpdateWidgetData(IWidgetCallback callback, String wid, String payload) {
        final Widget widget = findWidget(wid);
        if (widget != null) {
            final WidgetData data = parseWidgetData(widget, payload);
            mDataCache.put(wid, data);

            widget.onUpdateFinished(data);
            callback.onUpdateWidgetData(widget, data);
        }
    }
}