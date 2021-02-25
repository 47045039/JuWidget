package com.ju.widget.demo;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewGroup;

import com.ju.widget.IWidgetServer;
import com.ju.widget.demo.main.SectionsPagerAdapter;
import com.ju.widget.impl.WidgetServer;
import com.ju.widget.plugin.InitHelper;

public class MainActivity extends AppCompatActivity {

    private IWidgetServer iWidgetServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        iWidgetServer = WidgetServer.getInstance(getApplicationContext());
        /*调用plugin的初始化接口*/
        InitHelper.init(getApplicationContext(), iWidgetServer);
        showWidgets(iWidgetServer);
    }

    private void showWidgets(IWidgetServer iWidgetServer) {
        iWidgetServer.show((ViewGroup) getWindow().getDecorView());
    }
}