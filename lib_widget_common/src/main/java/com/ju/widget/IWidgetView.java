package com.ju.widget;

import android.view.View;

public interface IWidgetView {
    void resize(int width, int height);
    String getPkgName();
    String getWidgetId();

    void update(String stringExtra);

    View createView();
}
