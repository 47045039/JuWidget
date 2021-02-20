package com.ju.widget.impl.cache;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ju.widget.api.WidgetView;
import com.ju.widget.util.Log;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/2/20
 * @Description: TODO
 */
public class CachedWidgetView extends WidgetView<CachedWidgetData> {

    private static final String TAG = "CachedWidgetView";

    private TextView mTitle;
    private ImageView mBackground;

    public CachedWidgetView(Context context) {
        super(context);
    }

    public CachedWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CachedWidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CachedWidgetView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        mTitle = ...;
//        mBackground = ...;
    }

    @Override
    protected void onDataChanged(CachedWidgetData data) {
        Log.d(TAG, "onDataChanged: ", data);
        if (data != null) {
//            mTitle.setText(data.getTitle());
//            mBackground.setImageBitmap(data.getBackground());
//        } else {
//            mTitle.setText("null");
//            mBackground.setImageBitmap(null);
        }
    }
}
