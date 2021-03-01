package com.ju.widget.api;

import android.content.Context;
import android.view.View;

import com.ju.widget.impl.PopupWindow;

public class WidgetMenuContainer extends PopupWindow {

    public WidgetMenuContainer(Context context, OnDismissListener listener) {
        super(context);
        super.setOnDismissListener(listener);

        setTouchable(true);
        setFocusable(true);
    }

    @Override
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        // 禁止外部设置onDismissListener
        // super.setOnDismissListener(onDismissListener);
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        try {
            super.showAtLocation(parent, gravity, x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        try {
            super.showAsDropDown(anchor, xoff, yoff);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        try {
            super.showAsDropDown(anchor, xoff, yoff, gravity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
