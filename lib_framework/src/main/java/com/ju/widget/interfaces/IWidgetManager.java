package com.ju.widget.interfaces;

import android.content.Intent;

import com.ju.widget.api.Query;
import com.ju.widget.api.Widget;

import java.util.ArrayList;

/**
 * 各产品业务内部的Widget管理
 */
public interface IWidgetManager {

    /**
     * 控制内部加载、刷新等耗时动作的执行；
     * 进程刚启动时，资源消耗很大，避免资源竞争导致系统应用卡顿；
     *
     * @param enable 是否开始运行
     */
    void setEnable(boolean enable);

    /**
     * 设置回调
     *
     * @param callback
     */
    void setCallback(IWidgetCallback callback);

    /**
     * 收到远端业务模块传递的数据
     *
     * @param intent
     */
    void onHandleIntent(Intent intent);

    /**
     * 获取Widget信息
     *
     * @param query 查询条件
     * @return
     */
    ArrayList<Widget> queryWidget(Query query);

    /**
     * 通知更新Widget数据
     *
     * @param widget widget信息
     * @return
     */
    boolean updateWidgetData(Widget widget);

}
