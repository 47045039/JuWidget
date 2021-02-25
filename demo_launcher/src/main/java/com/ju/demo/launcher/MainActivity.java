package com.ju.demo.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.ju.widget.api.WidgetContainer;

public class MainActivity extends Activity implements View.OnClickListener {


    private WidgetContainer mContainer1;
    private WidgetContainer mContainer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer1 = findViewById(R.id.widget_container_1);
        mContainer2 = findViewById(R.id.widget_container_2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.widget_edit_1:
            case R.id.widget_add_1:
            case R.id.widget_remove_1:
            case R.id.widget_update_1:

            case R.id.widget_edit_2:
            case R.id.widget_add_2:
            case R.id.widget_remove_2:
            case R.id.widget_update_2:

        }
    }
}