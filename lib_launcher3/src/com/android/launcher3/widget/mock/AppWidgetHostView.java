/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.widget.mock;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * Provides the glue to show AppWidget views. This class offers automatic animation
 * between updates, and will try recycling old views for each incoming
 * {@link RemoteViews}.
 */
public class AppWidgetHostView extends FrameLayout {

    static final String TAG = "AppWidgetHostView";
    private static final String KEY_JAILED_ARRAY = "jail";

    static final int VIEW_MODE_NOINIT = 0;
    static final int VIEW_MODE_CONTENT = 1;
    static final int VIEW_MODE_ERROR = 2;
    static final int VIEW_MODE_DEFAULT = 3;

    Context mContext;

    int mAppWidgetId;
    AppWidgetProviderInfo mInfo;
    View mView;
    int mViewMode = VIEW_MODE_NOINIT;

    /**
     * Create a host view.  Uses default fade animations.
     */
    public AppWidgetHostView(Context context) {
        this(context, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Create a host view. Uses specified animations when pushing
     * { #updateAppWidget(RemoteViews)}.
     *
     * @param animationIn Resource ID of in animation to use
     * @param animationOut Resource ID of out animation to use
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public AppWidgetHostView(Context context, int animationIn, int animationOut) {
        super(context);
        mContext = context;
    }

    /**
     * Set the AppWidget that will be displayed by this view. This method also adds default padding
     * to widgets, as described in {@link #getDefaultPaddingForWidget(Context, Rect)}
     * and can be overridden in order to add custom padding.
     */
    public void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
        mAppWidgetId = appWidgetId;
        mInfo = info;

        // We add padding to the AppWidgetHostView if necessary
        Rect padding = getDefaultPadding();
        setPadding(padding.left, padding.top, padding.right, padding.bottom);

        // Sometimes the AppWidgetManager returns a null AppWidgetProviderInfo object for
        // a widget, eg. for some widgets in safe mode.
        if (info != null) {
            String description = info.loadLabel(getContext().getPackageManager());
            setContentDescription(description);
        }
    }

    /**
     * As of ICE_CREAM_SANDWICH we are automatically adding padding to widgets targeting
     * ICE_CREAM_SANDWICH and higher. The new widget design guidelines strongly recommend
     * that widget developers do not add extra padding to their widgets. This will help
     * achieve consistency among widgets.
     *
     * Note: this method is only needed by developers of AppWidgetHosts. The method is provided in
     * order for the AppWidgetHost to account for the automatic padding when computing the number
     * of cells to allocate to a particular widget.
     *
     * @param context the current context
     * @param padding Rect in which to place the output, if null, a new Rect will be allocated and
     *                returned
     * @return default padding for this widget, in pixels
     */
    public static Rect getDefaultPaddingForWidget(Context context,
                                                   Rect padding) {
        if (padding == null) {
            padding = new Rect(0, 0, 0, 0);
        } else {
            padding.set(0, 0, 0, 0);
        }
        return padding;
    }

    private Rect getDefaultPadding() {
        return getDefaultPaddingForWidget(mContext,
                null);
    }

    public int getAppWidgetId() {
        return mAppWidgetId;
    }

    public AppWidgetProviderInfo getAppWidgetInfo() {
        return mInfo;
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        final SparseArray<Parcelable> jail = new SparseArray<>();
        super.dispatchSaveInstanceState(jail);

        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(KEY_JAILED_ARRAY, jail);
        container.put(generateId(), bundle);
    }

    private int generateId() {
        final int id = getId();
        return id == View.NO_ID ? mAppWidgetId : id;
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        final Parcelable parcelable = container.get(generateId());

        SparseArray<Parcelable> jail = null;
        if (parcelable instanceof Bundle) {
            jail = ((Bundle) parcelable).getSparseParcelableArray(KEY_JAILED_ARRAY);
        }

        if (jail == null) jail = new SparseArray<>();

        try  {
            super.dispatchRestoreInstanceState(jail);
        } catch (Exception e) {
            Log.e(TAG, "failed to restoreInstanceState for widget id: " + mAppWidgetId + ", "
                    + (mInfo == null ? "null" : mInfo.provider), e);
        }
    }

    /**
     * Provide guidance about the size of this widget to the AppWidgetManager. The widths and
     * heights should correspond to the full area the AppWidgetHostView is given. Padding added by
     * the framework will be accounted for automatically. This information gets embedded into the
     * AppWidget options and causes a callback to the AppWidgetProvider.
     *  AppWidgetProvider#onAppWidgetOptionsChanged(Context, AppWidgetManager, int, Bundle)
     *
     * @param newOptions The bundle of options, in addition to the size information,
     *          can be null.
     * @param minWidth The minimum width in dips that the widget will be displayed at.
     * @param minHeight The maximum height in dips that the widget will be displayed at.
     * @param maxWidth The maximum width in dips that the widget will be displayed at.
     * @param maxHeight The maximum height in dips that the widget will be displayed at.
     *
     */
    public void updateAppWidgetSize(Bundle newOptions, int minWidth, int minHeight, int maxWidth,
            int maxHeight) {
        updateAppWidgetSize(newOptions, minWidth, minHeight, maxWidth, maxHeight, false);
    }

    /**
     */
    public void updateAppWidgetSize(Bundle newOptions, int minWidth, int minHeight, int maxWidth,
            int maxHeight, boolean ignorePadding) {
        if (newOptions == null) {
            newOptions = new Bundle();
        }

        Rect padding = getDefaultPadding();
        float density = getResources().getDisplayMetrics().density;

        int xPaddingDips = (int) ((padding.left + padding.right) / density);
        int yPaddingDips = (int) ((padding.top + padding.bottom) / density);

        int newMinWidth = minWidth - (ignorePadding ? 0 : xPaddingDips);
        int newMinHeight = minHeight - (ignorePadding ? 0 : yPaddingDips);
        int newMaxWidth = maxWidth - (ignorePadding ? 0 : xPaddingDips);
        int newMaxHeight = maxHeight - (ignorePadding ? 0 : yPaddingDips);

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);

        // We get the old options to see if the sizes have changed
        Bundle oldOptions = widgetManager.getAppWidgetOptions(mAppWidgetId);
        boolean needsUpdate = false;
        if (newMinWidth != oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ||
                newMinHeight != oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ||
                newMaxWidth != oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) ||
                newMaxHeight != oldOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)) {
            needsUpdate = true;
        }

