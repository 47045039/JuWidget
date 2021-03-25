package com.ju.demo.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.ju.widget.api.Config;
import com.ju.widget.api.Widget;
import com.ju.widget.api.WidgetContainer;
import com.ju.widget.api.WidgetEnv;
import com.ju.widget.impl.WidgetServer;
import com.ju.widget.interfaces.IWidgetManager;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "DemoLauncherActivity";

    // TODO：业务调试时，可改为实际的产品ID和Widget ID
    private static final String PRODUCT_ID = "DemoBusinessProductID";
    private static final String WIDGET_ID_1 = "DemoBusiness_Widget_1";
    private static final String WIDGET_ID_2 = "DemoBusiness_Widget_2";

    private WidgetContainer mContainer1;
    private WidgetContainer mContainer2;

    private Random mTestRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WidgetEnv.init(getApplicationContext(), getApplicationContext());

        setContentView(R.layout.activity_main);
        mContainer1 = findViewById(R.id.widget_container_1);
        mContainer2 = findViewById(R.id.widget_container_2);

        mContainer1.setLayoutConfig(new Config());

        findViewById(R.id.widget_edit_1).setOnClickListener(this);
        findViewById(R.id.widget_add_1).setOnClickListener(this);
        findViewById(R.id.widget_remove_1).setOnClickListener(this);
        findViewById(R.id.widget_update_1).setOnClickListener(this);

        findViewById(R.id.widget_edit_2).setOnClickListener(this);
        findViewById(R.id.widget_add_2).setOnClickListener(this);
        findViewById(R.id.widget_remove_2).setOnClickListener(this);
        findViewById(R.id.widget_update_2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.widget_edit_1:
                editMode(mContainer1);
                break;
            case R.id.widget_add_1:
                addWidget(mContainer1);
                break;
            case R.id.widget_remove_1:
                removeWidget(mContainer1);
                break;
            case R.id.widget_update_1:
                updateWidget(mContainer1);
                break;
//            case R.id.widget_edit_2:
//            case R.id.widget_add_2:
//            case R.id.widget_remove_2:
//            case R.id.widget_update_2:
        }
    }

    private void editMode(WidgetContainer container) {
        container.setEditMode(!container.isEditMode());
    }

    private void addWidget(WidgetContainer container) {
        final String wid = (mTestRandom.nextInt() % 2 == 0)  ? WIDGET_ID_1 : WIDGET_ID_2;
        final Widget widget = WidgetServer.findWidget(wid);
        if (widget != null) {
            container.addWidget(widget);
        }
    }

    private void removeWidget(WidgetContainer container) {
        container.removeAllViews();
    }

    private void updateWidget(WidgetContainer container) {
        final IWidgetManager manager = WidgetServer.findWidgetManager(PRODUCT_ID);
        if (manager == null) {
            return;
        }

        Widget widget = WidgetServer.findWidget(WIDGET_ID_1);
        if (widget != null) {
            manager.updateWidgetData(widget);
        }
        widget = WidgetServer.findWidget(WIDGET_ID_2);
        if (widget != null) {
            manager.updateWidgetData(widget);
        }
    }
}