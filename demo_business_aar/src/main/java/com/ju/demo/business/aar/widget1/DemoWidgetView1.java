package com.ju.demo.business.aar.widget1;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ju.widget.api.WidgetView;

public class DemoWidgetView1 extends WidgetView<DemoWidgetData1> {

    private TextView mTextView;

    public DemoWidgetView1(Context context) {
        this(context, null, 0, 0);
    }

    public DemoWidgetView1(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public DemoWidgetView1(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, null, defStyleAttr, 0);
    }

    public DemoWidgetView1(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setBackgroundColor(Color.LTGRAY);

        mTextView = new TextView(context);
        mTextView.setTextColor(Color.RED);
        addView(mTextView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onDataChanged(DemoWidgetData1 data) {
        // TODO: 更新界面
        if (data == null) {
            mTextView.setText("DemoWidgetView1 null data");
        } else {
            mTextView.setText(data.getTitle() + " " + data.randomString);
        }
    }

    @Override
    protected void onVisibleChanged(boolean showing) {

    }
}
