/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * AppWidgetHost provides the interaction with the AppWidget service for apps,
 * like the home screen, that want to embed AppWidgets in their UI.
 */
public class AppWidgetHost {

    static final int HANDLE_UPDATE = 1;
    static final int HANDLE_PROVIDER_CHANGED = 2;
    static final int HANDLE_PROVIDERS_CHANGED = 3;
    static final int HANDLE_VIEW_DATA_CHANGED = 4;

    private DisplayMetrics mDisplayMetrics;
    private int hostId;
    private Looper looper;

    private final HashMap<Integer, AppWidgetHostView> mViews = new HashMap<>();

    public AppWidgetHost(Context context, int hostId) {
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        this.hostId = hostId;
        this.looper = context.getMainLooper();
    }

    /**
     * Start receiving onAppWidgetChanged calls for your AppWidgets.  Call this when your activity
     * becomes visible, i.e. from onStart() in your Activity.
     */
    public void startListening() {
    }

    /**
     * Stop receiving onAppWidgetChanged calls for your AppWidgets.  Call this when your activity is
     * no longer visible, i.e. from onStop() in your Activity.
     */
    public void stopListening() {
    }

    /**
     * Get a appWidgetId for a host in the calling process.
     *
     * @return a appWidgetId
     * @param widgetId
     */
    public int allocateAppWidgetId(int widgetId) {
        return widgetId;
    }

    /**
     * Starts an app widget provider configure activity for result on behalf of the caller.
     * Use this method if the provider is in another profile as you are not allowed to start
     * an activity in another profile. You can optionally provide a request code that is
     * returned in {Activity.onActivityResult(int, int, android.content.Intent)} and
     * an options bundle to be passed to the started activity.
     * <p>
     * Note that the provided app widget has to be bound for this method to work.
     * </p>
     *
     * @param activity The activity from which to start the configure one.
     * @param appWidgetId The bound app widget whose provider's config activity to start.
     * @param requestCode Optional request code retuned with the result.
     * @param intentFlags Optional intent flags.
     *
     * @throws ActivityNotFoundException If the activity is not found.
     *
     * @see AppWidgetProviderInfo#getProfile()
     */
    public final void startAppWidgetConfigureActivityForResult(@NonNull Activity activity,
            int appWidgetId, int intentFlags, int requestCode, @Nullable Bundle options) {
    }

    /**Host中的WidgetId的数组
     * Gets a list of all the appWidgetIds that are bound to the current host
     */
    public int[] getAppWidgetIds() {
        int size = mViews.size();
        int[] widgetIdArray = new int[size];
        for (int index=0;index<size;index++)
            widgetIdArray[index] = mViews.get(index).mAppWidgetId;
        return widgetIdArray;
    }

    /**
     * Stop listening to changes for this AppWidget.
     */
    public void deleteAppWidgetId(int appWidgetId) {
        /*通知IntentService、WidgetServer停止接受更新*/
    }

    /**
     * Remove all records about this host from the AppWidget manager.
     * <ul>
     *   <li>Call this when initializing your database, as it might be because of a data wipe.</li>
     *   <li>Call this to have the AppWidget manager release all resources associated with your
     *   host.  Any future calls about this host will cause the records to be re-allocated.</li>
     * </ul>
     */
    public void deleteHost() {
    }

    /**
     * Create the AppWidgetHostView for the given widget.
     * The AppWidgetHost retains a pointer to the newly-created View.
     */
    public final AppWidgetHostView createView(Context context, int appWidgetId,
                                              AppWidgetProviderInfo appWidget) {
        AppWidgetHostView appWidgetHostView = onCreateView(context, appWidgetId, appWidget);
        appWidgetHostView.setAppWidget(appWidgetId, appWidget);
        synchronized (mViews) {
            mViews.put(appWidgetId, appWidgetHostView);
        }

        return appWidgetHostView;
    }

    /**
     * Called to create the AppWidgetHostView.  Override to return a custom subclass if you
     * need it.  {@more}
     */
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
                                             AppWidgetProviderInfo appWidget) {
        AppWidgetHostView appWidgetHostView = new AppWidgetHostView(context);
        View widgetView = WidgetServer.getInstance(context).getWidgetView(appWidgetId);
        appWidgetHostView.addView(widgetView);
        return appWidgetHostView;
    }

    /**
     * Clear the list of Views that have been created by this AppWidgetHost.
     */
    protected void clearViews() {
        synchronized (mViews) {
            mViews.clear();
        }
    }
}


