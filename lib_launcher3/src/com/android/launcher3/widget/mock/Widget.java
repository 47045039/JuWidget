package com.android.launcher3.widget.mock;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.R;

import java.util.Random;

public class Widget {
    private AppWidgetProviderInfo appWidgetProviderInfo;

    public View getView(Context context) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.color.notification_icon_default_color);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(5, 5, 5, 5);
        return imageView;
    }

    public void set(AppWidgetProviderInfo appWidgetProviderInfo) {
        this.appWidgetProviderInfo = appWidgetProviderInfo;
    }

    public AppWidgetProviderInfo getAppWidgetProviderInfo() {
        return appWidgetProviderInfo;
    }
}
