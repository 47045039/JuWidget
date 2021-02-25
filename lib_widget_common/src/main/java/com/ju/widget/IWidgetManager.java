package com.ju.widget;

import java.util.List;

/*各个app的aar的自身widget管理类*/
public interface IWidgetManager {
    /**
     * @return aar中包含的widget列表
     */
    List<IWidgetView> getWidgetList();
}
