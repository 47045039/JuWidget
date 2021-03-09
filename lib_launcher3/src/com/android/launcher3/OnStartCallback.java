package com.android.launcher3;

/**
 * Callback for listening for onStart
 */
public interface OnStartCallback<T extends Launcher> {

    void onActivityStart(T activity);
}
