package com.ju.demo.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ju.widget.api.WidgetContainer;
import com.ju.widget.api.WidgetEnv;
import com.ju.widget.interfaces.connector.IRemoteBusinessConnector;
import com.ju.widget.interfaces.connector.IWidgetServiceConnector;
import com.ju.widget.util.Log;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "DemoLauncherActivity";

    private WidgetContainer mContainer1;
    private WidgetContainer mContainer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WidgetEnv.init(this);

        setContentView(R.layout.activity_main);
        mContainer1 = findViewById(R.id.widget_container_1);
        mContainer2 = findViewById(R.id.widget_container_2);

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

        Intent intent = new Intent(IWidgetServiceConnector.INTENT_ACTION);
        intent.setPackage(getPackageName());
        startService(intent);
        Log.e(TAG, "start service: ", intent);

        intent = new Intent(IRemoteBusinessConnector.INTENT_ACTION_PREFIX + "com.ju.demo.business");
        intent.setClassName("com.ju.demo.business", "com.ju.demo.business.DemoBusiness");
        startService(intent);
        Log.e(TAG, "start service: ", intent);
    }

    private void addWidget(WidgetContainer container) {

    }

    private void removeWidget(WidgetContainer container) {

    }

    private void updateWidget(WidgetContainer container) {

    }
}