        if (needsUpdate) {
            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, newMinWidth);
            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, newMinHeight);
            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, newMaxWidth);
            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, newMaxHeight);
            updateAppWidgetOptions(newOptions);
        }
    }

    /**
     * Specify some extra information for the widget provider. Causes a callback to the
     * AppWidgetProvider.
     * AppWidgetProvider#onAppWidgetOptionsChanged(Context, AppWidgetManager, int, Bundle)
     *
     * @param options The bundle of options information.
     */
    public void updateAppWidgetOptions(Bundle options) {
        AppWidgetManager.getInstance(mContext).updateAppWidgetOptions(mAppWidgetId, options);
    }

    /**
     * 更新数据
     * @param widgetView
     */
    public void updateAppWidget(Object widgetView) {

    }

    /**
     * Prepare the given view to be shown. This might include adjusting
     * {@link LayoutParams} before inserting.
     */
    protected void prepareView(View view) {
        // Take requested dimensions from child, but apply default gravity.
        LayoutParams requested = (LayoutParams)view.getLayoutParams();
        if (requested == null) {
            requested = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
        }

        requested.gravity = Gravity.CENTER;
        view.setLayoutParams(requested);
    }

    /**
     * Inflate and return a view that represents an error state.
     */
    protected View getErrorView() {
        TextView tv = new TextView(mContext);
        tv.setText("this is an error view");
        return tv;
    }
}
