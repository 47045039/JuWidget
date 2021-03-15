package com.ju.demo.business.aar.widget2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ju.widget.api.WidgetView;

public class DemoWidgetView2 extends WidgetView<DemoWidgetData2> {

    private TextView mTextView;

    public DemoWidgetView2(Context context) {
        this(context, null, 0, 0);
    }

    public DemoWidgetView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public DemoWidgetView2(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DemoWidgetView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setBackgroundColor(Color.LTGRAY);

        mTextView = new TextView(context);
        mTextView.setTextColor(Color.BLUE);
        addView(mTextView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onDataChanged(DemoWidgetData2 data) {
        // TODO: 更新界面
        if (data == null) {
            mTextView.setText("DemoWidgetView2 null data");
        } else {
            mTextView.setText(data.getTitle() + " " + data.mBackground);
        }
    }

    @Override
    protected void onVisibleChanged(boolean showing) {

    }
}
