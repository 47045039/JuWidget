package com.ju.widget.impl.launcher3.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;

import com.ju.widget.util.Log;

public class FirstFrameAnimHelper extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {

    private static final String TAG = "FirstFrameAnimHelper";

    private static final boolean DEBUG = false;
    private static final int MAX_DELAY = 1000;
    private static final int IDEAL_FRAME_DURATION = 16;
    private View mTarget;
    private long mStartFrame;
    private long mStartTime = -1;
    private boolean mHandlingOnAnimationUpdate;
    private boolean mAdjustedSecondFrameTime;

    private static ViewTreeObserver.OnDrawListener sGlobalDrawListener;
    private static long sGlobalFrameCounter;
    private static boolean sVisible;

    public FirstFrameAnimHelper(ValueAnimator animator, View target) {
        mTarget = target;
        animator.addUpdateListener(this);
    }

    public FirstFrameAnimHelper(ViewPropertyAnimator vpa, View target) {
        mTarget = target;
        vpa.setListener(this);
    }

    // only used for ViewPropertyAnimators
    public void onAnimationStart(Animator animation) {
        final ValueAnimator va = (ValueAnimator) animation;
        va.addUpdateListener(FirstFrameAnimHelper.this);
        onAnimationUpdate(va);
    }

    public static void setIsVisible(boolean visible) {
        sVisible = visible;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void initializeDrawListener(View view) {
        if (sGlobalDrawListener != null) {
            view.getViewTreeObserver().removeOnDrawListener(sGlobalDrawListener);
        }
        sGlobalDrawListener = new ViewTreeObserver.OnDrawListener() {
                private long mTime = System.currentTimeMillis();
                public void onDraw() {
                    sGlobalFrameCounter++;
                    if (DEBUG) {
                        long newTime = System.currentTimeMillis();
                        Log.d(TAG, "TICK " + (newTime - mTime));
                        mTime = newTime;
                    }
                }
            };
        view.getViewTreeObserver().addOnDrawListener(sGlobalDrawListener);
        sVisible = true;
    }

    public void onAnimationUpdate(final ValueAnimator animation) {
        final long currentTime = System.currentTimeMillis();
        if (mStartTime == -1) {
            mStartFrame = sGlobalFrameCounter;
            mStartTime = currentTime;
        }

        final long currentPlayTime = animation.getCurrentPlayTime();
        boolean isFinalFrame = Float.compare(1f, animation.getAnimatedFraction()) == 0;

        if (!mHandlingOnAnimationUpdate &&
            sVisible &&
            // If the current play time exceeds the duration, or the animated fraction is 1,
            // the animation will get finished, even if we call setCurrentPlayTime -- therefore
            // don't adjust the animation in that case
            currentPlayTime < animation.getDuration() && !isFinalFrame) {
            mHandlingOnAnimationUpdate = true;
            long frameNum = sGlobalFrameCounter - mStartFrame;
            // If we haven't drawn our first frame, reset the time to t = 0
            // (give up after MAX_DELAY ms of waiting though - might happen, for example, if we
            // are no longer in the foreground and no frames are being rendered ever)
            if (frameNum == 0 && currentTime < mStartTime + MAX_DELAY && currentPlayTime > 0) {
                // The first frame on animations doesn't always trigger an invalidate...
                // force an invalidate here to make sure the animation continues to advance
                mTarget.getRootView().invalidate();
                animation.setCurrentPlayTime(0);
            // For the second frame, if the first frame took more than 16ms,
            // adjust the start time and pretend it took only 16ms anyway. This
            // prevents a large jump in the animation due to an expensive first frame
            } else if (frameNum == 1 && currentTime < mStartTime + MAX_DELAY &&
                       !mAdjustedSecondFrameTime &&
                       currentTime > mStartTime + IDEAL_FRAME_DURATION &&
                       currentPlayTime > IDEAL_FRAME_DURATION) {
                animation.setCurrentPlayTime(IDEAL_FRAME_DURATION);
                mAdjustedSecondFrameTime = true;
            } else {
                if (frameNum > 1) {
                    mTarget.post(new Runnable() {
                            public void run() {
                                animation.removeUpdateListener(FirstFrameAnimHelper.this);
                            }
                        });
                }
                if (DEBUG) print(animation);
            }
            mHandlingOnAnimationUpdate = false;
        } else {
            if (DEBUG) print(animation);
        }
    }

    public void print(ValueAnimator animation) {
        float flatFraction = animation.getCurrentPlayTime() / (float) animation.getDuration();
        Log.d(TAG, sGlobalFrameCounter +
              "(" + (sGlobalFrameCounter - mStartFrame) + ") " + mTarget + " dirty? " +
              mTarget.isDirty() + " " + flatFraction + " " + this + " " + animation);
    }
}
