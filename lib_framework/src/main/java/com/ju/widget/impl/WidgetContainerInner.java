package com.ju.widget.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

public class WidgetContainerInner extends ViewGroup {

    private static final String TAG = "CellLayoutChildren";

    private static final Paint PAINT = new Paint();
    static {
        PAINT.setColor(Color.BLACK);
    }

    private int mCellWidth;
    private int mCellHeight;

    private int mWidthGap;
    private int mHeightGap;

    private int mCountX;
    private int mCountY;

    public WidgetContainerInner(Context context) {
        super(context);
    }

    public void setCellDimensions(int cellWidth, int cellHeight, int countX, int countY, int gapH, int gapV) {
        mCellWidth = cellWidth;
        mCellHeight = cellHeight;
        mCountX = countX;
        mCountY = countY;
        mWidthGap = gapH;
        mHeightGap = gapV;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        int startX, stopX, startY, stopY;
        for (int x = 0; x < mCountX; x ++) {
            startX = stopX = x * (mCellWidth + mWidthGap);
            startY = 0;
            stopY = getHeight();
            canvas.drawLine(startX, startY, stopX, stopY, PAINT);

            startX = stopX = startX + mCellWidth;
            canvas.drawLine(startX, startY, stopX, stopY, PAINT);
        }

        for (int y = 0; y < mCountY; y ++) {
            startY = stopY = y * (mCellHeight + mHeightGap);
            startX = 0;
            stopX = getWidth();
            canvas.drawLine(startX, startY, stopX, stopY, PAINT);

            startY = stopY = startY + mCellHeight;
            canvas.drawLine(startX, startY, stopX, stopY, PAINT);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSpecSize, heightSpecSize);

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                WidgetCellLayout.LayoutParams lp = (WidgetCellLayout.LayoutParams) child.getLayoutParams();
                lp.setup(mCellWidth, mCellHeight, mWidthGap, mHeightGap);

                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                WidgetCellLayout.LayoutParams lp = (WidgetCellLayout.LayoutParams) child.getLayoutParams();
                int childLeft = lp.x;
                int childTop = lp.y;
                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);

                if (lp.dropped) {
                    lp.dropped = false;
                }
            }
        }
    }

//    @Override
//    public boolean shouldDelayChildPressedState() {
//        return false;
//    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            Rect r = new Rect();
            child.getDrawingRect(r);
            requestRectangleOnScreen(r);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        // Cancel long press for all children
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.cancelLongPress();
        }
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            if (!view.isHardwareAccelerated() && enabled) {
                view.buildDrawingCache(true);
            }
        }
    }

    View getChildAt(int x, int y) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            WidgetCellLayout.LayoutParams lp = (WidgetCellLayout.LayoutParams) child.getLayoutParams();

            if ((lp.cellX <= x) && (x < lp.cellX + lp.cellHSpan)
                    && (lp.cellY <= y) && (y < lp.cellY + lp.cellVSpan)) {
                return child;
            }
        }
        return null;
    }

    void setupLayoutParams(WidgetCellLayout.LayoutParams lp) {
        lp.setup(mCellWidth, mCellHeight, mWidthGap, mHeightGap);
    }

    void setupLayoutParams(View child) {
        WidgetCellLayout.LayoutParams lp = (WidgetCellLayout.LayoutParams) child.getLayoutParams();
        lp.setup(mCellWidth, mCellHeight, mWidthGap, mHeightGap);
    }

}
