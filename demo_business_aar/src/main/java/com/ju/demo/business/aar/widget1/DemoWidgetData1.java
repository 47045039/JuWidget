package com.ju.demo.business.aar.widget1;

import com.ju.widget.api.WidgetData;

public class DemoWidgetData1 extends WidgetData {

    public final String randomString;

    public DemoWidgetData1(String title, String str) {
        super(title);
        randomString = str;
    }

}
