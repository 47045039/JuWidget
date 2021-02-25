package com.ju.widget.time;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.ju.widget.IWidgetView;

@SuppressLint("AppCompatCustomView")
public class WidgetTimesView extends TextView implements IWidgetView {
    public WidgetTimesView(Context context) {
        super(context);
    }

    public WidgetTimesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetTimesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WidgetTimesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void resize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    @Override
    public String getPkgName() {
        return null;
    }

    @Override
    public String getWidgetId() {
        return null;
    }

    @Override
    public void update(String stringExtra) {
        ReqFromWidgetTimesApp req = new Gson().fromJson(stringExtra, ReqFromWidgetTimesApp.class);
        ActionFromTimesWidgetApp action = req.action;
        String currentTime = action.currentTime;
        setText(currentTime);
    }

    @Override
    public View createView() {
        return null;
    }
}
