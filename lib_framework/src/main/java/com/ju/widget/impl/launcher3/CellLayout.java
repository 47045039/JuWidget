package com.ju.widget.impl.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;

import com.ju.widget.api.Config;
import com.ju.widget.impl.launcher3.util.AnimUtils;
import com.ju.widget.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

/**
 * @Author: liuqunshan@hisense.com
 * @Date: 2021/3/15
 * @Description:
 */
public class CellLayout extends ViewGroup {

    private static final String TAG = "WidgetCellLayout";

    private static final float REORDER_PREVIEW_MAGNITUDE = 0.12f;
    private static final int REORDER_ANIMATION_DURATION = 150;

    private static final boolean DESTRUCTIVE_REORDER = false;
    private static final boolean DEBUG_VISUALIZE_OCCUPIED = false;

    public static final int MODE_SHOW_REORDER_HINT = 0;
    public static final int MODE_DRAG_OVER = 1;
    public static final int MODE_ON_DROP = 2;
    public static final int MODE_ON_DROP_EXTERNAL = 3;
    public static final int MODE_ACCEPT_DROP = 4;

    private final ArrayMap<LayoutParams, Animator> mReorderAnimators = new ArrayMap<>();
    private final ArrayMap<View, ReorderPreviewAnimation> mShakeAnimators = new ArrayMap<>();

    private final ArrayList<View> mIntersectingViews = new ArrayList<View>();
    private final Stack<Rect> mTempRectStack = new Stack<Rect>();
    private final Config mUsedCellConfig = new Config();

    private final int[] mTmpXY = new int[2];
    private final int[] mTmpPoint = new int[2];
    private final int[] mTempLocation = new int[2];

    private final int[] mDragCell = new int[2];
    private boolean mDragging = false;
    private boolean mItemPlacementDirty = false;

    private final Rect mOccupiedRect = new Rect();
    private final Rect mTempRect = new Rect();

    private GridOccupancy mOccupied;
    private GridOccupancy mTmpOccupied;

    private float mReorderPreviewAnimationMagnitude = REORDER_PREVIEW_MAGNITUDE * 160;

    protected ShortcutAndWidgetContainer mWidgetContainerInner;
    private OnTouchListener mInterceptTouchListener;

    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // A ViewGroup usually does not draw, but CellLayout needs to draw a rectangle to show
        // the user where a dragged item will land when dropped.
        setWillNotDraw(false);
        setClipToPadding(false);

        setAlwaysDrawnWithCacheEnabled(false);

        mDragCell[0] = mDragCell[1] = -1;

        mWidgetContainerInner = new ShortcutAndWidgetContainer(context);
        addView(mWidgetContainerInner);

//        Launcher.attachCellLayout(context, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize =  MeasureSpec.getSize(heightMeasureSpec);
        final int childWidthSize = widthSize - getPaddingLeft() - getPaddingRight();
        final int childHeightSize = heightSize - getPaddingTop() - getPaddingBottom();

        final Config config = mUsedCellConfig;
        final int cellGapX = (childWidthSize - config.mCellWidth * config.mCellCountX) / (config.mCellCountX - 1);
        final int cellGapY = (childHeightSize - config.mCellHeight * config.mCellCountY) / (config.mCellCountY - 1);

        if (cellGapX < 0 || cellGapY < 0) {
            Log.e(TAG, "invalid layout: ", widthSize, heightSize, config);
//            throw new IllegalArgumentException("invalid layout: " + childWidthSize + " " + childWidthSize + " " + config);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        config.setGap(cellGapX, cellGapY);

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY);

        final ShortcutAndWidgetContainer child = mWidgetContainerInner;
        child.setCellDimensions(config.mCellWidth, config.mCellHeight,
                config.mCellCountX, config.mCellCountY,
                config.mCellGapX, config.mCellGapY);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

        setMeasuredDimension(child.getMeasuredWidth() + getPaddingLeft() + getPaddingRight(),
                child.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mWidgetContainerInner.layout(getPaddingLeft(), getPaddingTop(),
                r - l - getPaddingRight(), b - t - getPaddingBottom());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // First we clear the tag to ensure that on every touch down we start with a fresh slate,
        // even in the case where we return early. Not clearing here was causing bugs whereby on
        // long-press we'd end up picking up an item from a previous drag operation.
        // return mInterceptTouchListener != null && mInterceptTouchListener.onTouch(this, ev);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mWidgetContainerInner.cancelLongPress();
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        mWidgetContainerInner.setChildrenDrawingCacheEnabled(enabled);
    }

    @Override
    public int indexOfChild(View child) {
        return mWidgetContainerInner.indexOfChild(child);
    }

    @Override
    public int getChildCount() {
        return mWidgetContainerInner.getChildCount();
    }

    @Override
    public View getChildAt(int index) {
        return mWidgetContainerInner.getChildAt(index);
    }

    @Override
    public void removeAllViews() {
        mOccupied.clear();
        mWidgetContainerInner.removeAllViews();
    }

    @Override
    public void removeAllViewsInLayout() {
        if (mWidgetContainerInner.getChildCount() > 0) {
            mOccupied.clear();
            mWidgetContainerInner.removeAllViewsInLayout();
        }
    }

    @Override
    public void removeView(View view) {
        markCellsAsUnoccupiedForView(view);
        mWidgetContainerInner.removeView(view);
    }

    @Override
    public void removeViewAt(int index) {
        markCellsAsUnoccupiedForView(mWidgetContainerInner.getChildAt(index));
        mWidgetContainerInner.removeViewAt(index);
    }

    @Override
    public void removeViewInLayout(View view) {
        markCellsAsUnoccupiedForView(view);
        mWidgetContainerInner.removeViewInLayout(view);
    }

    @Override
    public void removeViews(int start, int count) {
        for (int i = start; i < start + count; i++) {
            markCellsAsUnoccupiedForView(mWidgetContainerInner.getChildAt(i));
        }
        mWidgetContainerInner.removeViews(start, count);
    }

    @Override
    public void removeViewsInLayout(int start, int count) {
        for (int i = start; i < start + count; i++) {
            markCellsAsUnoccupiedForView(mWidgetContainerInner.getChildAt(i));
        }
        mWidgetContainerInner.removeViewsInLayout(start, count);
    }

    public void removeViewWithoutMarkingCells(View view) {
        mWidgetContainerInner.removeView(view);
    }

    /**
     * 设置cell原始配置
     */
    protected void setOriginCellConfig(int cellW, int cellH, int cellCountX, int cellCountY) {
        invalidate();
        requestLayout();

        final Config config = mUsedCellConfig;
        config.mCellWidth = cellW;
        config.mCellHeight = cellH;
        config.mCellCountX = cellCountX;
        config.mCellCountY = cellCountY;

        mOccupied = new GridOccupancy(config.mCellCountX, config.mCellCountY);
        mTmpOccupied = new GridOccupancy(config.mCellCountX, config.mCellCountY);
    }

    protected void setOnInterceptTouchListener(View.OnTouchListener listener) {
        mInterceptTouchListener = listener;
    }

    protected boolean addViewToCells(View child, int index, int childId, LayoutParams params, boolean markCells) {
        // Generate an id for each view, this assumes we have at most 256x256 cells
        // per workspace screen
        final Config config = mUsedCellConfig;
        if (params.cellX >= 0 && params.cellX <= config.mCellCountX - 1
                && params.cellY >= 0 && params.cellY <= config.mCellCountY - 1) {
            // If the horizontal or vertical span is set to -1, it is taken to
            // mean that it spans the extent of the CellLayout
            if (params.cellHSpan < 0) {
                params.cellHSpan = mUsedCellConfig.mCellCountX;
            }
            if (params.cellVSpan < 0) {
                params.cellVSpan = mUsedCellConfig.mCellCountY;
            }

            child.setId(childId);

            mWidgetContainerInner.addView(child, index, params);

            if (markCells) {
                markCellsAsOccupiedForView(child);
            }

            return true;
        }
        return false;
    }

    /**
     * Given a point, return the cell that strictly encloses that point
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    void pointToCellExact(int x, int y, int[] result) {
        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();

        final Config config = mUsedCellConfig;
        result[0] = (x - hStartPadding) / (config.mCellWidth + config.mCellGapX);
        result[1] = (y - vStartPadding) / (config.mCellHeight + config.mCellGapY);

        final int xAxis = config.mCellCountX;
        final int yAxis = config.mCellCountY;

        if (result[0] < 0) {
            result[0] = 0;
        } else if (result[0] >= xAxis) {
            result[0] = xAxis - 1;
        }
        if (result[1] < 0) {
            result[1] = 0;
        } else if (result[1] >= yAxis) {
            result[1] = yAxis - 1;
        }
    }

    /**
     * Given a point, return the cell that most closely encloses that point
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    void pointToCellRounded(int x, int y, int[] result) {
        pointToCellExact(x + (mUsedCellConfig.mCellWidth / 2), y + (mUsedCellConfig.mCellHeight / 2), result);
    }

    /**
     * Given a cell coordinate, return the point that represents the upper left corner of that cell
     *
     * @param cellX X coordinate of the cell
     * @param cellY Y coordinate of the cell
     *
     * @param result Array of 2 ints to hold the x and y coordinate of the point
     */
    void cellToPoint(int cellX, int cellY, int[] result) {
        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();

        final Config config = mUsedCellConfig;
        result[0] = hStartPadding + cellX * (config.mCellWidth + config.mCellGapX);
        result[1] = vStartPadding + cellY * (config.mCellHeight + config.mCellGapY);
    }

