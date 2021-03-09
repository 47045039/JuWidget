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

package com.android.launcher3;

import static android.app.Activity.RESULT_CANCELED;

import com.android.launcher3.widget.mock.AppWidgetHost;
import com.android.launcher3.widget.mock.AppWidgetHostView;
import com.android.launcher3.widget.mock.AppWidgetManager;
import com.android.launcher3.widget.mock.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Handler;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.widget.DeferredAppWidgetHostView;
import com.android.launcher3.widget.LauncherAppWidgetHostView;
import com.android.launcher3.widget.mock.WidgetServer;

import java.util.ArrayList;


/**
 * Specific {@link AppWidgetHost} that creates our {@link LauncherAppWidgetHostView}
 * which correctly captures all long-press events. This ensures that users can
 * always pick up and move widgets.
 */
public class LauncherAppWidgetHost extends AppWidgetHost {

    private static final int FLAG_LISTENING = 1;
    private static final int FLAG_RESUMED = 1 << 1;
    private static final int FLAG_LISTEN_IF_RESUMED = 1 << 2;

    public static final int APPWIDGET_HOST_ID = 1024;

    private final ArrayList<ProviderChangedListener> mProviderChangeListeners = new ArrayList<>();
    private final SparseArray<LauncherAppWidgetHostView> mViews = new SparseArray<>();

    private final Context mContext;
    private int mFlags = FLAG_RESUMED;

    public LauncherAppWidgetHost(Context context) {
        super(context, APPWIDGET_HOST_ID);
        mContext = context;
    }

    @Override
    protected LauncherAppWidgetHostView onCreateView(Context context, int appWidgetId,
            AppWidgetProviderInfo appWidget) {
        LauncherAppWidgetHostView launcherAppWidgetHostView = new LauncherAppWidgetHostView(context);
        View widgetView = WidgetServer.getInstance(context).getWidgetView(appWidgetId);
        launcherAppWidgetHostView.addView(widgetView);
        mViews.put(appWidgetId, launcherAppWidgetHostView);
        return launcherAppWidgetHostView;
    }

    @Override
    public void startListening() {
        if (FeatureFlags.GO_DISABLE_WIDGETS) {
            return;
        }
        mFlags |= FLAG_LISTENING;
        try {
            super.startListening();
        } catch (Exception e) {
            if (!Utilities.isBinderSizeError(e)) {
                throw new RuntimeException(e);
            }
            // We're willing to let this slide. The exception is being caused by the list of
            // RemoteViews which is being passed back. The startListening relationship will
            // have been established by this point, and we will end up populating the
            // widgets upon bind anyway. See issue 14255011 for more context.
        }

        // We go in reverse order and inflate any deferred widget
        for (int i = mViews.size() - 1; i >= 0; i--) {
            LauncherAppWidgetHostView view = mViews.valueAt(i);
            if (view instanceof DeferredAppWidgetHostView) {
                view.reInflate();
            }
        }
    }

    @Override
    public void stopListening() {
        if (FeatureFlags.GO_DISABLE_WIDGETS) {
            return;
        }
        mFlags &= ~FLAG_LISTENING;
        super.stopListening();
    }

    /**
     * Updates the resumed state of the host.
     * When a host is not resumed, it defers calls to startListening until host is resumed again.
     * But if the host was already listening, it will not call stopListening.
     *
     * @see #setListenIfResumed(boolean)
     */
    public void setResumed(boolean isResumed) {
        if (isResumed == ((mFlags & FLAG_RESUMED) != 0)) {
            return;
        }
        if (isResumed) {
            mFlags |= FLAG_RESUMED;
            // Start listening if we were supposed to start listening on resume
            if ((mFlags & FLAG_LISTEN_IF_RESUMED) != 0 && (mFlags & FLAG_LISTENING) == 0) {
                startListening();
            }
        } else {
            mFlags &= ~FLAG_RESUMED;
        }
    }

    /**
     * Updates the listening state of the host. If the host is not resumed, startListening is
     * deferred until next resume.
     *
     * @see #setResumed(boolean)
     */
    public void setListenIfResumed(boolean listenIfResumed) {
        if (!Utilities.ATLEAST_NOUGAT_MR1) {
            return;
        }
        if (listenIfResumed == ((mFlags & FLAG_LISTEN_IF_RESUMED) != 0)) {
            return;
        }
        if (listenIfResumed) {
            mFlags |= FLAG_LISTEN_IF_RESUMED;
            if ((mFlags & FLAG_RESUMED) != 0) {
                // If we are resumed, start listening immediately. Note we do not check for
                // duplicate calls before calling startListening as startListening is safe to call
                // multiple times.
                startListening();
            }
        } else {
            mFlags &= ~FLAG_LISTEN_IF_RESUMED;
            stopListening();
        }
    }

    @Override
    public int allocateAppWidgetId(int widgetId) {
        if (FeatureFlags.GO_DISABLE_WIDGETS) {
            return AppWidgetManager.INVALID_APPWIDGET_ID;
        }

        return super.allocateAppWidgetId(widgetId);
    }

    public void addProviderChangeListener(ProviderChangedListener callback) {
        mProviderChangeListeners.add(callback);
    }

    public void removeProviderChangeListener(ProviderChangedListener callback) {
        mProviderChangeListeners.remove(callback);
    }

    public AppWidgetHostView createView(Context context, int appWidgetId,
            LauncherAppWidgetProviderInfo appWidget) {
        try {
            return super.createView(context, appWidgetId, appWidget);
        } catch (Exception e) {
            // If the exception was thrown while fetching the remote views, let the view stay.
            // This will ensure that if the widget posts a valid update later, the view
            // will update.
            LauncherAppWidgetHostView view = mViews.get(appWidgetId);
            if (view == null) {
                view = onCreateView(mContext, appWidgetId, appWidget);
            }
            view.setAppWidget(appWidgetId, appWidget);
            view.switchToErrorView();
            return  view;
        }
    }

    @Override
    public void deleteAppWidgetId(int appWidgetId) {
        super.deleteAppWidgetId(appWidgetId);
        mViews.remove(appWidgetId);
    }

    @Override
    public void clearViews() {
        super.clearViews();
        mViews.clear();
    }

    public void startBindFlow(IBaseActivity activity,
                              int appWidgetId, int requestCode) {
    }


    public void startConfigActivity(Launcher activity, int widgetId, int requestCode) {
        if (FeatureFlags.GO_DISABLE_WIDGETS) {
            sendActionCancelled(activity, requestCode);
            return;
        }

        try {
            startAppWidgetConfigureActivityForResult(activity, widgetId, 0, requestCode, null);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            sendActionCancelled(activity, requestCode);
        }
    }

    private void sendActionCancelled(final Launcher activity, final int requestCode) {
        new Handler().post(() -> activity.onActivityResult(requestCode, RESULT_CANCELED, null));
    }

    /**
     * Listener for getting notifications on provider changes.
     */
    public interface ProviderChangedListener {

        void notifyWidgetProvidersChanged();
    }
}
