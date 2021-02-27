package com.ju.widget.connector;

import android.app.IntentService;
import android.content.Intent;

import com.ju.widget.interfaces.connector.Connector;
import com.ju.widget.interfaces.connector.IRemoteBusinessConnector;
import com.ju.widget.util.Log;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/25
 * @Description: 业务通信实现的基类，在业务模块中实现
 */
public abstract class AbsBusiness extends IntentService {

    private static final String TAG = "AbsBusiness";

    protected final String mProductID;                      // 产品类型
    protected final int mVersion;                           // 本地业务模块版本信息

    protected final WidgetServiceConnector mWidgetService;  // 远端Widget服务通信接口

    public AbsBusiness(String name, String remotePackage, String productID, int version) {
        super(name);
        mProductID = productID;
        mVersion = version;
        mWidgetService = new WidgetServiceConnector(this, remotePackage);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        final String pkg = Connector.getPackage(intent);
        final int remoteVersion = Connector.getVersion(intent);
        final int command = Connector.getCommand(intent);
        final String pid = Connector.getProductId(intent);
        final String wid = Connector.getWidgetId(intent);
        final String params = Connector.getParams(intent);

        Log.i(TAG, "onHandleIntent: ", pkg, remoteVersion, command, pid, wid, params);

        switch (command) {
            case IRemoteBusinessConnector.CMD_UPDATE_WIDGET_DATA:
                updateWidgetData(remoteVersion, pid, wid, params);
                return;
            default:
                Log.e(TAG, "invalid command: ", command);
                return;
        }
    }

    /**
     * 业务分成了两块：aar（随launcher打包和升级）、apk/component（独立应用/组件）：
     * <p>
     * 1、remoteVersion = localVersion，aar和apk逻辑对应，不存在兼容问题；
     * 1、如果aar随launcher升级（remoteVersion变大），而apk未升级，remoteVersion > localVersion，
     * 则aar需要兼容低版本（localVersion），
     * 2、如果aar未随launcher升级，而apk升级（localVersion变大），remoteVersion > localVersion，
     * 则apk需要兼容低版本（remoteVersion）
     *
     * @param remoteVersion 远端业务aar的版本
     * @param localVersion  本地业务的版本
     * @return 兼容版本号
     */
    protected int compatBusinessVersion(int remoteVersion, int localVersion) {
        return Math.min(remoteVersion, localVersion);
    }

    /**
     * 根据业务需求，实现异步更新Widget数据的逻辑
     *
     * @param remoteVersion 对端业务模块的版本号
     * @param pid           Product ID
     * @param wid           Widget ID
     * @param params        其它参数
     */
    protected abstract void updateWidgetData(int remoteVersion, String pid, String wid, String params);

    /**
     * Widget数据更新完成后，回传数据给WidgetService
     *
     * @param compatVersion 兼容版本号
     * @param pid           Product ID
     * @param wid           Widget ID
     * @param data          Widget数据
     */
    protected void notifyWidgetDataUpdated(int compatVersion, String pid, String wid, String data) {
        mWidgetService.notifyWidgetDataUpdated(compatVersion, pid, wid, data);
    }

    /**
     * 主动通知WidgetService，Widget信息有新增
     *
     * @param widget Widget信息
     */
    protected void notifyWidgetAdded(String widget) {
        mWidgetService.notifyWidgetAdded(mVersion, mProductID, widget);
    }

    /**
     * 主动通知WidgetService，Widget信息有移除
     *
     * @param widget Widget信息
     */
    protected void notifyWidgetRemoved(String widget) {
        mWidgetService.notifyWidgetRemoved(mVersion, mProductID, widget);
    }

    /**
     * 主动通知WidgetService，Widget信息有新增
     *
     * @param list Widget信息列表
     */
    protected void notifyWidgetListAdded(String list) {
        mWidgetService.notifyWidgetAdded(mVersion, mProductID, list);
    }

    /**
     * 主动通知WidgetService，Widget信息有新增
     *
     * @param list Widget信息列表
     */
    protected void notifyWidgetListRemoved(String list) {
        mWidgetService.notifyWidgetListRemoved(mVersion, mProductID, list);
    }
}