    /**
     * Given a cell coordinate, return the point that represents the center of the cell
     *
     * @param cellX X coordinate of the cell
     * @param cellY Y coordinate of the cell
     *
     * @param result Array of 2 ints to hold the x and y coordinate of the point
     */
    void cellToCenterPoint(int cellX, int cellY, int[] result) {
        regionToCenterPoint(cellX, cellY, 1, 1, result);
    }

    /**
     * Given a cell coordinate and span return the point that represents the center of the regio
     *
     * @param cellX X coordinate of the cell
     * @param cellY Y coordinate of the cell
     *
     * @param result Array of 2 ints to hold the x and y coordinate of the point
     */
    void regionToCenterPoint(int cellX, int cellY, int spanX, int spanY, int[] result) {
        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();

        final Config config = mUsedCellConfig;
        result[0] = hStartPadding + cellX * (config.mCellWidth + config.mCellGapX) +
                (spanX * config.mCellWidth + (spanX - 1) * config.mCellGapX) / 2;
        result[1] = vStartPadding + cellY * (config.mCellHeight + config.mCellGapY) +
                (spanY * config.mCellHeight + (spanY - 1) * config.mCellGapY) / 2;
    }

    /**
     * Given a cell coordinate and span fills out a corresponding pixel rect
     *
     * @param cellX X coordinate of the cell
     * @param cellY Y coordinate of the cell
     * @param result Rect in which to write the result
     */
    void regionToRect(int cellX, int cellY, int spanX, int spanY, Rect result) {
        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();

        final Config config = mUsedCellConfig;
        final int left = hStartPadding + cellX * (config.mCellWidth + config.mCellGapX);
        final int top = vStartPadding + cellY * (config.mCellHeight + config.mCellGapY);
        result.set(left, top, left + (spanX * config.mCellWidth + (spanX - 1) * config.mCellGapX),
                top + (spanY * config.mCellHeight + (spanY - 1) * config.mCellGapY));
    }

    public float getDistanceFromCell(float x, float y, int[] cell) {
        final int[] temp = mTmpPoint;
        cellToCenterPoint(cell[0], cell[1], temp);
        float distance = (float) Math.sqrt( Math.pow(x - temp[0], 2) + Math.pow(y - temp[1], 2));
        return distance;
    }

    Rect getContentRect(Rect r) {
        if (r == null) {
            r = new Rect();
        }
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = left + getWidth() - getPaddingLeft() - getPaddingRight();
        int bottom = top + getHeight() - getPaddingTop() - getPaddingBottom();
        r.set(left, top, right, bottom);
        return r;
    }

    public float getChildrenScale() {
        return 1.0f;
    }

    public ShortcutAndWidgetContainer getShortcutsAndWidgets() {
        return mWidgetContainerInner;
    }

    public View getChildAt(int x, int y) {
        return mWidgetContainerInner.getChildAt(x, y);
    }

