package com.ju.demo.business.aar.widget1;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ju.widget.api.WidgetMenuView;
import com.ju.widget.util.Log;

public class DemoWidgetMenuView1 extends WidgetMenuView {

    private static final String TAG = "DemoWidgetMenuView1";

    private TextView mTextView;

    public DemoWidgetMenuView1(Context context) {
        this(context, null, 0, 0);
    }

    public DemoWidgetMenuView1(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public DemoWidgetMenuView1(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, null, defStyleAttr, 0);
    }

    public DemoWidgetMenuView1(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setBackgroundColor(Color.RED);

        mTextView = new TextView(context);
        mTextView.setTextColor(Color.BLUE);
        mTextView.setText("DemoWidgetMenuView1");
        addView(mTextView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mTextView.setFocusableInTouchMode(true);  // TOUCH模式也能拿焦点？
        mTextView.setFocusable(true);
        mTextView.setClickable(true);

        // TODO：业务的菜单界面需要自己处理BACK按键：
        // 1、收到BACK_UP时，移除一级菜单，返回true；
        // 2、当菜单只剩余一级时，返回false，框架层将会把外层菜单弹出框整体隐藏；
        mTextView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.e(TAG, "onKey: ", event);
                return true;
            }
        });
    }

}
