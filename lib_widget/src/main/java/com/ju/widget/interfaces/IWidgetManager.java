package com.ju.widget.interfaces;

import android.graphics.Point;

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
     * @param enable
     */
    void setEnable(boolean enable);

    /**
     * 设置Widget信息回调接口，只允许在WidgetServer中设置
     *
     * @param callback
     */
    void setCallback(IWidgetCallback callback);

    /**
     * 获取推荐的Widget信息
     *
     * @param maxSpan
     * @param bestSpan
     * @return
     */
    ArrayList<Widget> getRecommendWidget(Point maxSpan, Point bestSpan);

}