    public boolean animateChildToPosition(final View child, int cellX, int cellY, int duration,
            int delay, boolean permanent, boolean adjustOccupied) {
        ShortcutAndWidgetContainer clc = getShortcutsAndWidgets();

        if (clc.indexOfChild(child) != -1) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final ItemInfo info = (ItemInfo) child.getTag();

            // We cancel any existing animations
            if (mReorderAnimators.containsKey(lp)) {
                mReorderAnimators.get(lp).cancel();
                mReorderAnimators.remove(lp);
            }

            final int oldX = lp.x;
            final int oldY = lp.y;
            if (adjustOccupied) {
                GridOccupancy occupied = permanent ? mOccupied : mTmpOccupied;
                occupied.markCells(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, false);
                occupied.markCells(cellX, cellY, lp.cellHSpan, lp.cellVSpan, true);
            }
            lp.isLockedToGrid = true;
            if (permanent) {
                lp.cellX = info.cellX = cellX;
                lp.cellY = info.cellY = cellY;
            } else {
                lp.tmpCellX = cellX;
                lp.tmpCellY = cellY;
            }
            clc.setupLayoutParams(child);
            lp.isLockedToGrid = false;
            final int newX = lp.x;
            final int newY = lp.y;

            lp.x = oldX;
            lp.y = oldY;

            // Exit early if we're not actually moving the view
            if (oldX == newX && oldY == newY) {
                lp.isLockedToGrid = true;
                return true;
            }

            ValueAnimator va = AnimUtils.ofFloat(child, 0f, 1f);
            va.setDuration(duration);
            mReorderAnimators.put(lp, va);

            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float r = (Float) animation.getAnimatedValue();
                    lp.x = (int) ((1 - r) * oldX + r * newX);
                    lp.y = (int) ((1 - r) * oldY + r * newY);
                    child.requestLayout();
                }
            });
            va.addListener(new AnimatorListenerAdapter() {
                boolean cancelled = false;
                public void onAnimationEnd(Animator animation) {
                    // If the animation was cancelled, it means that another animation
                    // has interrupted this one, and we don't want to lock the item into
                    // place just yet.
                    if (!cancelled) {
                        lp.isLockedToGrid = true;
                        child.requestLayout();
                    }
                    if (mReorderAnimators.containsKey(lp)) {
                        mReorderAnimators.remove(lp);
                    }
                }
                public void onAnimationCancel(Animator animation) {
                    cancelled = true;
                }
            });
            va.setStartDelay(delay);
            va.start();
            return true;
        }
        return false;
    }

    void visualizeDropLocation(View v, Bitmap dragOutline, int originX, int originY, int cellX,
            int cellY, int spanX, int spanY, boolean resize, Point dragOffset, Rect dragRegion) {
        final int oldDragCellX = mDragCell[0];
        final int oldDragCellY = mDragCell[1];

        if (dragOutline == null && v == null) {
            return;
        }

        final Config config = mUsedCellConfig;

        if (cellX != oldDragCellX || cellY != oldDragCellY) {
            mDragCell[0] = cellX;
            mDragCell[1] = cellY;
            // Find the top left corner of the rect the object will occupy
            final int[] topLeft = mTmpPoint;
            cellToPoint(cellX, cellY, topLeft);

            int left = topLeft[0];
            int top = topLeft[1];

            if (v != null && dragOffset == null) {
                // When drawing the drag outline, it did not account for margin offsets
                // added by the view's parent.
                MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
                left += lp.leftMargin;
                top += lp.topMargin;

                // Offsets due to the size difference between the View and the dragOutline.
                // There is a size difference to account for the outer blur, which may lie
                // outside the bounds of the view.
                top += (v.getHeight() - dragOutline.getHeight()) / 2;
                // We center about the x axis
                left += ((config.mCellWidth * spanX) + ((spanX - 1) * config.mCellGapX)
                        - dragOutline.getWidth()) / 2;
            } else {
                if (dragOffset != null && dragRegion != null) {
                    // Center the drag region *horizontally* in the cell and apply a drag
                    // outline offset
                    left += dragOffset.x + ((config.mCellWidth * spanX) + ((spanX - 1) * config.mCellGapX)
                            - dragRegion.width()) / 2;
                    int cHeight = config.mCellHeight;
                    int cellPaddingY = (int) Math.max(0, ((config.mCellHeight - cHeight) / 2f));
                    top += dragOffset.y + cellPaddingY;
                } else {
                    // Center the drag outline in the cell
                    left += ((config.mCellWidth * spanX) + ((spanX - 1) * config.mCellGapX)
                            - dragOutline.getWidth()) / 2;
                    top += ((config.mCellHeight * spanY) + ((spanY - 1) * config.mCellGapY)
                            - dragOutline.getHeight()) / 2;
                }
            }

//            final int oldIndex = mDragOutlineCurrent;
//            mDragOutlineAnims[oldIndex].animateOut();
//            mDragOutlineCurrent = (oldIndex + 1) % mDragOutlines.length;
//            Rect r = mDragOutlines[mDragOutlineCurrent];
//            r.set(left, top, left + dragOutline.getWidth(), top + dragOutline.getHeight());
//            if (resize) {
//                cellToRect(cellX, cellY, spanX, spanY, r);
//            }
//
//            mDragOutlineAnims[mDragOutlineCurrent].setTag(dragOutline);
//            mDragOutlineAnims[mDragOutlineCurrent].animateIn();
        }
    }

    public void clearDragOutlines() {
//        final int oldIndex = mDragOutlineCurrent;
//        mDragOutlineAnims[oldIndex].animateOut();
        mDragCell[0] = mDragCell[1] = -1;
    }

    /**
     * Find a vacant area that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     *
     * @param pixelX The X location at which you want to search for a vacant area.
     * @param pixelY The Y location at which you want to search for a vacant area.
     * @param minSpanX The minimum horizontal span required
     * @param minSpanY The minimum vertical span required
     * @param spanX Horizontal span of the object.
     * @param spanY Vertical span of the object.
     * @param result Array in which to place the result, or null (in which case a new array will
     *        be allocated)
     * @return The X, Y cell of a vacant area that can contain this object,
     *         nearest the requested location.
     */
    int[] findNearestVacantArea(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX,
            int spanY, int[] result, int[] resultSpan) {
        return findNearestArea(pixelX, pixelY, minSpanX, minSpanY, spanX, spanY, true,
                result, resultSpan);
    }

    /**
     * Find a vacant area that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     *
     * @param pixelX The X location at which you want to search for a vacant area.
     * @param pixelY The Y location at which you want to search for a vacant area.
     * @param minSpanX The minimum horizontal span required
     * @param minSpanY The minimum vertical span required
     * @param spanX Horizontal span of the object.
     * @param spanY Vertical span of the object.
     * @param ignoreOccupied If true, the result can be an occupied cell
     * @param result Array in which to place the result, or null (in which case a new array will
     *        be allocated)
     * @return The X, Y cell of a vacant area that can contain this object,
     *         nearest the requested location.
     */
    private int[] findNearestArea(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX,
            int spanY, boolean ignoreOccupied, int[] result, int[] resultSpan) {
        lazyInitTempRectStack();

        // For items with a spanX / spanY > 1, the passed in point (pixelX, pixelY) corresponds
        // to the center of the item, but we are searching based on the top-left cell, so
        // we translate the point over to correspond to the top-left.
        final Config config = mUsedCellConfig;
        pixelX -= (config.mCellWidth + config.mCellGapX) * (spanX - 1) / 2f;
        pixelY -= (config.mCellHeight + config.mCellGapY) * (spanY - 1) / 2f;

        // Keep track of best-scoring drop area
        final int[] bestXY = result != null ? result : new int[2];
        double bestDistance = Double.MAX_VALUE;
        final Rect bestRect = new Rect(-1, -1, -1, -1);
        final Stack<Rect> validRegions = new Stack<>();

        final int countX = config.mCellCountX;
        final int countY = config.mCellCountY;

        if (minSpanX <= 0 || minSpanY <= 0 || spanX <= 0 || spanY <= 0 ||
                spanX < minSpanX || spanY < minSpanY) {
            return bestXY;
        }

        for (int y = 0; y < countY - (minSpanY - 1); y++) {
            inner:
            for (int x = 0; x < countX - (minSpanX - 1); x++) {
                int ySize = -1;
                int xSize = -1;
                if (ignoreOccupied) {
                    // First, let's see if this thing fits anywhere
                    for (int i = 0; i < minSpanX; i++) {
                        for (int j = 0; j < minSpanY; j++) {
                            if (mOccupied.cells[x + i][y + j]) {
                                continue inner;
                            }
                        }
                    }
                    xSize = minSpanX;
                    ySize = minSpanY;

                    // We know that the item will fit at _some_ acceptable size, now let's see
                    // how big we can make it. We'll alternate between incrementing x and y spans
                    // until we hit a limit.
                    boolean incX = true;
                    boolean hitMaxX = xSize >= spanX;
                    boolean hitMaxY = ySize >= spanY;
                    while (!(hitMaxX && hitMaxY)) {
                        if (incX && !hitMaxX) {
                            for (int j = 0; j < ySize; j++) {
                                if (x + xSize > countX -1 || mOccupied.cells[x + xSize][y + j]) {
                                    // We can't move out horizontally
                                    hitMaxX = true;
                                }
                            }
                            if (!hitMaxX) {
                                xSize++;
                            }
                        } else if (!hitMaxY) {
                            for (int i = 0; i < xSize; i++) {
                                if (y + ySize > countY - 1 || mOccupied.cells[x + i][y + ySize]) {
                                    // We can't move out vertically
                                    hitMaxY = true;
                                }
                            }
                            if (!hitMaxY) {
                                ySize++;
                            }
                        }
                        hitMaxX |= xSize >= spanX;
                        hitMaxY |= ySize >= spanY;
                        incX = !incX;
                    }
                    incX = true;
                    hitMaxX = xSize >= spanX;
                    hitMaxY = ySize >= spanY;
                }
                final int[] cellXY = mTmpPoint;
                cellToCenterPoint(x, y, cellXY);

                // We verify that the current rect is not a sub-rect of any of our previous
                // candidates. In this case, the current rect is disqualified in favour of the
                // containing rect.
                Rect currentRect = mTempRectStack.pop();
                currentRect.set(x, y, x + xSize, y + ySize);
                boolean contained = false;
                for (Rect r : validRegions) {
                    if (r.contains(currentRect)) {
                        contained = true;
                        break;
                    }
                }
                validRegions.push(currentRect);
                double distance = Math.hypot(cellXY[0] - pixelX,  cellXY[1] - pixelY);

                if ((distance <= bestDistance && !contained) ||
                        currentRect.contains(bestRect)) {
                    bestDistance = distance;
                    bestXY[0] = x;
                    bestXY[1] = y;
                    if (resultSpan != null) {
                        resultSpan[0] = xSize;
                        resultSpan[1] = ySize;
                    }
                    bestRect.set(currentRect);
                }
            }
        }

        // Return -1, -1 if no suitable location found
        if (bestDistance == Double.MAX_VALUE) {
            bestXY[0] = -1;
            bestXY[1] = -1;
        }
        recycleTempRects(validRegions);
        return bestXY;
    }

    /**
     * Find a vacant area that will fit the given bounds nearest the requested
     * cell location, and will also weigh in a suggested direction vector of the
     * desired location. This method computers distance based on unit grid distances,
     * not pixel distances.
     *
     * @param cellX The X cell nearest to which you want to search for a vacant area.
     * @param cellY The Y cell nearest which you want to search for a vacant area.
     * @param spanX Horizontal span of the object.
     * @param spanY Vertical span of the object.
     * @param direction The favored direction in which the views should move from cellX, cellY
     * @param occupied The array which represents which cells in the CellLayout are occupied
     * @param blockOccupied The array which represents which cells in the specified block (cellX,
     *        cellY, spanX, spanY) are occupied. This is used when try to move a group of views.
     * @param result Array in which to place the result, or null (in which case a new array will
     *        be allocated)
     * @return The X, Y cell of a vacant area that can contain this object,
     *         nearest the requested location.
     */
    private int[] findNearestArea(int cellX, int cellY, int spanX, int spanY, int[] direction,
            boolean[][] occupied, boolean blockOccupied[][], int[] result) {
        // Keep track of best-scoring drop area
        final int[] bestXY = result != null ? result : new int[2];
        float bestDistance = Float.MAX_VALUE;
        int bestDirectionScore = Integer.MIN_VALUE;

        final Config config = mUsedCellConfig;
        final int countX = config.mCellCountX;
        final int countY = config.mCellCountY;

        for (int y = 0; y < countY - (spanY - 1); y++) {
            inner:
            for (int x = 0; x < countX - (spanX - 1); x++) {
                // First, let's see if this thing fits anywhere
                for (int i = 0; i < spanX; i++) {
                    for (int j = 0; j < spanY; j++) {
                        if (occupied[x + i][y + j] && (blockOccupied == null || blockOccupied[i][j])) {
                            continue inner;
                        }
                    }
                }

                float distance = (float) Math.hypot(x - cellX, y - cellY);
                int[] curDirection = mTmpPoint;
                computeDirectionVector(x - cellX, y - cellY, curDirection);
                // The direction score is just the dot product of the two candidate direction
                // and that passed in.
                int curDirectionScore = direction[0] * curDirection[0] +
                        direction[1] * curDirection[1];
                if (Float.compare(distance,  bestDistance) < 0 ||
                        (Float.compare(distance, bestDistance) == 0
                                && curDirectionScore > bestDirectionScore)) {
                    bestDistance = distance;
                    bestDirectionScore = curDirectionScore;
                    bestXY[0] = x;
                    bestXY[1] = y;
                }
            }
        }

        // Return -1, -1 if no suitable location found
        if (bestDistance == Float.MAX_VALUE) {
            bestXY[0] = -1;
            bestXY[1] = -1;
        }
        return bestXY;
    }

    private void lazyInitTempRectStack() {
        if (mTempRectStack.isEmpty()) {
            for (int i = 0; i < mUsedCellConfig.mCellCountX * mUsedCellConfig.mCellCountY; i++) {
                mTempRectStack.push(new Rect());
            }
        }
    }

    private void recycleTempRects(Stack<Rect> used) {
        while (!used.isEmpty()) {
            mTempRectStack.push(used.pop());
        }
    }

    private boolean addViewToTempLocation(View v, Rect rectOccupiedByPotentialDrop,
            int[] direction, ItemConfiguration currentState) {
        CellAndSpan c = currentState.map.get(v);
        boolean success = false;
        mTmpOccupied.markCells(c, false);
        mTmpOccupied.markCells(rectOccupiedByPotentialDrop, true);

        findNearestArea(c.cellX, c.cellY, c.spanX, c.spanY, direction,
                mTmpOccupied.cells, null, mTempLocation);

        if (mTempLocation[0] >= 0 && mTempLocation[1] >= 0) {
            c.cellX = mTempLocation[0];
            c.cellY = mTempLocation[1];
            success = true;
        }
        mTmpOccupied.markCells(c, true);
        return success;
    }

    /**
     * This helper class defines a cluster of views. It helps with defining complex edges
     * of the cluster and determining how those edges interact with other views. The edges
     * essentially define a fine-grained boundary around the cluster of views -- like a more
     * precise version of a bounding box.
     */
    private class ViewCluster {
        final static int LEFT = 0;
        final static int TOP = 1;
        final static int RIGHT = 2;
        final static int BOTTOM = 3;

        ArrayList<View> views;
        ItemConfiguration config;
        Rect boundingRect = new Rect();

        int[] leftEdge = new int[mUsedCellConfig.mCellCountY];
        int[] rightEdge = new int[mUsedCellConfig.mCellCountY];
        int[] topEdge = new int[mUsedCellConfig.mCellCountX];
        int[] bottomEdge = new int[mUsedCellConfig.mCellCountX];
        boolean leftEdgeDirty, rightEdgeDirty, topEdgeDirty, bottomEdgeDirty, boundingRectDirty;

        @SuppressWarnings("unchecked")
        public ViewCluster(ArrayList<View> views, ItemConfiguration config) {
            this.views = (ArrayList<View>) views.clone();
            this.config = config;
            resetEdges();
        }

        void resetEdges() {
            for (int i = 0; i < mUsedCellConfig.mCellCountX; i++) {
                topEdge[i] = -1;
                bottomEdge[i] = -1;
            }
            for (int i = 0; i < mUsedCellConfig.mCellCountY; i++) {
                leftEdge[i] = -1;
                rightEdge[i] = -1;
            }
            leftEdgeDirty = true;
            rightEdgeDirty = true;
            bottomEdgeDirty = true;
            topEdgeDirty = true;
            boundingRectDirty = true;
        }

        void computeEdge(int which, int[] edge) {
            int count = views.size();
            for (int i = 0; i < count; i++) {
                CellAndSpan cs = config.map.get(views.get(i));
                switch (which) {
                    case LEFT:
                        int left = cs.cellX;
                        for (int j = cs.cellY; j < cs.cellY + cs.spanY; j++) {
                            if (left < edge[j] || edge[j] < 0) {
                                edge[j] = left;
                            }
                        }
                        break;
                    case RIGHT:
                        int right = cs.cellX + cs.spanX;
                        for (int j = cs.cellY; j < cs.cellY + cs.spanY; j++) {
                            if (right > edge[j]) {
                                edge[j] = right;
                            }
                        }
                        break;
                    case TOP:
                        int top = cs.cellY;
                        for (int j = cs.cellX; j < cs.cellX + cs.spanX; j++) {
                            if (top < edge[j] || edge[j] < 0) {
                                edge[j] = top;
                            }
                        }
                        break;
                    case BOTTOM:
                        int bottom = cs.cellY + cs.spanY;
                        for (int j = cs.cellX; j < cs.cellX + cs.spanX; j++) {
                            if (bottom > edge[j]) {
                                edge[j] = bottom;
                            }
                        }
                        break;
                }
            }
        }

        boolean isViewTouchingEdge(View v, int whichEdge) {
            CellAndSpan cs = config.map.get(v);

            int[] edge = getEdge(whichEdge);

            switch (whichEdge) {
                case LEFT:
                    for (int i = cs.cellY; i < cs.cellY + cs.spanY; i++) {
                        if (edge[i] == cs.cellX + cs.spanX) {
                            return true;
                        }
                    }
                    break;
                case RIGHT:
                    for (int i = cs.cellY; i < cs.cellY + cs.spanY; i++) {
                        if (edge[i] == cs.cellX) {
                            return true;
                        }
                    }
                    break;
                case TOP:
                    for (int i = cs.cellX; i < cs.cellX + cs.spanX; i++) {
                        if (edge[i] == cs.cellY + cs.spanY) {
                            return true;
                        }
                    }
                    break;
                case BOTTOM:
                    for (int i = cs.cellX; i < cs.cellX + cs.spanX; i++) {
                        if (edge[i] == cs.cellY) {
                            return true;
                        }
                    }
                    break;
            }
            return false;
        }

        void shift(int whichEdge, int delta) {
            for (View v: views) {
                CellAndSpan c = config.map.get(v);
                switch (whichEdge) {
                    case LEFT:
                        c.cellX -= delta;
                        break;
                    case RIGHT:
                        c.cellX += delta;
                        break;
                    case TOP:
                        c.cellY -= delta;
                        break;
                    case BOTTOM:
                    default:
                        c.cellY += delta;
                        break;
                }
            }
            resetEdges();
        }

        public void addView(View v) {
            views.add(v);
            resetEdges();
        }

        public Rect getBoundingRect() {
            if (boundingRectDirty) {
                boolean first = true;
                for (View v: views) {
                    CellAndSpan c = config.map.get(v);
                    if (first) {
                        boundingRect.set(c.cellX, c.cellY, c.cellX + c.spanX, c.cellY + c.spanY);
                        first = false;
                    } else {
                        boundingRect.union(c.cellX, c.cellY, c.cellX + c.spanX, c.cellY + c.spanY);
                    }
                }
            }
            return boundingRect;
        }

        public int[] getEdge(int which) {
            switch (which) {
                case LEFT:
                    return getLeftEdge();
                case RIGHT:
                    return getRightEdge();
                case TOP:
                    return getTopEdge();
                case BOTTOM:
                default:
                    return getBottomEdge();
            }
        }

        public int[] getLeftEdge() {
            if (leftEdgeDirty) {
                computeEdge(LEFT, leftEdge);
            }
            return leftEdge;
        }

        public int[] getRightEdge() {
            if (rightEdgeDirty) {
                computeEdge(RIGHT, rightEdge);
            }
            return rightEdge;
        }

        public int[] getTopEdge() {
            if (topEdgeDirty) {
                computeEdge(TOP, topEdge);
            }
            return topEdge;
        }

        public int[] getBottomEdge() {
            if (bottomEdgeDirty) {
                computeEdge(BOTTOM, bottomEdge);
            }
            return bottomEdge;
        }

        PositionComparator comparator = new PositionComparator();
        class PositionComparator implements Comparator<View> {
            int whichEdge = 0;
            public int compare(View left, View right) {
                CellAndSpan l = config.map.get(left);
                CellAndSpan r = config.map.get(right);
                switch (whichEdge) {
                    case LEFT:
                        return (r.cellX + r.spanX) - (l.cellX + l.spanX);
                    case RIGHT:
                        return l.cellX - r.cellX;
                    case TOP:
                        return (r.cellY + r.spanY) - (l.cellY + l.spanY);
                    case BOTTOM:
                    default:
                        return l.cellY - r.cellY;
                }
            }
        }

        public void sortConfigurationForEdgePush(int edge) {
            comparator.whichEdge = edge;
            Collections.sort(config.sortedViews, comparator);
        }
    }

    private boolean pushViewsToTempLocation(ArrayList<View> views, Rect rectOccupiedByPotentialDrop,
            int[] direction, View dragView, ItemConfiguration currentState) {

        ViewCluster cluster = new ViewCluster(views, currentState);
        Rect clusterRect = cluster.getBoundingRect();
        int whichEdge;
        int pushDistance;
        boolean fail = false;

        // Determine the edge of the cluster that will be leading the push and how far
        // the cluster must be shifted.
        if (direction[0] < 0) {
            whichEdge = ViewCluster.LEFT;
            pushDistance = clusterRect.right - rectOccupiedByPotentialDrop.left;
        } else if (direction[0] > 0) {
            whichEdge = ViewCluster.RIGHT;
            pushDistance = rectOccupiedByPotentialDrop.right - clusterRect.left;
        } else if (direction[1] < 0) {
            whichEdge = ViewCluster.TOP;
            pushDistance = clusterRect.bottom - rectOccupiedByPotentialDrop.top;
        } else {
            whichEdge = ViewCluster.BOTTOM;
            pushDistance = rectOccupiedByPotentialDrop.bottom - clusterRect.top;
        }

        // Break early for invalid push distance.
        if (pushDistance <= 0) {
            return false;
        }

        // Mark the occupied state as false for the group of views we want to move.
        for (View v: views) {
            CellAndSpan c = currentState.map.get(v);
            mTmpOccupied.markCells(c, false);
        }

        // We save the current configuration -- if we fail to find a solution we will revert
        // to the initial state. The process of finding a solution modifies the configuration
        // in place, hence the need for revert in the failure case.
        currentState.save();

        // The pushing algorithm is simplified by considering the views in the order in which
        // they would be pushed by the cluster. For example, if the cluster is leading with its
        // left edge, we consider sort the views by their right edge, from right to left.
        cluster.sortConfigurationForEdgePush(whichEdge);

        while (pushDistance > 0 && !fail) {
            for (View v: currentState.sortedViews) {
                // For each view that isn't in the cluster, we see if the leading edge of the
                // cluster is contacting the edge of that view. If so, we add that view to the
                // cluster.
                if (!cluster.views.contains(v) && v != dragView) {
                    if (cluster.isViewTouchingEdge(v, whichEdge)) {
                        LayoutParams lp = (LayoutParams) v.getLayoutParams();
                        if (!lp.canReorder) {
                            // The push solution includes the all apps button, this is not viable.
                            fail = true;
                            break;
                        }
                        cluster.addView(v);
                        CellAndSpan c = currentState.map.get(v);

                        // Adding view to cluster, mark it as not occupied.
                        mTmpOccupied.markCells(c, false);
                    }
                }
            }
            pushDistance--;

            // The cluster has been completed, now we move the whole thing over in the appropriate
            // direction.
            cluster.shift(whichEdge, 1);
        }

        boolean foundSolution = false;
        clusterRect = cluster.getBoundingRect();

        // Due to the nature of the algorithm, the only check required to verify a valid solution
        // is to ensure that completed shifted cluster lies completely within the cell layout.
        if (!fail && clusterRect.left >= 0 && clusterRect.right <= mUsedCellConfig.mCellCountX && clusterRect.top >= 0 &&
                clusterRect.bottom <= mUsedCellConfig.mCellCountY) {
            foundSolution = true;
        } else {
            currentState.restore();
        }

        // In either case, we set the occupied array as marked for the location of the views
        for (View v: cluster.views) {
            CellAndSpan c = currentState.map.get(v);
            mTmpOccupied.markCells(c, true);
        }

        return foundSolution;
    }

    private boolean addViewsToTempLocation(ArrayList<View> views, Rect rectOccupiedByPotentialDrop,
            int[] direction, View dragView, ItemConfiguration currentState) {
        if (views.size() == 0) return true;

        boolean success = false;
        Rect boundingRect = new Rect();
        // We construct a rect which represents the entire group of views passed in
        // TODO:
//        currentState.getBoundingRectForViews(views, boundingRect);

        // Mark the occupied state as false for the group of views we want to move.
        for (View v: views) {
            CellAndSpan c = currentState.map.get(v);
            mTmpOccupied.markCells(c, false);
        }

        GridOccupancy blockOccupied = new GridOccupancy(boundingRect.width(), boundingRect.height());
        int top = boundingRect.top;
        int left = boundingRect.left;
        // We mark more precisely which parts of the bounding rect are truly occupied, allowing
        // for interlocking.
        for (View v: views) {
            CellAndSpan c = currentState.map.get(v);
            blockOccupied.markCells(c.cellX - left, c.cellY - top, c.spanX, c.spanY, true);
        }

        mTmpOccupied.markCells(rectOccupiedByPotentialDrop, true);

        findNearestArea(boundingRect.left, boundingRect.top, boundingRect.width(),
                boundingRect.height(), direction,
                mTmpOccupied.cells, blockOccupied.cells, mTempLocation);

        // If we successfuly found a location by pushing the block of views, we commit it
        if (mTempLocation[0] >= 0 && mTempLocation[1] >= 0) {
            int deltaX = mTempLocation[0] - boundingRect.left;
            int deltaY = mTempLocation[1] - boundingRect.top;
            for (View v: views) {
                CellAndSpan c = currentState.map.get(v);
                c.cellX += deltaX;
                c.cellY += deltaY;
            }
            success = true;
        }

        // In either case, we set the occupied array as marked for the location of the views
        for (View v: views) {
            CellAndSpan c = currentState.map.get(v);
            mTmpOccupied.markCells(c, true);
        }
        return success;
    }

    // This method tries to find a reordering solution which satisfies the push mechanic by trying
    // to push items in each of the cardinal directions, in an order based on the direction vector
    // passed.
    private boolean attemptPushInDirection(ArrayList<View> intersectingViews, Rect occupied,
            int[] direction, View ignoreView, ItemConfiguration solution) {
        if ((Math.abs(direction[0]) + Math.abs(direction[1])) > 1) {
            // If the direction vector has two non-zero components, we try pushing
            // separately in each of the components.
            int temp = direction[1];
            direction[1] = 0;

            if (pushViewsToTempLocation(intersectingViews, occupied, direction,
                    ignoreView, solution)) {
                return true;
            }
            direction[1] = temp;
            temp = direction[0];
            direction[0] = 0;

            if (pushViewsToTempLocation(intersectingViews, occupied, direction,
                    ignoreView, solution)) {
                return true;
            }
            // Revert the direction
            direction[0] = temp;

            // Now we try pushing in each component of the opposite direction
            direction[0] *= -1;
            direction[1] *= -1;
            temp = direction[1];
            direction[1] = 0;
            if (pushViewsToTempLocation(intersectingViews, occupied, direction,
                    ignoreView, solution)) {
                return true;
            }

            direction[1] = temp;
            temp = direction[0];
            direction[0] = 0;
            if (pushViewsToTempLocation(intersectingViews, occupied, direction,
                    ignoreView, solution)) {
                return true;
            }
            // revert the direction
            direction[0] = temp;
            direction[0] *= -1;
            direction[1] *= -1;

        } else {
            // If the direction vector has a single non-zero component, we push first in the
            // direction of the vector
            if (pushViewsToTempLocation(intersectingViews, occupied, direction,
                    ignoreView, solution)) {
                return true;
            }
            // Then we try the opposite direction
            direction[0] *= -1;
            direction[1] *= -1;
            if (pushViewsToTempLocation(intersectingViews, occupied, direction,
                    ignoreView, solution)) {
                return true;
            }
            // Switch the direction back
            direction[0] *= -1;
            direction[1] *= -1;

            // If we have failed to find a push solution with the above, then we try
            // to find a solution by pushing along the perpendicular axis.

            // Swap the components
            int temp = direction[1];
            direction[1] = direction[0];
            direction[0] = temp;
            if (pushViewsToTempLocation(intersectingViews, occupied, direction,
                    ignoreView, solution)) {
                return true;
            }

            // Then we try the opposite direction
            direction[0] *= -1;
            direction[1] *= -1;
            if (pushViewsToTempLocation(intersectingViews, occupied, direction,
                    ignoreView, solution)) {
                return true;
            }
            // Switch the direction back
            direction[0] *= -1;
            direction[1] *= -1;

            // Swap the components back
            temp = direction[1];
            direction[1] = direction[0];
            direction[0] = temp;
        }
        return false;
    }

    private boolean rearrangementExists(int cellX, int cellY, int spanX, int spanY, int[] direction,
            View ignoreView, ItemConfiguration solution) {
        // Return early if get invalid cell positions
        if (cellX < 0 || cellY < 0) return false;

        mIntersectingViews.clear();
        mOccupiedRect.set(cellX, cellY, cellX + spanX, cellY + spanY);

        // Mark the desired location of the view currently being dragged.
        if (ignoreView != null) {
            CellAndSpan c = solution.map.get(ignoreView);
            if (c != null) {
                c.cellX = cellX;
                c.cellY = cellY;
            }
        }
        Rect r0 = new Rect(cellX, cellY, cellX + spanX, cellY + spanY);
        Rect r1 = new Rect();
        for (View child: solution.map.keySet()) {
            if (child == ignoreView) continue;
            CellAndSpan c = solution.map.get(child);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            r1.set(c.cellX, c.cellY, c.cellX + c.spanX, c.cellY + c.spanY);
            if (Rect.intersects(r0, r1)) {
                if (!lp.canReorder) {
                    return false;
                }
                mIntersectingViews.add(child);
            }
        }

        solution.intersectingViews = new ArrayList<>(mIntersectingViews);

        // First we try to find a solution which respects the push mechanic. That is,
        // we try to find a solution such that no displaced item travels through another item
        // without also displacing that item.
        if (attemptPushInDirection(mIntersectingViews, mOccupiedRect, direction, ignoreView,
                solution)) {
            return true;
        }

        // Next we try moving the views as a block, but without requiring the push mechanic.
        if (addViewsToTempLocation(mIntersectingViews, mOccupiedRect, direction, ignoreView,
                solution)) {
            return true;
        }

        // Ok, they couldn't move as a block, let's move them individually
        for (View v : mIntersectingViews) {
            if (!addViewToTempLocation(v, mOccupiedRect, direction, solution)) {
                return false;
            }
        }
        return true;
    }

    /*
     * Returns a pair (x, y), where x,y are in {-1, 0, 1} corresponding to vector between
     * the provided point and the provided cell
     */
    private void computeDirectionVector(float deltaX, float deltaY, int[] result) {
        double angle = Math.atan(deltaY / deltaX);

        result[0] = 0;
        result[1] = 0;
        if (Math.abs(Math.cos(angle)) > 0.5f) {
            result[0] = (int) Math.signum(deltaX);
        }
        if (Math.abs(Math.sin(angle)) > 0.5f) {
            result[1] = (int) Math.signum(deltaY);
        }
    }

    private ItemConfiguration findReorderSolution(int pixelX, int pixelY, int minSpanX, int minSpanY,
            int spanX, int spanY, int[] direction, View dragView, boolean decX,
            ItemConfiguration solution) {
        // Copy the current state into the solution. This solution will be manipulated as necessary.
        copyCurrentStateToSolution(solution, false);
        // Copy the current occupied array into the temporary occupied array. This array will be
        // manipulated as necessary to find a solution.
        mOccupied.copyTo(mTmpOccupied);

        // We find the nearest cell into which we would place the dragged item, assuming there's
        // nothing in its way.
        int result[] = new int[2];
        result = findNearestArea(pixelX, pixelY, spanX, spanY, result);

        boolean success;
        // First we try the exact nearest position of the item being dragged,
        // we will then want to try to move this around to other neighbouring positions
        success = rearrangementExists(result[0], result[1], spanX, spanY, direction, dragView,
                solution);

        if (!success) {
            // We try shrinking the widget down to size in an alternating pattern, shrink 1 in
            // x, then 1 in y etc.
            if (spanX > minSpanX && (minSpanY == spanY || decX)) {
                return findReorderSolution(pixelX, pixelY, minSpanX, minSpanY, spanX - 1, spanY,
                        direction, dragView, false, solution);
            } else if (spanY > minSpanY) {
                return findReorderSolution(pixelX, pixelY, minSpanX, minSpanY, spanX, spanY - 1,
                        direction, dragView, true, solution);
            }
            solution.isSolution = false;
        } else {
            solution.isSolution = true;
            solution.cellX = result[0];
            solution.cellY = result[1];
            solution.spanX = spanX;
            solution.spanY = spanY;
        }
        return solution;
    }

    private void copyCurrentStateToSolution(ItemConfiguration solution, boolean temp) {
        int childCount = mWidgetContainerInner.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mWidgetContainerInner.getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            CellAndSpan c;
            if (temp) {
                c = new CellAndSpan(lp.tmpCellX, lp.tmpCellY, lp.cellHSpan, lp.cellVSpan);
            } else {
                c = new CellAndSpan(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan);
            }
            solution.add(child, c);
        }
    }

    private void copySolutionToTempState(ItemConfiguration solution, View dragView) {
        mTmpOccupied.clear();

        int childCount = mWidgetContainerInner.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mWidgetContainerInner.getChildAt(i);
            if (child == dragView) continue;
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            CellAndSpan c = solution.map.get(child);
            if (c != null) {
                lp.tmpCellX = c.cellX;
                lp.tmpCellY = c.cellY;
                lp.cellHSpan = c.spanX;
                lp.cellVSpan = c.spanY;
                mTmpOccupied.markCells(c, true);
            }
        }
        mTmpOccupied.markCells(solution, true);
    }

    private void animateItemsToSolution(ItemConfiguration solution, View dragView, boolean commitDragView) {
        GridOccupancy occupied = DESTRUCTIVE_REORDER ? mOccupied : mTmpOccupied;
        occupied.clear();

        int childCount = mWidgetContainerInner.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mWidgetContainerInner.getChildAt(i);
            if (child == dragView) continue;
            CellAndSpan c = solution.map.get(child);
            if (c != null) {
                animateChildToPosition(child, c.cellX, c.cellY, REORDER_ANIMATION_DURATION, 0,
                        DESTRUCTIVE_REORDER, false);
                occupied.markCells(c, true);
            }
        }
        if (commitDragView) {
            occupied.markCells(solution, true);
        }
    }


    // This method starts or changes the reorder preview animations
    private void beginOrAdjustReorderPreviewAnimations(ItemConfiguration solution,
            View dragView, int delay, int mode) {
        int childCount = mWidgetContainerInner.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mWidgetContainerInner.getChildAt(i);
            if (child == dragView) continue;
            CellAndSpan c = solution.map.get(child);
            boolean skip = mode == ReorderPreviewAnimation.MODE_HINT && solution.intersectingViews
                    != null && !solution.intersectingViews.contains(child);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (c != null && !skip) {
                ReorderPreviewAnimation rha = new ReorderPreviewAnimation(child, mode, lp.cellX,
                        lp.cellY, c.cellX, c.cellY, c.spanX, c.spanY);
                rha.animate();
            }
        }
    }

    // Class which represents the reorder preview animations. These animations show that an item is
    // in a temporary state, and hint at where the item will return to.
    class ReorderPreviewAnimation {
        final View child;
        float finalDeltaX;
        float finalDeltaY;
        float initDeltaX;
        float initDeltaY;
        final float finalScale;
        float initScale;
        final int mode;
        boolean repeating = false;

        private static final int PREVIEW_DURATION = 300;
        private static final int HINT_DURATION = 250;

        private static final float CHILD_DIVIDEND = 4.0f;

        public static final int MODE_HINT = 0;
        public static final int MODE_PREVIEW = 1;

        Animator a;

        public ReorderPreviewAnimation(View child, int mode, int cellX0, int cellY0, int cellX1,
                int cellY1, int spanX, int spanY) {
            regionToCenterPoint(cellX0, cellY0, spanX, spanY, mTmpPoint);
            final int x0 = mTmpPoint[0];
            final int y0 = mTmpPoint[1];
            regionToCenterPoint(cellX1, cellY1, spanX, spanY, mTmpPoint);
            final int x1 = mTmpPoint[0];
            final int y1 = mTmpPoint[1];
            final int dX = x1 - x0;
            final int dY = y1 - y0;

            this.child = child;
            this.mode = mode;
            setInitialAnimationValues(false);
            finalScale = (getChildrenScale() - (CHILD_DIVIDEND / child.getWidth())) * initScale;
            finalDeltaX = initDeltaX;
            finalDeltaY = initDeltaY;
            int dir = mode == MODE_HINT ? -1 : 1;
            if (dX == dY && dX == 0) {
            } else {
                if (dY == 0) {
                    finalDeltaX += - dir * Math.signum(dX) * mReorderPreviewAnimationMagnitude;
                } else if (dX == 0) {
                    finalDeltaY += - dir * Math.signum(dY) * mReorderPreviewAnimationMagnitude;
                } else {
                    double angle = Math.atan( (float) (dY) / dX);
                    finalDeltaX += (int) (- dir * Math.signum(dX) *
                            Math.abs(Math.cos(angle) * mReorderPreviewAnimationMagnitude));
                    finalDeltaY += (int) (- dir * Math.signum(dY) *
                            Math.abs(Math.sin(angle) * mReorderPreviewAnimationMagnitude));
                }
            }
        }

        void setInitialAnimationValues(boolean restoreOriginalValues) {
//            if (restoreOriginalValues) {
//                if (child instanceof LauncherAppWidgetHostView) {
//                    LauncherAppWidgetHostView lahv = (LauncherAppWidgetHostView) child;
//                    initScale = lahv.getScaleToFit();
//                    initDeltaX = lahv.getTranslationForCentering().x;
//                    initDeltaY = lahv.getTranslationForCentering().y;
//                } else {
//                    initScale = mChildScale;
//                    initDeltaX = 0;
//                    initDeltaY = 0;
//                }
//            } else {
                initScale = child.getScaleX();
                initDeltaX = child.getTranslationX();
                initDeltaY = child.getTranslationY();
//            }
        }

        void animate() {
            boolean noMovement = (finalDeltaX == initDeltaX) && (finalDeltaY == initDeltaY);

            if (mShakeAnimators.containsKey(child)) {
                ReorderPreviewAnimation oldAnimation = mShakeAnimators.get(child);
                oldAnimation.cancel();
                mShakeAnimators.remove(child);
                if (noMovement) {
                    completeAnimationImmediately();
                    return;
                }
            }
            if (noMovement) {
                return;
            }
            ValueAnimator va = AnimUtils.ofFloat(child, 0f, 1f);
            a = va;

            // Animations are disabled in power save mode, causing the repeated animation to jump
            // spastically between beginning and end states. Since this looks bad, we don't repeat
            // the animation in power save mode.
//            if (!Utilities.isPowerSaverPreventingAnimation(getContext())) {
                va.setRepeatMode(ValueAnimator.REVERSE);
                va.setRepeatCount(ValueAnimator.INFINITE);
//            }

            va.setDuration(mode == MODE_HINT ? HINT_DURATION : PREVIEW_DURATION);
            va.setStartDelay((int) (Math.random() * 60));
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float r = (Float) animation.getAnimatedValue();
                    float r1 = (mode == MODE_HINT && repeating) ? 1.0f : r;
                    float x = r1 * finalDeltaX + (1 - r1) * initDeltaX;
                    float y = r1 * finalDeltaY + (1 - r1) * initDeltaY;
                    child.setTranslationX(x);
                    child.setTranslationY(y);
                    float s = r * finalScale + (1 - r) * initScale;
                    child.setScaleX(s);
                    child.setScaleY(s);
                }
            });
            va.addListener(new AnimatorListenerAdapter() {
                public void onAnimationRepeat(Animator animation) {
                    // We make sure to end only after a full period
                    setInitialAnimationValues(true);
                    repeating = true;
                }
            });
            mShakeAnimators.put(child, this);
            va.start();
        }

        private void cancel() {
            if (a != null) {
                a.cancel();
            }
        }

        void completeAnimationImmediately() {
            if (a != null) {
                a.cancel();
            }

            setInitialAnimationValues(true);
            AnimatorSet s = AnimUtils.createAnimatorSet();
            a = s;
            s.playTogether(
                    AnimUtils.ofFloat(child, "scaleX", getChildrenScale()),
                    AnimUtils.ofFloat(child, "scaleY", getChildrenScale()),
                    AnimUtils.ofFloat(child, "translationX", 0f),
                    AnimUtils.ofFloat(child, "translationY", 0f)
            );
            s.setDuration(REORDER_ANIMATION_DURATION);
            s.setInterpolator(Interpolators.DEACCEL_1_5);
            s.start();
        }
    }

    private void completeAndClearReorderPreviewAnimations() {
        for (ReorderPreviewAnimation a: mShakeAnimators.values()) {
            a.completeAnimationImmediately();
        }
        mShakeAnimators.clear();
    }

    private void commitTempPlacement() {
        mTmpOccupied.copyTo(mOccupied);

        int childCount = mWidgetContainerInner.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mWidgetContainerInner.getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            ItemInfo info = (ItemInfo) child.getTag();
            // We do a null check here because the item info can be null in the case of the
            // AllApps button in the hotseat.
            if (info != null) {
                final boolean requiresDbUpdate = (info.cellX != lp.tmpCellX
                        || info.cellY != lp.tmpCellY || info.spanX != lp.cellHSpan
                        || info.spanY != lp.cellVSpan);

                info.cellX = lp.cellX = lp.tmpCellX;
                info.cellY = lp.cellY = lp.tmpCellY;
                info.spanX = lp.cellHSpan;
                info.spanY = lp.cellVSpan;

                if (requiresDbUpdate) {
                    // TODO:
//                    mLauncher.getModelWriter().modifyItemInDatabase(info, container, screenId,
//                            info.cellX, info.cellY, info.spanX, info.spanY);
                }
            }
        }
    }

    private void setUseTempCoords(boolean useTempCoords) {
        int childCount = mWidgetContainerInner.getChildCount();
        for (int i = 0; i < childCount; i++) {
            LayoutParams lp = (LayoutParams) mWidgetContainerInner.getChildAt(i).getLayoutParams();
            lp.useTmpCoords = useTempCoords;
        }
    }

    private ItemConfiguration findConfigurationNoShuffle(int pixelX, int pixelY, int minSpanX, int minSpanY,
            int spanX, int spanY, View dragView, ItemConfiguration solution) {
        int[] result = new int[2];
        int[] resultSpan = new int[2];
        findNearestVacantArea(pixelX, pixelY, minSpanX, minSpanY, spanX, spanY, result,
                resultSpan);
        if (result[0] >= 0 && result[1] >= 0) {
            copyCurrentStateToSolution(solution, false);
            solution.cellX = result[0];
            solution.cellY = result[1];
            solution.spanX = resultSpan[0];
            solution.spanY = resultSpan[1];
            solution.isSolution = true;
        } else {
            solution.isSolution = false;
        }
        return solution;
    }

    /* This seems like it should be obvious and straight-forward, but when the direction vector
    needs to match with the notion of the dragView pushing other views, we have to employ
    a slightly more subtle notion of the direction vector. The question is what two points is
    the vector between? The center of the dragView and its desired destination? Not quite, as
    this doesn't necessarily coincide with the interaction of the dragView and items occupying
    those cells. Instead we use some heuristics to often lock the vector to up, down, left
    or right, which helps make pushing feel right.
    */
    private void getDirectionVectorForDrop(int dragViewCenterX, int dragViewCenterY, int spanX,
            int spanY, View dragView, int[] resultDirection) {
        int[] targetDestination = new int[2];

        findNearestArea(dragViewCenterX, dragViewCenterY, spanX, spanY, targetDestination);
        Rect dragRect = new Rect();
        regionToRect(targetDestination[0], targetDestination[1], spanX, spanY, dragRect);
        dragRect.offset(dragViewCenterX - dragRect.centerX(), dragViewCenterY - dragRect.centerY());

        Rect dropRegionRect = new Rect();
        getViewsIntersectingRegion(targetDestination[0], targetDestination[1], spanX, spanY,
                dragView, dropRegionRect, mIntersectingViews);

        int dropRegionSpanX = dropRegionRect.width();
        int dropRegionSpanY = dropRegionRect.height();

        regionToRect(dropRegionRect.left, dropRegionRect.top, dropRegionRect.width(),
                dropRegionRect.height(), dropRegionRect);

        int deltaX = (dropRegionRect.centerX() - dragViewCenterX) / spanX;
        int deltaY = (dropRegionRect.centerY() - dragViewCenterY) / spanY;

        if (dropRegionSpanX == mUsedCellConfig.mCellCountX || spanX == mUsedCellConfig.mCellCountX) {
            deltaX = 0;
        }
        if (dropRegionSpanY == mUsedCellConfig.mCellCountY || spanY == mUsedCellConfig.mCellCountY) {
            deltaY = 0;
        }

        if (deltaX == 0 && deltaY == 0) {
            // No idea what to do, give a random direction.
            resultDirection[0] = 1;
            resultDirection[1] = 0;
        } else {
            computeDirectionVector(deltaX, deltaY, resultDirection);
        }
    }

    // For a given cell and span, fetch the set of views intersecting the region.
    private void getViewsIntersectingRegion(int cellX, int cellY, int spanX, int spanY,
            View dragView, Rect boundingRect, ArrayList<View> intersectingViews) {
        if (boundingRect != null) {
            boundingRect.set(cellX, cellY, cellX + spanX, cellY + spanY);
        }
        intersectingViews.clear();
        Rect r0 = new Rect(cellX, cellY, cellX + spanX, cellY + spanY);
        Rect r1 = new Rect();
        final int count = mWidgetContainerInner.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mWidgetContainerInner.getChildAt(i);
            if (child == dragView) continue;
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            r1.set(lp.cellX, lp.cellY, lp.cellX + lp.cellHSpan, lp.cellY + lp.cellVSpan);
            if (Rect.intersects(r0, r1)) {
                mIntersectingViews.add(child);
                if (boundingRect != null) {
                    boundingRect.union(r1);
                }
            }
        }
    }

    boolean isNearestDropLocationOccupied(int pixelX, int pixelY, int spanX, int spanY,
            View dragView, int[] result) {
        result = findNearestArea(pixelX, pixelY, spanX, spanY, result);
        getViewsIntersectingRegion(result[0], result[1], spanX, spanY, dragView, null,
                mIntersectingViews);
        return !mIntersectingViews.isEmpty();
    }

    void revertTempState() {
        completeAndClearReorderPreviewAnimations();
        if (isItemPlacementDirty() && !DESTRUCTIVE_REORDER) {
            final int count = mWidgetContainerInner.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = mWidgetContainerInner.getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY) {
                    lp.tmpCellX = lp.cellX;
                    lp.tmpCellY = lp.cellY;
                    animateChildToPosition(child, lp.cellX, lp.cellY, REORDER_ANIMATION_DURATION,
                            0, false, false);
                }
            }
            setItemPlacementDirty(false);
        }
    }

    boolean createAreaForResize(int cellX, int cellY, int spanX, int spanY,
            View dragView, int[] direction, boolean commit) {
        int[] pixelXY = new int[2];
        regionToCenterPoint(cellX, cellY, spanX, spanY, pixelXY);

        // First we determine if things have moved enough to cause a different layout
        ItemConfiguration swapSolution = findReorderSolution(pixelXY[0], pixelXY[1], spanX, spanY,
                spanX,  spanY, direction, dragView,  true,  new ItemConfiguration());

        setUseTempCoords(true);
        if (swapSolution != null && swapSolution.isSolution) {
            // If we're just testing for a possible location (MODE_ACCEPT_DROP), we don't bother
            // committing anything or animating anything as we just want to determine if a solution
            // exists
            copySolutionToTempState(swapSolution, dragView);
            setItemPlacementDirty(true);
            animateItemsToSolution(swapSolution, dragView, commit);

            if (commit) {
                commitTempPlacement();
                completeAndClearReorderPreviewAnimations();
                setItemPlacementDirty(false);
            } else {
                beginOrAdjustReorderPreviewAnimations(swapSolution, dragView,
                        REORDER_ANIMATION_DURATION, ReorderPreviewAnimation.MODE_PREVIEW);
            }
            mWidgetContainerInner.requestLayout();
        }
        return swapSolution.isSolution;
    }

    int[] performReorder(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX, int spanY,
            View dragView, int[] result, int resultSpan[], int mode) {
        // First we determine if things have moved enough to cause a different layout
        result = findNearestArea(pixelX, pixelY, spanX, spanY, result);

        if (resultSpan == null) {
            resultSpan = new int[2];
        }

        // When we are checking drop validity or actually dropping, we don't recompute the
        // direction vector, since we want the solution to match the preview, and it's possible
        // that the exact position of the item has changed to result in a new reordering outcome.
        // TODO
//        if ((mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL || mode == MODE_ACCEPT_DROP)
//                && mPreviousReorderDirection[0] != INVALID_DIRECTION) {
//            mDirectionVector[0] = mPreviousReorderDirection[0];
//            mDirectionVector[1] = mPreviousReorderDirection[1];
//            // We reset this vector after drop
//            if (mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL) {
//                mPreviousReorderDirection[0] = INVALID_DIRECTION;
//                mPreviousReorderDirection[1] = INVALID_DIRECTION;
//            }
//        } else {
//            getDirectionVectorForDrop(pixelX, pixelY, spanX, spanY, dragView, mDirectionVector);
//            mPreviousReorderDirection[0] = mDirectionVector[0];
//            mPreviousReorderDirection[1] = mDirectionVector[1];
//        }
//
//        // Find a solution involving pushing / displacing any items in the way
//        ItemConfiguration swapSolution = findReorderSolution(pixelX, pixelY, minSpanX, minSpanY,
//                spanX,  spanY, mDirectionVector, dragView,  true,  new ItemConfiguration());
//
//        // We attempt the approach which doesn't shuffle views at all
//        ItemConfiguration noShuffleSolution = findConfigurationNoShuffle(pixelX, pixelY, minSpanX,
//                minSpanY, spanX, spanY, dragView, new ItemConfiguration());
//
//        ItemConfiguration finalSolution = null;
//
//        // If the reorder solution requires resizing (shrinking) the item being dropped, we instead
//        // favor a solution in which the item is not resized, but
//        if (swapSolution.isSolution && swapSolution.area() >= noShuffleSolution.area()) {
//            finalSolution = swapSolution;
//        } else if (noShuffleSolution.isSolution) {
//            finalSolution = noShuffleSolution;
//        }
//
//        if (mode == MODE_SHOW_REORDER_HINT) {
//            if (finalSolution != null) {
//                beginOrAdjustReorderPreviewAnimations(finalSolution, dragView, 0,
//                        ReorderPreviewAnimation.MODE_HINT);
//                result[0] = finalSolution.cellX;
//                result[1] = finalSolution.cellY;
//                resultSpan[0] = finalSolution.spanX;
//                resultSpan[1] = finalSolution.spanY;
//            } else {
//                result[0] = result[1] = resultSpan[0] = resultSpan[1] = -1;
//            }
//            return result;
//        }
//
//        boolean foundSolution = true;
//        if (!DESTRUCTIVE_REORDER) {
//            setUseTempCoords(true);
//        }
//
//        if (finalSolution != null) {
//            result[0] = finalSolution.cellX;
//            result[1] = finalSolution.cellY;
//            resultSpan[0] = finalSolution.spanX;
//            resultSpan[1] = finalSolution.spanY;
//
//            // If we're just testing for a possible location (MODE_ACCEPT_DROP), we don't bother
//            // committing anything or animating anything as we just want to determine if a solution
//            // exists
//            if (mode == MODE_DRAG_OVER || mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL) {
//                if (!DESTRUCTIVE_REORDER) {
//                    copySolutionToTempState(finalSolution, dragView);
//                }
//                setItemPlacementDirty(true);
//                animateItemsToSolution(finalSolution, dragView, mode == MODE_ON_DROP);
//
//                if (!DESTRUCTIVE_REORDER &&
//                        (mode == MODE_ON_DROP || mode == MODE_ON_DROP_EXTERNAL)) {
//                    commitTempPlacement();
//                    completeAndClearReorderPreviewAnimations();
//                    setItemPlacementDirty(false);
//                } else {
//                    beginOrAdjustReorderPreviewAnimations(finalSolution, dragView,
//                            REORDER_ANIMATION_DURATION,  ReorderPreviewAnimation.MODE_PREVIEW);
//                }
//            }
//        } else {
//            foundSolution = false;
//            result[0] = result[1] = resultSpan[0] = resultSpan[1] = -1;
//        }
//
//        if ((mode == MODE_ON_DROP || !foundSolution) && !DESTRUCTIVE_REORDER) {
//            setUseTempCoords(false);
//        }

        mWidgetContainerInner.requestLayout();
        return result;
    }

    void setItemPlacementDirty(boolean dirty) {
        mItemPlacementDirty = dirty;
    }
    boolean isItemPlacementDirty() {
        return mItemPlacementDirty;
    }

    private static class ItemConfiguration extends CellAndSpan {
        final ArrayMap<View, CellAndSpan> map = new ArrayMap<>();
        private final ArrayMap<View, CellAndSpan> savedMap = new ArrayMap<>();
        final ArrayList<View> sortedViews = new ArrayList<>();
        ArrayList<View> intersectingViews;
        boolean isSolution = false;

        void save() {
            // Copy current state into savedMap
            for (View v: map.keySet()) {
                savedMap.get(v).copyFrom(map.get(v));
            }
        }

        void restore() {
            // Restore current state from savedMap
            for (View v: savedMap.keySet()) {
                map.get(v).copyFrom(savedMap.get(v));
            }
        }

        void add(View v, CellAndSpan cs) {
            map.put(v, cs);
            savedMap.put(v, new CellAndSpan());
            sortedViews.add(v);
        }

        int area() {
            return spanX * spanY;
        }

        void getBoundingRectForViews(ArrayList<View> views, Rect outRect) {
            boolean first = true;
            for (View v: views) {
                CellAndSpan c = map.get(v);
                if (first) {
                    outRect.set(c.cellX, c.cellY, c.cellX + c.spanX, c.cellY + c.spanY);
                    first = false;
                } else {
                    outRect.union(c.cellX, c.cellY, c.cellX + c.spanX, c.cellY + c.spanY);
                }
            }
        }
    }

    /**
     * Find a starting cell position that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     *
     * @param pixelX The X location at which you want to search for a vacant area.
     * @param pixelY The Y location at which you want to search for a vacant area.
     * @param spanX Horizontal span of the object.
     * @param spanY Vertical span of the object.
     * @param result Previously returned value to possibly recycle.
     * @return The X, Y cell of a vacant area that can contain this object,
     *         nearest the requested location.
     */
    public int[] findNearestArea(int pixelX, int pixelY, int spanX, int spanY, int[] result) {
        return findNearestArea(pixelX, pixelY, spanX, spanY, spanX, spanY, false, result, null);
    }

    boolean existsEmptyCell() {
        return findCellForSpan(null, 1, 1);
    }

    /**
     * Finds the upper-left coordinate of the first rectangle in the grid that can
     * hold a cell of the specified dimensions. If intersectX and intersectY are not -1,
     * then this method will only return coordinates for rectangles that contain the cell
     * (intersectX, intersectY)
     *
     * @param cellXY The array that will contain the position of a vacant cell if such a cell
     *               can be found.
     * @param spanX The horizontal span of the cell we want to find.
     * @param spanY The vertical span of the cell we want to find.
     *
     * @return True if a vacant cell of the specified dimension was found, false otherwise.
     */
    public boolean findCellForSpan(int[] cellXY, int spanX, int spanY) {
        if (cellXY == null) {
            cellXY = new int[2];
        }
        return mOccupied.findVacantCell(cellXY, spanX, spanY);
    }

    /**
     * A drag event has begun over this layout.
     * It may have begun over this layout (in which case onDragChild is called first),
     * or it may have begun on another layout.
     */
    void onDragEnter() {
        mDragging = true;
    }

    /**
     * Called when drag has left this CellLayout or has been completed (successfully or not)
     */
    void onDragExit() {
        // This can actually be called when we aren't in a drag, e.g. when adding a new
        // item to this layout via the customize drawer.
        // Guard against that case.
        if (mDragging) {
            mDragging = false;
        }

        // Invalidate the drag data
        mDragCell[0] = mDragCell[1] = -1;
        // TODO:
//        mDragOutlineAnims[mDragOutlineCurrent].animateOut();
//        mDragOutlineCurrent = (mDragOutlineCurrent + 1) % mDragOutlineAnims.length;
//        revertTempState();
//        setIsDragOverlapping(false);
    }

    /**
     * Mark a child as having been dropped.
     * At the beginning of the drag operation, the child may have been on another
     * screen, but it is re-parented before this method is called.
     *
     * @param child The child that is being dropped
     */
    void onDropChild(View child) {
        if (child != null) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.dropped = true;
            child.requestLayout();
            markCellsAsOccupiedForView(child);
        }
    }

    /**
     * Computes a bounding rectangle for a range of cells
     *
     * @param cellX X coordinate of upper left corner expressed as a cell position
     * @param cellY Y coordinate of upper left corner expressed as a cell position
     * @param cellHSpan Width in cells
     * @param cellVSpan Height in cells
     * @param resultRect Rect into which to put the results
     */
    public void cellToRect(int cellX, int cellY, int cellHSpan, int cellVSpan, Rect resultRect) {
        final int cellWidth = mUsedCellConfig.mCellWidth;
        final int cellHeight = mUsedCellConfig.mCellHeight;

        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();

        int width = cellHSpan * cellWidth;
        int height = cellVSpan * cellHeight;
        int x = hStartPadding + cellX * cellWidth;
        int y = vStartPadding + cellY * cellHeight;

        resultRect.set(x, y, x + width, y + height);
    }

    public void markCellsAsOccupiedForView(View view) {
        if (view == null || view.getParent() != mWidgetContainerInner) return;
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        mOccupied.markCells(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, true);
    }

    public void markCellsAsUnoccupiedForView(View view) {
        if (view == null || view.getParent() != mWidgetContainerInner) return;
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        mOccupied.markCells(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, false);
    }

    public int getDesiredWidth() {
        return getPaddingLeft() + getPaddingRight() + (mUsedCellConfig.mCellCountX * mUsedCellConfig.mCellWidth);
    }

    public int getDesiredHeight()  {
        return getPaddingTop() + getPaddingBottom() + (mUsedCellConfig.mCellCountY * mUsedCellConfig.mCellHeight);
    }

    public boolean isOccupied(int x, int y) {
        if (x < mUsedCellConfig.mCellCountX && y < mUsedCellConfig.mCellCountY) {
            return mOccupied.cells[x][y];
        } else {
            throw new RuntimeException("Position exceeds the bound of this CellLayout");
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CellLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CellLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new CellLayout.LayoutParams(p);
    }

    public static class CellLayoutAnimationController extends LayoutAnimationController {
        public CellLayoutAnimationController(Animation animation, float delay) {
            super(animation, delay);
        }

        @Override
        protected long getDelayForView(View view) {
            return (int) (Math.random() * 150);
        }
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        /**
         * Horizontal location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellY;

        /**
         * Temporary horizontal location of the item in the grid during reorder
         */
        public int tmpCellX;

        /**
         * Temporary vertical location of the item in the grid during reorder
         */
        public int tmpCellY;

        /**
         * Indicates that the temporary coordinates should be used to layout the items
         */
        public boolean useTmpCoords;

        /**
         * Number of cells spanned horizontally by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellHSpan;

        /**
         * Number of cells spanned vertically by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellVSpan;

        /**
         * Indicates whether the item will set its x, y, width and height parameters freely,
         * or whether these will be computed based on cellX, cellY, cellHSpan and cellVSpan.
         */
        public boolean isLockedToGrid = true;

        /**
         * Indicates that this item should use the full extents of its parent.
         */
        public boolean isFullscreen = false;

        /**
         * Indicates whether this item can be reordered. Always true except in the case of the
         * the AllApps button.
         */
        public boolean canReorder = true;

        // X coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        public int x;
        // Y coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        public int y;

        public boolean dropped;

        public boolean customPosition;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.cellX = source.cellX;
            this.cellY = source.cellY;
            this.cellHSpan = source.cellHSpan;
            this.cellVSpan = source.cellVSpan;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            customPosition = true;
        }

        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

        public void setup(int cellWidth, int cellHeight, int widthGap, int heightGap) {
            if (isLockedToGrid) {
                final int myCellHSpan = cellHSpan;
                final int myCellVSpan = cellVSpan;
                int myCellX = useTmpCoords ? tmpCellX : cellX;
                int myCellY = useTmpCoords ? tmpCellY : cellY;

                width = myCellHSpan * cellWidth + (myCellHSpan - 1) * widthGap -
                        leftMargin - rightMargin;
                height = myCellVSpan * cellHeight + (myCellVSpan - 1) * heightGap -
                        topMargin - bottomMargin;
                x = myCellX * (cellWidth + widthGap) + leftMargin;
                y = myCellY * (cellHeight + heightGap) + topMargin;
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("LayoutParams{");
            sb.append("width=").append(width);
            sb.append(", height=").append(height);
            sb.append(", cellX=").append(x);
            sb.append(", cellY=").append(y);
            sb.append(", cellX=").append(cellX);
            sb.append(", cellY=").append(cellY);
            sb.append(", cellHSpan=").append(cellHSpan);
            sb.append(", cellVSpan=").append(cellVSpan);
            sb.append('}');
            return sb.toString();
        }
    }

    // This class stores info for two purposes:
    // 1. When dragging items (mDragInfo in Workspace), we store the View, its cellX & cellY,
    //    its spanX, spanY, and the screen it is on
    // 2. When long clicking on an empty cell in a CellLayout, we save information about the
    //    cellX and cellY coordinates and which page was clicked. We then set this as a tag on
    //    the CellLayout that was long clicked
    static final class CellInfo {
        View cell;
        int cellX = -1;
        int cellY = -1;
        int spanX;
        int spanY;
        long container;

        CellInfo(View v, ItemInfo info) {
            cell = v;
            cellX = info.cellX;
            cellY = info.cellY;
            spanX = info.spanX;
            spanY = info.spanY;
            container = info.container;
        }

        @Override
        public String toString() {
            return "Cell[view=" + (cell == null ? "null" : cell.getClass())
                    + ", cellX=" + cellX + ", cellY=" + cellY + "]";
        }
    }

}
