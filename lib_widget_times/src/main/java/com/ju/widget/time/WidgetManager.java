package com.ju.widget.time;

import com.ju.widget.IWidgetManager;
import com.ju.widget.IWidgetView;

import java.util.ArrayList;
import java.util.List;

public class WidgetManager implements IWidgetManager {
    private List<IWidgetView> iWidgetViews = new ArrayList<>();
    @Override
    public List<IWidgetView> getWidgetList() {
        return iWidgetViews;
    }
}
