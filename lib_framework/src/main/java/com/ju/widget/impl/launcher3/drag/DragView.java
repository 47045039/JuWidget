package com.ju.widget.impl.launcher3.drag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import com.ju.widget.impl.launcher3.ItemInfo;
import com.ju.widget.impl.launcher3.Utilities;
import com.ju.widget.impl.launcher3.util.AnimUtils;

public class DragView extends View {

    public static final int VIEW_ZOOM_DURATION = 150;

    private static float sDragAlpha = 1f;

    private final int[] mTempLoc = new int[2];

    private final Context mContext;
    private final DragController mDragController;
    private final ViewGroup mRootView;
    private final Bitmap mBitmap;
    private final Paint mPaint;
    private final ValueAnimator mAnim;

    private final int mRegistrationX;
    private final int mRegistrationY;
    private final float mInitialScale;
    private final float mScaleOnDrop;

    private Rect mDragRegion = null;

    private boolean mHasDrawn = false;
    private boolean mDrawBitmap = true;
    private boolean mAnimationCancelled = false;

    private int mLastTouchX;
    private int mLastTouchY;

    public DragView(Context context, DragController dc, ViewGroup root, Bitmap bitmap,
            int registrationX, int registrationY) {
        this(context, dc, root, bitmap, registrationX, registrationY, 1, 1);
    }

    public DragView(Context context, DragController dc, ViewGroup root, Bitmap bitmap,
            int registrationX, int registrationY, final float initialScale, final float scaleOnDrop) {
        super(context);
        mContext = context;
        mDragController = dc;
        mRootView = root;
        mBitmap = bitmap;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

        setElevation(20);

        // The point in our scaled bitmap that the touch events are located
        mRegistrationX = registrationX;
        mRegistrationY = registrationY;

        mInitialScale = initialScale;
        mScaleOnDrop = scaleOnDrop;

        // Set the initial scale to avoid any jumps
        setScaleX(initialScale);
        setScaleY(initialScale);

        final float scale = 1;

        // Animate the view into the correct position
        mAnim = AnimUtils.ofFloat(this, 0f, 1f);
        mAnim.setDuration(VIEW_ZOOM_DURATION);
        mAnim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation.getAnimatedValue();

                setScaleX(initialScale + (value * (scale - initialScale)));
                setScaleY(initialScale + (value * (scale - initialScale)));
                if (sDragAlpha != 1f) {
                    setAlpha(sDragAlpha * value + (1f - value));
                }

                if (getParent() == null) {
                    animation.cancel();
                }
            }
        });

        mAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mAnimationCancelled) {
                    mDragController.onDragViewAnimationEnd();
                }
            }
        });

        setDragRegion(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));

        // Force a measure, because Workspace uses getMeasuredHeight() before the layout pass
        int ms = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        measure(ms, ms);
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        mPaint.setAlpha((int) (255 * alpha));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mHasDrawn = true;
        if (mDrawBitmap) {
            canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
        }
    }

    public void setItemInfo(final ItemInfo info) {
        if (!(Utilities.ATLEAST_OREO)) {
            return;
        }

        // TODO:
    }

    public void setDragRegion(Rect r) {
        mDragRegion = r;
    }

    public Rect getDragRegion() {
        return mDragRegion;
    }

    public Bitmap getPreviewBitmap() {
        return mBitmap;
    }

    public float getInitialScale() {
        return mInitialScale;
    }

    public boolean hasDrawn() {
        return mHasDrawn;
    }

    /**
     * Create a window containing this view and show it.
     *
     * @param touchX the x coordinate the user touched in DragLayer coordinates
     * @param touchY the y coordinate the user touched in DragLayer coordinates
     */
    public void show(int touchX, int touchY) {
        mRootView.addView(this);

        setLayoutParams(new CellLayout.LayoutParams(mBitmap.getWidth(), mBitmap.getHeight()));
        move(touchX, touchY);

        // Post the animation to skip other expensive work happening on the first frame
        post(new Runnable() {
            public void run() {
                mAnim.start();
            }
        });
    }

    /**
     * Move the window containing this view.
     *
     * @param touchX the x coordinate the user touched in DragLayer coordinates
     * @param touchY the y coordinate the user touched in DragLayer coordinates
     */
    public void move(int touchX, int touchY) {
        mLastTouchX = touchX;
        mLastTouchY = touchY;
        applyTranslation();
    }

    public void remove() {
        if (getParent() != null) {
            mRootView.removeView(DragView.this);
        }
    }

    public void animateTo(int toTouchX, int toTouchY, Runnable onCompleteRunnable, int duration) {
        mTempLoc[0] = toTouchX - mRegistrationX;
        mTempLoc[1] = toTouchY - mRegistrationY;
//        mRootView.animateViewIntoPosition(this, mTempLoc, 1f, mScaleOnDrop, mScaleOnDrop,
//                DragLayer.ANIMATION_END_DISAPPEAR, onCompleteRunnable, duration);
    }

    public void cancelAnimation() {
        mAnimationCancelled = true;
        if (mAnim != null && mAnim.isRunning()) {
            mAnim.cancel();
        }
    }

    private void applyTranslation() {
        setTranslationX(mLastTouchX - mRegistrationX);
        setTranslationY(mLastTouchY - mRegistrationY);
    }

}
