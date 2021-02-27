package com.ju.demo.business;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ju.widget.interfaces.connector.IRemoteBusinessConnector;
import com.ju.widget.interfaces.connector.IWidgetServiceConnector;

public class MainActivity extends Activity {

    private static final String TAG = "DemoBusinessActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.widget_test_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动
                Intent intent = new Intent(IWidgetServiceConnector.INTENT_ACTION);
                intent.setPackage(DemoBusiness.REMOTE_PACKAGE);
                startService(intent);

                intent = new Intent(IRemoteBusinessConnector.INTENT_ACTION_PREFIX + getPackageName());
                intent.setPackage(getPackageName());
                startService(intent);
            }
        });

    }
}