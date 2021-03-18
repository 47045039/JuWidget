package com.ju.widget.impl.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ju.widget.impl.launcher3.util.TouchController;
import com.ju.widget.impl.launcher3.util.UiThreadHelper;

import java.util.ArrayList;

/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class DragController implements DragDriver.EventListener, TouchController {

    private static final String TAG = "DragController";

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    private final ArrayList<DropTarget> mDropTargets = new ArrayList<>();
    private final ArrayList<DragListener> mListeners = new ArrayList<>();

    private final Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];
    private final int[] mTmpPoint = new int[2];
    private final int[] mLastTouch = new int[2];

    private final Context mLauncher;
    private final ViewGroup mRootView;

    private DragDriver mDragDriver;
    private DropTarget mLastDropTarget;
    private DropTarget.DragObject mDragObject;
    private IBinder mWindowToken;

    private int mMotionDownX;
    private int mMotionDownY;
    private long mLastTouchUpTime = -1;

    public DragController(Context context, ViewGroup root) {
        mLauncher = context;
        mRootView = root;
    }

    public boolean isDragging() {
        return mDragDriver != null;
    }

    /**
     * 开始拖拽
     *
     * @param bitmap 当前被拖拽的View的图片
     * @param dragLayerX 当前被拖拽的View的相对于DragLayer的左上角X坐标
     * @param dragLayerY 当前被拖拽的View的相对于DragLayer的左上角Y坐标
     * @param source 当前被拖拽的View的父容器
     * @param dragInfo 当前被拖拽的View的信息
     */
    public DragView startDrag(Bitmap bitmap, int dragLayerX, int dragLayerY,
            DragSource source, ItemInfo dragInfo) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        UiThreadHelper.hideKeyboardAsync(mLauncher, mWindowToken);

        final int registrationX = mMotionDownX - dragLayerX;
        final int registrationY = mMotionDownY - dragLayerY;

        mLastDropTarget = null;

        final DropTarget.DragObject object = mDragObject = new DropTarget.DragObject();

        final DragView dragView = new DragView(mLauncher, this, mRootView, bitmap, registrationX, registrationY);
        dragView.setItemInfo(dragInfo);

        object.dragComplete = false;
        object.dragView = dragView;
        object.xOffset = mMotionDownX - dragLayerX;
        object.yOffset = mMotionDownY - dragLayerY;
        object.dragSource = source;
        object.dragInfo = dragInfo;
        object.originalDragInfo = new ItemInfo();
        object.originalDragInfo.copyFrom(dragInfo);

        mDragDriver = DragDriver.create(mLauncher, this, mDragObject);
        mRootView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        callOnDragStart();

        mLastTouch[0] = mMotionDownX;
        mLastTouch[1] = mMotionDownY;

        dragView.show(mMotionDownX, mMotionDownY);
        handleMoveEvent(mMotionDownX, mMotionDownY);

        return dragView;
    }

    public void animateDragViewToOriginalPosition(final Runnable onComplete, final View originIcon, int duration) {
        Runnable onCompleteRunnable = new Runnable() {
            @Override
            public void run() {
                if (originIcon != null) {
                    originIcon.setVisibility(View.VISIBLE);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        };
        mDragObject.dragView.animateTo(mMotionDownX, mMotionDownY, onCompleteRunnable, duration);
    }

    @Override
    public void onDriverDragMove(float x, float y) {
        final int[] dragLayerPos = getClampedDragLayerPos(x, y);
        handleMoveEvent(dragLayerPos[0], dragLayerPos[1]);
    }

    @Override
    public void onDriverDragExitWindow() {
        if (mLastDropTarget != null) {
            mLastDropTarget.onDragExit(mDragObject);
            mLastDropTarget = null;
        }
    }

    @Override
    public void onDriverDragEnd(float x, float y) {
        doDrop(findDropTarget((int) x, (int) y, mCoordinatesTemp));
        doDragEnd();
    }

    @Override
    public void onDriverDragCancel() {
        doDragCancel();
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                break;
            case MotionEvent.ACTION_UP:
                mLastTouchUpTime = System.currentTimeMillis();
                break;
        }

        return mDragDriver != null && mDragDriver.onInterceptTouchEvent(ev);
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onControllerTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                break;
        }

        return mDragDriver != null && mDragDriver.onTouchEvent(ev);
    }

    /**
     * Call this from a drag view.
     */
    public void onDragViewAnimationEnd() {
        if (mDragDriver != null) {
            mDragDriver.onDragViewAnimationEnd();
        }
    }

    private void handleMoveEvent(int x, int y) {
        mDragObject.dragView.move(x, y);

        // Drop on someone?
        final int[] coordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(x, y, coordinates);
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        checkTouchMove(dropTarget);

        mLastTouch[0] = x;
        mLastTouch[1] = y;
    }

    private void checkTouchMove(DropTarget dropTarget) {
        if (dropTarget != null) {
            if (mLastDropTarget != dropTarget) {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragObject);
                }
                dropTarget.onDragEnter(mDragObject);
            }
            dropTarget.onDragOver(mDragObject);
        } else {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
        }
        mLastDropTarget = dropTarget;
    }

    private int[] getClampedDragLayerPos(float x, float y) {
        mRootView.getLocalVisibleRect(mRectTemp);
        mTmpPoint[0] = (int) Math.max(mRectTemp.left, Math.min(x, mRectTemp.right - 1));
        mTmpPoint[1] = (int) Math.max(mRectTemp.top, Math.min(y, mRectTemp.bottom - 1));
        return mTmpPoint;
    }

    public void forceTouchMove() {
        int[] dummyCoordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(mLastTouch[0], mLastTouch[1], dummyCoordinates);
        mDragObject.x = dummyCoordinates[0];
        mDragObject.y = dummyCoordinates[1];
        checkTouchMove(dropTarget);
    }

    /**
     * Since accessible drag and drop won't cause the same sequence of touch events, we manually
     * inject the appropriate state.
     */
    public void prepareAccessibleDrag(int x, int y) {
        mMotionDownX = x;
        mMotionDownY = y;
    }

    /**
     * As above, since accessible drag and drop won't cause the same sequence of touch events,
     * we manually ensure appropriate drag and drop events get emulated for accessible drag.
     */
    public void completeAccessibleDrag(int[] location) {
        final int[] coordinates = mCoordinatesTemp;

        // We make sure that we prime the target for drop.
        DropTarget dropTarget = findDropTarget(location[0], location[1], coordinates);
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        checkTouchMove(dropTarget);

        dropTarget.prepareAccessibilityDrop();
        // Perform the drop
        doDrop(dropTarget);
        doDragEnd();
    }

    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        mDragObject.x = x;
        mDragObject.y = y;

        final Rect r = mRectTemp;
        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i = count - 1; i >= 0; i--) {
            DropTarget target = dropTargets.get(i);
            if (!target.isDropEnabled()) {
                continue;
            }

            target.getHitRectRelativeToDragLayer(r);
            if (r.contains(x, y)) {
                dropCoordinates[0] = x;
                dropCoordinates[1] = y;
                Utilities.mapCoordInSelfToDescendant((View) target, mRootView, dropCoordinates);
                return target;
            }
        }

        return null;
    }

    private void dispatchDropComplete(View dropTarget, boolean accepted) {
        if (!accepted) {
            mDragObject.deferDragViewCleanupPostAnimation = false;
        }

        mDragObject.dragSource.onDropCompleted(dropTarget, mDragObject, accepted);
    }

    private void doDrop(DropTarget dropTarget) {
        final int[] coordinates = mCoordinatesTemp;
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];

        // Move dragging to the final target.
        if (dropTarget != mLastDropTarget) {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mLastDropTarget = dropTarget;
            if (dropTarget != null) {
                dropTarget.onDragEnter(mDragObject);
            }
        }

        mDragObject.dragComplete = true;

        // Drop onto the target.
        boolean accepted = false;
        if (dropTarget != null) {
            dropTarget.onDragExit(mDragObject);

            if (dropTarget.acceptDrop(mDragObject)) {
                dropTarget.onDrop(mDragObject);
                accepted = true;
            }
        }

        final View dropTargetAsView = dropTarget instanceof View ? (View) dropTarget : null;
        dispatchDropComplete(dropTargetAsView, accepted);
    }

    private void doDragEnd() {
        if (isDragging()) {
            mDragDriver = null;
            boolean isDeferred = false;
            if (mDragObject.dragView != null) {
                isDeferred = mDragObject.deferDragViewCleanupPostAnimation;
                if (!isDeferred) {
                    mDragObject.dragView.remove();
                }
                mDragObject.dragView = null;
            }

            // Only end the drag if we are not deferred
            if (!isDeferred) {
                callOnDragEnd();
            }
        }
    }

    private void doDragCancel() {
        if (isDragging()) {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mDragObject.deferDragViewCleanupPostAnimation = false;
            mDragObject.cancelled = true;
            mDragObject.dragComplete = true;

            dispatchDropComplete(null, false);
        }
        doDragEnd();
    }

    private void callOnDragStart() {
        for (DragListener listener : new ArrayList<>(mListeners)) {
            listener.onDragStart(mDragObject);
        }
    }

    private void callOnDragEnd() {
        for (DragListener listener : new ArrayList<>(mListeners)) {
            listener.onDragEnd();
        }
    }

    /**
     * Sets the drag listener which will be notified when a drag starts or ends.
     */
    public void addDragListener(DragListener l) {
        mListeners.add(l);
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        mListeners.remove(l);
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     */
    public void addDropTarget(DropTarget target) {
        mDropTargets.add(target);
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
    }

    /**
     * Interface to receive notifications when a drag starts or stops
     */
    public interface DragListener {
        /**
         * A drag has begun
         *
         * @param dragObject The object being dragged
         */
        void onDragStart(DropTarget.DragObject dragObject);

        /**
         * The drag has ended
         */
        void onDragEnd();
    }

}
